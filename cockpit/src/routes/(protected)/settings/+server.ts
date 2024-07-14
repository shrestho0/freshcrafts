import { BackendEndpoints } from "@/backend-endpoints";
import { EngineConnection } from "@/server/EngineConnection";
import { AuthProviderType } from "@/types/enums";
import { json, type RequestHandler } from "@sveltejs/kit";


export const PATCH: RequestHandler = async ({ request }) => {
    const {
        update_type = "oauth",
        provider,
        githubId,
        googleEmail
    }: {
        update_type: "oatuh" | any
        provider: AuthProviderType;
        githubId?: string,
        googleEmail?: string
    } = await request.json();

    let res;
    if (githubId) {
        res = await EngineConnection.getInstance().updateSystemConfigPartial({
            systemUserOauthGithubEnabled: true,
            systemUserOAuthGithubId: githubId,
        } as any)
    } else if (googleEmail) {
        res = await EngineConnection.getInstance().updateSystemConfigPartial({
            systemUserOauthGoogleEnabled: true,
            systemUserOAuthGoogleEmail: googleEmail,
        })
    } else {
        res = {
            success: false,
            message: "Invalid Request"
        }
    }
    return json(res)
};

