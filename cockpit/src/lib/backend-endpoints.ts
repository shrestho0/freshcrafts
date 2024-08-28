import { ENGINE_BASE_URL } from '$env/static/private';

// maybe map will be more efficient
const _endpoints = {
	PROVIDERS: '/tokens/allowed-providers',
	REMOVE_OAUTH_PROVIDER: '/tokens/remove-oauth-provider/:provider',

	GENERATE_TOKEN: '/tokens/generate',
	REFRESH_TOKEN: '/tokens/refresh',
	INVALIDATE_TOKEN: '/tokens/invalidate',
	CHANGE_PASSWORD: '/tokens/change-password',

	SETUP_SYSCONFIG: '/config/sysconf',

	MYSQL_NEW: '/db-mysql',
	MYSQL_SEARCH: '/db-mysql/search/:query',
	MYSQL_BY_ID: '/db-mysql/:id',
	MYSQL_FIND_ALL: '/db-mysql',

	POSTGRES_NEW: '/db-postgres',
	POSTGRES_SEARCH: '/db-postgres/search/:query',
	POSTGRES_BY_ID: '/db-postgres/:id',
	POSTGRES_FIND_ALL: '/db-postgres',

	MONGO_NEW: '/db-mongo',
	MONGO_SEARCH: '/db-mongo/search/:query',
	MONGO_BY_ID: '/db-mongo/:id',
	MONGO_FIND_ALL: '/db-mongo',

	NOTIFICATIONS: '/notifications',
	NOTIFICATION_BY_ID: '/notifications/:id',

	PROJECTS_INIT: '/projects/init',
	PROJECT_DEPLOY_BY_ID: '/projects/deploy/:id', // POST to deploy
	PROJECT_BY_ID: '/projects/:id',
	PROJECT_BY_UNIQUE_NAME: '/projects/:id?isIdUniqueName=true',

	PROJECT_INCOMPLETE_BY_ID: '/projects/incomplete/{id}',

	PROJECT_DEPLOYMENT_BY_ID: "/projects/deployments/:id", // Get to get, PATCH to partial update

	API_CHAT_APIKEY: "/ai/chat/api-keys", // Get to get, Post to set
	API_CHAT_HISTORY: "/ai/chat/history", // Get to get, Post to send, ?page=0&pageSize=1&sort=DESC
	API_CHAT_HISTORY_BY_ID: "/ai/chat/history/:id", // 


};

type BackendEndpointsType = typeof _endpoints;
type BackendEndpointsKeys = keyof BackendEndpointsType;

export const BackendEndpoints = new Proxy(_endpoints, {
	get: (target: BackendEndpointsType, prop: BackendEndpointsKeys) => {
		if (!ENGINE_BASE_URL) throw new Error('Backend Host not set');
		if (!target[prop]) throw new Error('Endpoint not found');
		return ENGINE_BASE_URL + target[prop];
	},
	set: () => {
		throw new Error('BackendApiEndpoints is a read-only object');
	}
});
