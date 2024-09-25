import { BackendEndpoints } from '@/backend-endpoints';
import type {
	CommonPagination,
	EngineCommonResponseDto,
	EngineMongoDBGetOneError,
	EngineMySQLGetOneError,
	EnginePaginatedDto,
	EnginePostgreSQLGetOneError,
	EngineRedisDBGetOneError,
	EngineSystemConfigResponseDto
} from '@/types/dtos';
import type { AIChatHistory, AIChatMessage, DBMongo, DBMysql, DBPostgres, DBRedis, Project, ProjectDeployment, SystemwideNotification } from '@/types/entities';
import { AuthProviderType, DBMongoStatus, DBMysqlStatus, DBRedisStatus } from '@/types/enums';
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
	async getProviders(): Promise<EngineCommonResponseDto<AuthProviderType[], null>> {
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
			}) as EngineCommonResponseDto<AuthProviderType[], null>

		// if (res.payload) {
		// 	res.providers = res.payload as {
		// 		success: boolean;
		// 		providers: AuthProviderType[];
		// 		message: string;
		// 	};
		// 	delete res.payload;
		// }
		// // console.log("RESRESRES", res)
		// return res;
		// return {
		// success: boolean;
		// }
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

	async generateEngineTokens(email: any, fetch: any) {
		const res = await fetch(BackendEndpoints.GENERATE_TOKEN, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				provider: AuthProviderType.OAUTH_GOOGLE,
				googleEmail: email
			})
		})
			.then((res: { json: () => any; }) => res.json())
			.catch(() => {
				return {
					success: false,
					message: 'Failed to communicate with `engine`'
				};
			});

		return res;
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


	////////////////////////////////////////// DB REDIS //////////////////////////////////////////

	async getRedisDBs({
		page = 1,
		pageSize = 10,
		orderBy = 'id',
		sort = 'DESC'
	}: CommonPagination): Promise<EnginePaginatedDto<DBRedis>> {
		console.log(page, pageSize, orderBy, sort);

		const url = new URL(BackendEndpoints.REDIS_FIND_ALL);
		url.searchParams.append('page', page.toString());
		url.searchParams.append('pageSize', pageSize.toString());
		url.searchParams.append('orderBy', orderBy);
		url.searchParams.append('sort', sort);

		return this.customFetch(url.toString());
	}

	async searchRedisDBs(query: string) {
		return this.customFetch(BackendEndpoints.REDIS_SEARCH.replace(':query', query));
	}

	async getRedisDB(
		db_id: string
	): Promise<EngineCommonResponseDto<DBRedis, EngineRedisDBGetOneError>> {
		return this.customFetch(BackendEndpoints.REDIS_BY_ID.replace(':id', db_id));
	}

	async deleteRedisDB(
		db_id: string
	): Promise<EngineCommonResponseDto<null, EngineRedisDBGetOneError>> {
		return this.customFetch(BackendEndpoints.REDIS_BY_ID.replace(':id', db_id), {
			method: 'DELETE',
		});
	}

	async updateRedisDB(
		dbModelId: string,
		password: string
		// newDBName: string,
		// newDBUser: string,
		// newUserPassword: string
	): Promise<EngineCommonResponseDto<Partial<DBRedis>, null>> {
		console.warn(
			'[DEBUG]: databases/redis/[db_id] update',
			JSON.stringify({ dbModelId, password }, null, 2)
		);

		// if (!newDBName && !newDBUser && !newUserPassword) {
		// 	return {
		// 		success: false,
		// 		message: 'Atleast one field must be filled to update'
		// 	} as EngineCommonResponseDto<Partial<DBMongo>, null>;
		// }

		// validate invalid db names here
		// validate invalid db users here
		// validate invalid user passwords

		return this.customFetch(BackendEndpoints.REDIS_BY_ID.replace(':id', dbModelId), {
			method: 'PATCH',
			body: JSON.stringify({ password })
		});
	}

	async revertChangesRedis(dbModelId: string): Promise<EngineCommonResponseDto<DBRedis, null>> {
		// joratali revert changes
		return this.customFetch(BackendEndpoints.REDIS_BY_ID.replace(':id', dbModelId) + '/revert', {
			method: 'PATCH',
			body: JSON.stringify({ status: DBRedisStatus.OK })
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

	async getProject(id: string): Promise<{
		success: boolean,
		statusCode: number,
		message?: string,

		project?: Project
		activeDeployment?: ProjectDeployment
		currentDeployment?: ProjectDeployment

	}> {
		const res = await this.customFetch<EngineCommonResponseDto<Project, null, ProjectDeployment | null, ProjectDeployment | null>>(BackendEndpoints.PROJECT_BY_ID.replace(':id', id));
		return {
			success: res.success,
			statusCode: res.statusCode,
			message: res.message ?? undefined,
			project: res.payload ?? undefined,
			activeDeployment: res.payload2 ?? undefined,
			currentDeployment: res.payload3 ?? undefined
		}
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

	async reDeployProject(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_REDEPLOY_BY_ID.replace(':id', id), {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}

	async updateProject(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_UPDATE_BY_ID.replace(':id', id), {
			method: 'PATCH',
			body: JSON.stringify(data)
		})
	}

	async rollbackProject(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_ROLLBACK_BY_ID.replace(':id', id), {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}



	async rollforwardProjectPreProcessing(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_ROLLFORWARD_BY_ID.replace(':id', id) + "?preprocessing=true", {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}

	async rollforwardProjectRequest(id: string, data: any) {
		return this.customFetch(BackendEndpoints.PROJECT_ROLLFORWARD_BY_ID.replace(':id', id), {
			method: 'POST',
			body: JSON.stringify(data)
		})
	}

	async updatePartialDeployment(id: string, data: Partial<ProjectDeployment>) {
		return this.customFetch(BackendEndpoints.DEPLOYMENT_BY_ID.replace(':id', id), {
			method: 'PATCH',
			body: JSON.stringify(data)
		})
	}

	async getAllDeployments(id: string) {
		return this.customFetch(BackendEndpoints.PROJECT_DEPLOYMENTS_BY_ID.replace(':id', id))
	}

	// FIXME: we may need this in future, but not now
	// async createNewDeploymentForProject(id: string, data: Partial<ProjectDeployment>) {
	// 	return this.customFetch(BackendEndpoints.PROJECT_DEPLOYMENTS_BY_ID.replace(':id', id), {
	// 		method: 'POST',
	// 		body: JSON.stringify(data)
	// 	})
	// }

	async deleteIncompleteProject(id: string) {
		return this.customFetch(BackendEndpoints.PROJECT_INCOMPLETE_BY_ID.replace(':id', id), {
			method: 'DELETE',
		})
	}

	async deleteProject(id: string) {
		return this.customFetch(BackendEndpoints.PROJECT_BY_ID.replace(":id", id), {
			method: 'DELETE'
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


	async getCodeDocForProject(projectId: string) {
		return this.customFetch(BackendEndpoints.AI_CODE_DOC_BY_PROJECT.replace(":id", projectId))
	}

	async createCodeDoc(d: Object) {
		return this.customFetch(BackendEndpoints.AI_CODE_DOC.replace("/:id", ''), {
			method: 'POST',
			body: JSON.stringify(d)
		})
	}

	async updateCodeDoc(docId: string, d: Object) {
		return this.customFetch(BackendEndpoints.AI_CODE_DOC.replace(":id", docId), {
			method: 'PATCH',
			body: JSON.stringify(d)
		})
	}
	async deleteCodeDoc(docId: string) {
		return this.customFetch(BackendEndpoints.AI_CODE_DOC.replace(":id", docId), {
			method: 'DELETE'
		})
	}


	////////////// System Specific //////////...
	async ping() {
		return this.customFetch(BackendEndpoints.PING)
	}
	async getServiceStatus() {
		return this.customFetch(BackendEndpoints.WATCHDOG_SERVICE_STATUS)
	}

}	
