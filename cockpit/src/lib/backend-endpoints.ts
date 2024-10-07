import { ENGINE_BASE_URL } from '$env/static/private';

// maybe map will be more efficient
const _endpoints = {

	PING: '/ping',
	WATCHDOG_SERVICE_STATUS: '/watchdog-service-status',

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

	REDIS_NEW: '/db-redis',
	REDIS_SEARCH: '/db-redis/search/:query',
	REDIS_BY_ID: '/db-redis/:id',
	REDIS_FIND_ALL: '/db-redis',

	NOTIFICATIONS: '/notifications',
	NOTIFICATION_BY_ID: '/notifications/:id',

	PROJECTS_ALL: '/projects',
	PROJECTS_INIT: '/projects/init',

	PROJECT_DEPLOY_BY_ID: '/projects/deploy/:id', // POST to deploy
	PROJECT_REDEPLOY_BY_ID: '/projects/redeploy/:id', // POST to re-deploy
	PROJECT_UPDATE_BY_ID: '/projects/update/:id', // PATCH to update
	PROJECT_ROLLBACK_BY_ID: '/projects/rollback/:id', // POST to rollback
	PROJECT_ROLLFORWARD_BY_ID: '/projects/rollforward/:id', // POST to rollforward

	PROJECT_BY_ID: '/projects/:id',
	PROJECT_BY_UNIQUE_NAME: '/projects/:id?isIdUniqueName=true',
	PROJECT_INCOMPLETE_BY_ID: '/projects/incomplete/:id',
	DEPLOYMENT_BY_ID: "/projects/deployments/:id", // Get to get, PATCH to partial update

	PROJECT_DEPLOYMENTS_BY_ID: '/projects/:id/deployments', // Get to get all deps, Post to create new dep

	API_CHAT_APIKEY: "/ai/chat/api-keys", // Get to get, Post to set
	API_CHAT_HISTORY: "/ai/chat/history", // Get to get, Post to send, ?page=0&pageSize=1&sort=DESC
	API_CHAT_HISTORY_BY_ID: "/ai/chat/history/:id", // 

	AI_CODE_DOC: "/ai/code-doc/:id", // Get to Post to create Patch to update,
	AI_CODE_DOC_BY_PROJECT: "/ai/code-doc/by-project/:id", // Get to get all, Post to create

	DASHBOARD_DBS: "/dashboard/dbs",
	DAShBOARD_DEPS: "/dashboard/deployments",


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
