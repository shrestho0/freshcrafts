/**
 * Response Object DTOs
 */
export type EngineSystemConfigResponseDto = {
	systemUserSetupComplete: boolean;
	systemUserName: string;
	systemUserEmail: string;
	systemUserPasswordHash: string;
	systemUserOauthGoogleEnabled: boolean;
	systemUserOauthGithubEnabled: boolean;
	systemUserOauthGoogleData: Object | null;
	systemUserOauthGithubData: Object | null;
	systemUserOAuthGithubId?: string | null;
	systemUserOAuthGoogleEmail?: string | null;
	created: null;
	updated: Date;
};

export type GoogleOAuthLoginCallbackDto = {
	access_token: string;
	refresh_token: string;
	scope: string;
	token_type: string;
	id_token: string;
	expiry_date: number;
};

export type EngineCommonResponseDto<P = any, E = any> = {
	success: boolean;
	message: string | null;
	/**
	 * @deprecated
	 */
	data: any;
	payload: P;
	errors: E;
};

/**
 * Payloads
 */

// Get One MySQL Database Payload
/**
 * @deprecated
 * Check types/entities/DBMysql
 */
export type EngineMySQLGetOnePayload = {
	id: string;
	dbName: string;
	dbUser: string;
	dbPassword: string;
	status: null;
	reasonFailed: null;
};

export type EngineMySQLGetOneError = {};

// Create MySQL Database Payload
export type EngineMySQLCreatePayload = EngineMySQLGetOnePayload;
export type EngineMySQLCreateError = {
	dbName: string;
	dbUser: string;
	dbPassword: string;
};
export type EngineMySQLUpdateError = {
	newDBName: string;
	newDBUser: string;
	newUserPassword: string;
};
// To parse this data:
//
//   import { Convert, Pagable } from "./file";
//
//   const pagable = Convert.toPagable(json);

export type EnginePaginatedDto<T> = {
	totalElements: number;
	totalPages: number;
	pageable: Pageable;
	size: number;
	content: T[];
	number: number;
	sort: Sort;
	first: boolean;
	last: boolean;
	numberOfElements: number;
	empty: boolean;
};

export type Content = {
	id: string;
	dbName: string;
	dbUser: string;
	dbPassword: string;
	status: string;
	reasonFailed: null;
};

export type Pageable = {
	pageNumber: number;
	pageSize: number;
	sort: Sort;
	offset: number;
	paged: boolean;
	unpaged: boolean;
};

export type CommonPagination = {
	page: number | undefined;
	pageSize: number | undefined;
	orderBy: string | undefined;
	sort: string | undefined;
};

export type CommonPaginationKey = keyof CommonPagination;

export type Sort = {
	sorted: boolean;
	empty: boolean;
	unsorted: boolean;
};
