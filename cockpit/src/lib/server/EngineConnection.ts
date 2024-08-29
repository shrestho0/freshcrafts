import { BackendEndpoints } from '@/backend-endpoints';
import type {
	CommonPagination,
	EngineCommonResponseDto,
	EngineMongoDBGetOneError,
	EngineMySQLGetOneError,
	EnginePaginatedDto,
	EnginePostgreSQLGetOneError,
	EngineSystemConfigResponseDto
} from '@/types/dtos';
import type { AIChatHistory, AIChatMessage, DBMongo, DBMysql, DBPostgres, Project, ProjectDeployment, SystemwideNotification } from '@/types/entities';
import { AuthProviderType, DBMongoStatus, DBMysqlStatus } from '@/types/enums';
import messages from '@/utils/messages';

/**
 * EngineConnection
 * @description functions per service should be moved to its own class [Not sure]
 */
export class EngineConnection {


	private static _instance: EngineConnection;
	public static getInstance(): EngineConnection {
		if (!this._instance) {
			this._instance = new EngineConnection();
		}
		return this._instance;
	}

	private constructor() { }

	async customFetch<T = EngineCommonResponseDto>(url: string, init?: RequestInit): Promise<T> {
		init = init || {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			},
			signal: AbortSignal.timeout(3000)
		};

		if (!init?.headers) {
			init.headers = {
				'Content-Type': 'application/json'
			}
		}

