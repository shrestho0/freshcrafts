import { ENGINE_BASE_URL } from '$env/static/private';

// maybe map will be more efficient
const _endpoints = {
	PROVIDERS: '/tokens/allowed-providers',

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

	PROJECTS_NEW: '/projects',
	PROJECT_BY_ID: '/projects/:id',
	PROJECT_BY_UNIQUE_NAME: '/projects/:id?isIdUniqueName=true',

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
