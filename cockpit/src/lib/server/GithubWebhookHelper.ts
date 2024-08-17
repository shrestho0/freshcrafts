import { Octokit } from "octokit";
import jwt from 'jsonwebtoken';
import { GITHUB_CLIENT_ID, GITHUB_WEBHOOK_PEM_KEY } from "$env/static/private";

export class GithubWebhookHelper {
    private static instance: GithubWebhookHelper;
    private constructor() { }
    public static getInstance(): GithubWebhookHelper {
        if (!GithubWebhookHelper.instance) {
            GithubWebhookHelper.instance = new GithubWebhookHelper();
        }
        return GithubWebhookHelper.instance;
    }

    public getOctokitInstance(token: string | null = null): Octokit | null {
        try {
            return new Octokit({ auth: token || this.generateJwt() });
        } catch (e) {
            console.error(e);
            return null;
        }
    }

    public generateJwt() {

        const payload = {
            iat: Math.floor(Date.now() / 1000),
            exp: Math.floor(Date.now() / 1000) + 60,
            iss: GITHUB_CLIENT_ID,
        }
        const jwt_token = jwt.sign(payload, GITHUB_WEBHOOK_PEM_KEY, { algorithm: 'RS256' });
        return jwt_token;
    }
}