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

	NOTIFICATIONS: '/notifications',

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
