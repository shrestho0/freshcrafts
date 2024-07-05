import { AUTH_COOKIE_NAME, GITHUB_CLIENT_SECRET } from "$env/static/private";
import { PUBLIC_GITHUB_CLIENT_ID } from "$env/static/public";
import type { PageServerLoad } from "./$types";


interface OAuthDataRequestDto { }

interface OAuthGithubDataRequestDto extends OAuthDataRequestDto {
    token: string;
    userInfo: string;
    dataJson: string;
}

interface OAuthGoogleDataRequestDto extends OAuthDataRequestDto {
    token: string;
    user: string;
    dataJson: string;
}

export const load: PageServerLoad = async ({ locals, url, cookies }) => {
    const oProvider = url.searchParams.get('p');
    if (!oProvider) {
        return {
            success: false,
            message: 'OAuth provider not found'
        };
    }





    switch (oProvider) {

        case 'github':

            const dataToSend: OAuthGithubDataRequestDto = {
                token: "",
                userInfo: "",
                data: ""
            };

            const oData = {
                provider: url.searchParams.get("p"),
                code: url.searchParams.get("code"),
                state: url.searchParams.get("state")
            }
            const savedState = cookies.get("gh_state")

            console.log(oData, savedState)

            if (savedState != oData.state) {
                return {
                    success: false,
                    message: "State mismatch"
                }
            }

            // verify from gh

            const gh_url = new URL("https://github.com/login/oauth/access_token")
            gh_url.searchParams.set("client_id", PUBLIC_GITHUB_CLIENT_ID)
            gh_url.searchParams.set("client_secret", GITHUB_CLIENT_SECRET)
            gh_url.searchParams.set("code", oData.code as string)



            const gh_res = await fetch(gh_url, {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                }
            }).then(res => res.json()).catch(err => {
                console.log("gh_err", err)
                return null
            })

            console.log("gh_res", gh_res)
            if (!gh_res || !gh_res.access_token) {
                return {
                    success: false,
                    message: "Failed to get access token"
                }
            }
            cookies.set("gh_token", gh_res.access_token, { path: "/", sameSite: 'lax' }) // save token, temporary

            // get users info and save it
            const gh_user = await fetch("https://api.github.com/user", {
                headers: {
                    'Accept': 'application/json',
                    'Authorization': `Bearer ${gh_res.access_token}`
                }
            }).then(res => res.json()).catch(err => { console.log("gh_user_err", err); return null })

            if (!gh_user) {
                return {
                    success: false,
                    message: "Failed to get user info"
                }
            }

            console.log("seting cookies", gh_user)

            cookies.set("gh_user", JSON.stringify(gh_user), { path: "/", sameSite: 'lax' }) // save user info, temporary

            // TODO: Check with providers data from engine
            // TODO: send validated data to engine for token generation
            // TODO: if user is logged in, link accounts
            // // Send data to engine
            // TODO: save token in cookies
            // TODO: remove unncecessary console.logs

            cookies.set(AUTH_COOKIE_NAME, "some_data", {
                path: "/",
                expires: new Date(Date.now() + 1000 * 60 * 60 * 24 * 7) // 7 days
            })

            return {
                success: true,
                message: "Success",
                user: gh_user
            }
        case 'google':
            console.log('oauth-callback google');
            break;
        default:
            return {
                success: false,
                message: 'OAuth provider not supported'
            };
    }


};