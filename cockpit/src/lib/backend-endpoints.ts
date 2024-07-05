import { ENGINE_BASE_URL } from "$env/static/private";

const _endpoints = {
    PROVIDERS: '/tokens/allowed-providers',
    GENERATE: '/tokens/generate',
    REFRESH: '/tokens/refresh',

    SETUP_SYSCONFIG: '/config/sysconf',
}

type BackendEndpointsType = typeof _endpoints;
type BackendEndpointsKeys = keyof BackendEndpointsType;

export const BackendEndpoints = new Proxy(_endpoints, {
    get: (target: BackendEndpointsType, prop: BackendEndpointsKeys) => {
        if (!ENGINE_BASE_URL) throw new Error("Backend Host not set");
        if (!target[prop]) throw new Error("Endpoint not found");
        return ENGINE_BASE_URL + target[prop];
    },
    set: () => {
        throw new Error("BackendApiEndpoints is a read-only object");
    }
})