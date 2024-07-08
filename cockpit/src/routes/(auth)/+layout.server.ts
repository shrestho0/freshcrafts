
import { getGithubLoginUrl, getGooleLoginhUrl } from "@/server/helpers/OAuthHelper";
import type { LayoutServerLoad } from "./$types";
import { ulid } from "ulid";
import { OAUTH_STATE_COOKIE_NAME } from "$env/static/private";

export const load: LayoutServerLoad = async ({ locals, url, cookies }) => {

    let oAuthState = cookies.get(OAUTH_STATE_COOKIE_NAME);

    if (!oAuthState) {
        oAuthState = ulid();
        cookies.set(OAUTH_STATE_COOKIE_NAME, oAuthState, { path: '/', maxAge: 60 * 60 * 24 * 3 });
    }

    return {
        githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
        googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState)
    }
};