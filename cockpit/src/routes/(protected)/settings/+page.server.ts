import { EngineConnection } from "@/server/EngineConnection";
import type { PageServerLoad } from "./$types";
import { OAUTH_STATE_COOKIE_NAME } from "$env/static/private";
import { ulid } from "ulid";
import { getGithubLoginUrl, getGooleLoginhUrl } from "@/server/helpers/OAuthHelper";

export const load: PageServerLoad = async ({ locals, cookies, url }) => {
    const engine = EngineConnection.getInstance()

    let oAuthState = cookies.get(OAUTH_STATE_COOKIE_NAME);

    if (!oAuthState) {
        oAuthState = ulid();
        cookies.set(OAUTH_STATE_COOKIE_NAME, oAuthState, { path: '/', maxAge: 60 * 60 * 24 * 3 });
    }

    return {
        githubLoginUrl: getGithubLoginUrl(url.origin, oAuthState),
        googleLoginUrl: getGooleLoginhUrl(url.origin, oAuthState),
        providers: await engine.getProviders(),
        systemConfig: await engine.getSystemConfig()
    }
};