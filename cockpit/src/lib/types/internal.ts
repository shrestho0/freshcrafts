/**
 * Internal types used by this project
 */

import type { GoogleOAuthLoginCallbackDto } from './dtos';
import type { ProjectDeployment } from './entities';
import type { AuthProviderType } from './enums';

export type SetupPageOauthData = {
	githubLoginUrl: string;
	googleLoginUrl: string;
	githubStatus: 'loading' | 'connected' | 'idle' | 'error';
	googleStatus: 'loading' | 'connected' | 'idle' | 'error';
	githubOAuthJson: string | null;
	googleOAuthJson: string | null;

	oAuthGoogleEmail?: string;
	oAuthGithubId?: string;
};

export type SetupPageAccountData = {
	name: string;
	email: string;
	password: string;
};

export type OAuthGoogleDataRequestDto = {
	token?: string;
	userInfo?: OAuthGoogleIdTokenData;
	dataJson?: GoogleOAuthLoginCallbackDto;
	emails: string[];
};
export type OAuthGithubDataRequestDto = {
	token?: string;
	userInfo?: any;
	dataJson?: any;
	// egula thakbe
	oAuthGithubId?: string;
	oAuthGoogleEmail?: string;

	emails: string[];
};

export type OAuthCallbackInternalResponse = {
	redirect: string;
	success: boolean;
	provider: string;
	closeWindow: boolean;
	message: string;
	data: OAuthGoogleDataRequestDto;
};

export type OAuthGoogleIdTokenData = {
	iss: string;
	azp: string;
	aud: string;
	sub: string;
	email: string;
	email_verified: boolean;
	at_hash: string;
	name: string;
	picture: string;
	given_name: string;
	family_name: string;
	iat: number;
	exp: number;
};

export type CustomJwtPayload = {
	iss: string;
	systemUserName: string;
	systemUserEmail: string;
	provider: AuthProviderType;
	exp: number;
	sub: 'ACCESS_TOKEN' | 'REFRESH_TOKEN';
};

export type InternalDeployProjectData = {
	projectName: string,
	buildCommand: string,
	installCommand: string,
	outputDir: string,
	selectedFileRelativeUrl: string,
	envFileContent: string,
	deployment: ProjectDeployment,
	projectId: string,
	postInstall: string,
	domain: string,
	ssl: boolean,
	version: number,
}