// TODO: forget, what this one is for
export class ProviderStore {
	private static instance: ProviderStore;

	private providers = [];

	static getInstance() {
		if (!ProviderStore.instance) {
			ProviderStore.instance = new ProviderStore();
		}
		return ProviderStore.instance;
	}

	async getProviders() {
		if (this.providers.length === 0) {
			await this.syncProvidersFromServer();
		}
		return this.providers;
	}

	async syncProvidersFromServer() {
		// fetch providers from engine

		return;
	}
}
