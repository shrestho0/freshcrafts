import { PUBLIC_GITHUB_CLIENT_ID, PUBLIC_GITHUB_CLIENT_SECRET } from "$env/static/public";
import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ request, locals, cookies, url }) => {
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
    gh_url.searchParams.set("client_secret", PUBLIC_GITHUB_CLIENT_SECRET)
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
    cookies.set("gh_token", gh_res.access_token, { path: "/" }) // save token, temporary

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

    cookies.set("gh_user", JSON.stringify(gh_user), { path: "/" }) // save user info, temporary

    return {
        success: true,
        message: "Success",
        user: gh_user
    }


};