		return await fetch(url, init).then((res) => res.json()).catch((err) => {
			// console.log(err)
			return {
				success: false,
				message: messages.RESPONSE_ERROR
			};
		});


	}
	async getProviders(): Promise<{
		success: boolean;
		providers: AuthProviderType[];
		message: string;
	}> {
		const res = await fetch(BackendEndpoints.PROVIDERS, {
			headers: {
				'Content-Type': 'application/json'
			},
			signal: AbortSignal.timeout(1000)
		})
			.then((res) => res.json())
			.catch((err) => {
				console.log(err)
				return {
					success: false,
					message: messages.RESPONSE_ERROR,
					providers: []
				};
			});

		if (res.data) {
			res.providers = res.data as {
				success: boolean;
				providers: AuthProviderType[];
				message: string;
			};
			delete res.data;
		}
		// console.log("RESRESRES", res)
		return res;
	}

	async getSystemConfig(): Promise<EngineSystemConfigResponseDto | null> {
		const res = await this.customFetch<EngineCommonResponseDto<EngineSystemConfigResponseDto>>(BackendEndpoints.SETUP_SYSCONFIG)
		if (res.success) {
			return res.payload;
		}
		return null;
	}

	//  async updateSystemConfigPartial(
	// 	new_sysconf: Partial<EngineSystemConfigResponseDto>
	// ): Promise<{ success: boolean; message: string; data: EngineSystemConfigResponseDto }> {
	// 	return (await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
	// 		method: 'PATCH',
	// 		headers: {
	// 			'Content-Type': 'application/json'
	// 		},
	// 		body: JSON.stringify(new_sysconf)
	// 	})
	// 		.then((res) => res.json())
	// 		.catch((err) => {
	// 			return { success: false, message: messages.RESPONSE_ERROR };
	// 		})) as unknown as { success: boolean; message: string; data: EngineSystemConfigResponseDto };

	// }

	async updateSystemConfigPartial(new_sysconf: Partial<EngineSystemConfigResponseDto>) {
		return this.customFetch(BackendEndpoints.SETUP_SYSCONFIG, {
			method: 'PATCH',
			body: JSON.stringify(new_sysconf)
		})
	}

	async generateToken(
		provider: AuthProviderType,
		data: {
			email?: string;
			password?: string;
			googleEmail?: string;
			githubId?: string;
		}
	) {
		switch (provider) {
			case AuthProviderType.EMAIL_PASSWORD:
				return await this.customFetch(BackendEndpoints.GENERATE_TOKEN, {
					method: 'POST',
					body: JSON.stringify({
						provider,
						email: data.email,
						password: data.password
					})
				})
				break;
			default:
				console.log('Requested Generate Token', provider, data);
				break;
		}
	}

	async refreshToken(refreshToken: string, provider: AuthProviderType) {
		return this.customFetch(BackendEndpoints.REFRESH_TOKEN, {
			method: 'POST',
			body: JSON.stringify({
				refreshToken,
				provider
			})
		});
	}

	async changePassword({
		oldPassword,
		newPassword
	}: {
		oldPassword: string;
		newPassword: string;
	}) {
		return await this.customFetch(BackendEndpoints.CHANGE_PASSWORD, {
			method: 'POST',
			body: JSON.stringify({
				oldPassword,
				newPassword
			})
		})
	}

	// console.log("RESRESRES", res)
	async removeOAuthProvider(OAUTH_GOOGLE: AuthProviderType) {
		return this.customFetch(BackendEndpoints.REMOVE_OAUTH_PROVIDER.replace(':provider', OAUTH_GOOGLE), {
			method: 'DELETE'
		})
	}


	////////////////////////////////////////// DB MYSQL //////////////////////////////////////////

	async getMysqlDBs({
		page = 1,
		pageSize = 10,
		orderBy = 'id',
		sort = 'DESC'
	}: CommonPagination): Promise<EnginePaginatedDto<DBMysql>> {
		console.log(page, pageSize, orderBy, sort);

		const url = new URL(BackendEndpoints.MYSQL_FIND_ALL);
		url.searchParams.append('page', page.toString());
		url.searchParams.append('pageSize', pageSize.toString());
		url.searchParams.append('orderBy', orderBy);
		url.searchParams.append('sort', sort);

		return this.customFetch<EnginePaginatedDto<DBMysql>>(url.toString());
	}

	async searchMysqlDBs(query: string) {
		return this.customFetch(BackendEndpoints.MYSQL_SEARCH.replace(':query', query));
	}


	async getMysqlDB(
		db_id: string
	): Promise<EngineCommonResponseDto<DBMysql, EngineMySQLGetOneError>> {
		return this.customFetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', db_id));
	}

	async deleteMysqlDB(
		db_id: string
	): Promise<EngineCommonResponseDto<null, EngineMySQLGetOneError>> {
		return this.customFetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', db_id), {
			method: 'DELETE',
		})
	}

	async updateMysqlDB(
		dbModelId: string,
		newDBName: string,
		newDBUser: string,
		newUserPassword: string
	): Promise<EngineCommonResponseDto<Partial<DBMysql>, null>> {
		console.warn(
			'[DEBUG]: databases/mysql/[db_id] update',
			JSON.stringify({ dbModelId, newDBName, newDBUser, newUserPassword }, null, 2)
		);

		if (!newDBName && !newDBUser && !newUserPassword) {
			return {
				success: false,
				message: 'Atleast one field must be filled to update'
			} as EngineCommonResponseDto<Partial<DBMysql>, null>;
		}

		// validate invalid db names here
		// validate invalid db users here
		// validate invalid user passwords
		const xObj: any = {};
		if (newDBName) xObj['newDBName'] = newDBName;
		if (newDBUser) xObj['newDBUser'] = newDBUser;
		if (newUserPassword) xObj['newUserPassword'] = newUserPassword;

		return this.customFetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', dbModelId), {
			method: 'PATCH',
			body: JSON.stringify(xObj)
		})
	}

	async revertChangesMysql(dbModelId: string): Promise<EngineCommonResponseDto<DBMysql, null>> {
		// joratali revert changes
		return this.customFetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', dbModelId) + '/revert', {
			method: 'PATCH',
			body: JSON.stringify({ status: DBMysqlStatus.OK })
		})
	}




	////////////////////////////////////////// DB POSTGRES //////////////////////////////////////////

	async getPostgresDBs({
		page = 1,
		pageSize = 10,
		orderBy = 'id',
		sort = 'DESC'
	}: CommonPagination): Promise<EnginePaginatedDto<DBPostgres>> {
		console.log(page, pageSize, orderBy, sort);

		const url = new URL(BackendEndpoints.POSTGRES_FIND_ALL);
		url.searchParams.append('page', page.toString());
		url.searchParams.append('pageSize', pageSize.toString());
		url.searchParams.append('orderBy', orderBy);
		url.searchParams.append('sort', sort);

		return this.customFetch(url.toString());
	}

	async searchPostgresDBs(query: string) {
		return this.customFetch(BackendEndpoints.POSTGRES_SEARCH.replace(':query', query))
	}

	async getPostgresDB(
		db_id: string
	): Promise<EngineCommonResponseDto<DBPostgres, EnginePostgreSQLGetOneError>> {
		return this.customFetch(BackendEndpoints.POSTGRES_BY_ID.replace(':id', db_id))
	}

	async deletePostgresDB(
		db_id: string
	): Promise<EngineCommonResponseDto<null, EnginePostgreSQLGetOneError>> {
		return this.customFetch(BackendEndpoints.POSTGRES_BY_ID.replace(':id', db_id), {
			method: 'DELETE'
		})
	}

	async updatePostgresDB(
		dbModelId: string,
		newDBName: string,
		newDBUser: string,
		newUserPassword: string
	): Promise<EngineCommonResponseDto<Partial<DBPostgres>, null>> {
		console.warn(
			'[DEBUG]: databases/postgres/[db_id] update',
			JSON.stringify({ dbModelId, newDBName, newDBUser, newUserPassword }, null, 2)
		);

		if (!newDBName && !newDBUser && !newUserPassword) {
			return {
				success: false,
				message: 'Atleast one field must be filled to update'
			} as EngineCommonResponseDto<Partial<DBPostgres>, null>;
		}

		// validate invalid db names here
		// validate invalid db users here
		// validate invalid user passwords
		const xObj: any = {};
		if (newDBName) xObj['newDBName'] = newDBName;
		if (newDBUser) xObj['newDBUser'] = newDBUser;
		if (newUserPassword) xObj['newUserPassword'] = newUserPassword;

		return this.customFetch(BackendEndpoints.POSTGRES_BY_ID.replace(':id', dbModelId), {
			method: 'PATCH',
			body: JSON.stringify(xObj)
		})
	}

	async revertChangesPostgres(dbModelId: string): Promise<EngineCommonResponseDto<DBPostgres, null>> {
		// joratali revert changes
		return this.customFetch(BackendEndpoints.POSTGRES_BY_ID.replace(':id', dbModelId) + '/revert', {
			method: 'PATCH',
			body: JSON.stringify({ status: DBMysqlStatus.OK })
		})
	}

	////////////////////////////////////////// DB MONGO //////////////////////////////////////////

	async getMongoDBs({
		page = 1,
		pageSize = 10,
		orderBy = 'id',
		sort = 'DESC'
	}: CommonPagination): Promise<EnginePaginatedDto<DBMongo>> {
		console.log(page, pageSize, orderBy, sort);

		const url = new URL(BackendEndpoints.MONGO_FIND_ALL);
		url.searchParams.append('page', page.toString());
		url.searchParams.append('pageSize', pageSize.toString());
		url.searchParams.append('orderBy', orderBy);
		url.searchParams.append('sort', sort);

		return this.customFetch(url.toString());
	}

	async searchMongoDBs(query: string) {
		return this.customFetch(BackendEndpoints.MONGO_SEARCH.replace(':query', query));
	}

	async getMongoDB(
		db_id: string
	): Promise<EngineCommonResponseDto<DBMongo, EngineMongoDBGetOneError>> {
		return this.customFetch(BackendEndpoints.MONGO_BY_ID.replace(':id', db_id));
	}

	async deleteMongoDB(
		db_id: string
	): Promise<EngineCommonResponseDto<null, EngineMongoDBGetOneError>> {
		return this.customFetch(BackendEndpoints.MONGO_BY_ID.replace(':id', db_id), {
			method: 'DELETE',
		});
	}

	async updateMongoDB(
		dbModelId: string,
		newDBName: string,
		newDBUser: string,
		newUserPassword: string
	): Promise<EngineCommonResponseDto<Partial<DBMongo>, null>> {
		console.warn(
			'[DEBUG]: databases/mongodb/[db_id] update',
			JSON.stringify({ dbModelId, newDBName, newDBUser, newUserPassword }, null, 2)
		);

		if (!newDBName && !newDBUser && !newUserPassword) {
			return {
				success: false,
				message: 'Atleast one field must be filled to update'
			} as EngineCommonResponseDto<Partial<DBMongo>, null>;
		}

		// validate invalid db names here
		// validate invalid db users here
		// validate invalid user passwords
		const xObj: any = {};
		if (newDBName) xObj['newDBName'] = newDBName;
		if (newDBUser) xObj['newDBUser'] = newDBUser;
		if (newUserPassword) xObj['newUserPassword'] = newUserPassword;

		return this.customFetch(BackendEndpoints.MONGO_BY_ID.replace(':id', dbModelId), {
			method: 'PATCH',
			body: JSON.stringify(xObj)
		});
	}

	async revertChangesMongo(dbModelId: string): Promise<EngineCommonResponseDto<DBMongo, null>> {
		// joratali revert changes
		return this.customFetch(BackendEndpoints.MONGO_BY_ID.replace(':id', dbModelId) + '/revert', {
			method: 'PATCH',
			body: JSON.stringify({ status: DBMongoStatus.OK })
		});
	}

	////////////////////////////////////////// NOTIFICATIONS //////////////////////////////////////////


	async getNotificaions({
		page = 1,
		limit = 1,
		order = 'id',
		sort = 'desc'
	}: {
		page: number;
		limit: number;
		order: 'id' | any;
		sort: 'desc' | 'asc';
	}) {
		console.log({ page, limit, order, sort });
		return {
			page: 1,
			limit: 5,
			orderBy: '_id',
			sortBy: 'desc',
			data: [{}]
		};
	}

	async getPaginatedNotifications({
		page = 1,
		pageSize = 10,
		orderBy = 'id',
		sort = 'DESC'
	}: CommonPagination): Promise<EnginePaginatedDto<SystemwideNotification>> {
		console.log(page, pageSize, orderBy, sort);

		const url = new URL(BackendEndpoints.NOTIFICATIONS);
		url.searchParams.append('page', page.toString());
		url.searchParams.append('pageSize', pageSize.toString());
		url.searchParams.append('orderBy', orderBy);
		url.searchParams.append('sort', sort);

		return this.customFetch<EnginePaginatedDto<SystemwideNotification>>(url.toString());
	}

	async deleteNofitication(id: string) {
		return this.customFetch(BackendEndpoints.NOTIFICATION_BY_ID.replace(':id', id), {
			method: 'DELETE',
			body: JSON.stringify({ id })
		})

	}



	////////////////////////////////////////// PROJECT STUFF //////////////////////////////////////////

	async initProject(data: any) {
		return this.customFetch(BackendEndpoints.PROJECTS_INIT, {
			method: 'POST',
			body: JSON.stringify(data)
		});
	}

	async getProject<T>(id: string) {
		return this.customFetch<T>(BackendEndpoints.PROJECT_BY_ID.replace(':id', id));
	}

	async getProjectByUniqueName(uniqueName: string): Promise<EngineCommonResponseDto<Project, null>> {
		return this.customFetch(BackendEndpoints.PROJECT_BY_UNIQUE_NAME.replace(':id', uniqueName));
	}


	async deployProject(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_DEPLOY_BY_ID.replace(':id', id), {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}

	async updatePartialProjectDeployment(id: string, data: Partial<ProjectDeployment>) {
		return this.customFetch(BackendEndpoints.PROJECT_DEPLOYMENT_BY_ID.replace(':id', id), {
			method: 'PATCH',
			body: JSON.stringify(data)
		})
	}

	async deleteIncompleteProject(id: string) {
		return this.customFetch(BackendEndpoints.PROJECT_INCOMPLETE_BY_ID.replace('{id}', id), {
			method: 'DELETE',
		})
	}

	////////////////////////////////////////// AI & CHAT STUFF //////////////////////////////////////////
	async getChatApiKeys() {
		return this.customFetch(BackendEndpoints.API_CHAT_APIKEY);
	}

	async setChatApiKey(data: {
		azureChatApiEndpoint: string,
		azureChatApiKey: string
	}) {
		console.log(data)
		return this.customFetch(BackendEndpoints.API_CHAT_APIKEY, {
			method: 'POST',
			body: JSON.stringify(data)
		})

	}
	async saveAIChatMessages(data: {
		chatName: string,
		messages: AIChatMessage[]
	}) {
		return this.customFetch(BackendEndpoints.API_CHAT_HISTORY, {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}

	async getAIChatHistory(data: {
		page: number,
		pageSize: number,
		sort: 'ASC' | 'DESC',
	}) {
		const url = new URL(BackendEndpoints.API_CHAT_HISTORY);
		url.searchParams.append('page', data.page.toString());
		url.searchParams.append('pageSize', data.pageSize.toString());
		url.searchParams.append('sort', data.sort);
		return this.customFetch<EnginePaginatedDto<AIChatHistory>>(url.toString());
	}


	async getProjectsPaginated(data: {
		page: number,
		pageSize: number,
		sort: 'ASC' | 'DESC',
	}) {
		const url = new URL(BackendEndpoints.PROJECTS_ALL);
		url.searchParams.append('page', data.page.toString());
		url.searchParams.append('pageSize', data.pageSize.toString());
		url.searchParams.append('sort', data.sort);
		return this.customFetch<EnginePaginatedDto<Project>>(url.toString());
	}


	async deleteAIChatHistory(id: string) {
		return this.customFetch(BackendEndpoints.API_CHAT_HISTORY_BY_ID.replace(':id', id), {
			method: 'DELETE',
			body: JSON.stringify({ id })
		})
	}

}	
