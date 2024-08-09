import { BackendEndpoints } from '@/backend-endpoints';
import type {
	CommonPagination,
	EngineCommonResponseDto,
	EngineMySQLGetOneError,
	EngineMySQLGetOnePayload,
	EnginePaginatedDto,
	EngineSystemConfigResponseDto
} from '@/types/dtos';
import type { DBMysql } from '@/types/entities';
import { AuthProviderType, DBMysqlStatus } from '@/types/enums';
import messages from '@/utils/messages';

/**
 * EngineConnection
 * @description functions per service should be moved to its own class [Not sure]
 */
export class EngineConnection {
	private static _instance: EngineConnection;
	static getInstance(): EngineConnection {
		if (!this._instance) {
			this._instance = new EngineConnection();
		}
		return this._instance;
	}

	private constructor() { }

	public async getProviders(): Promise<{
		success: boolean;
		providers: AuthProviderType[];
		message: string;
	}> {
		const res = await fetch(BackendEndpoints.PROVIDERS, {
			headers: {
				'Content-Type': 'application/json'
			},
			signal: AbortSignal.timeout(3000)
		})
			.then((res) => res.json())
			.catch(() => {
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
		return res;
	}

	public async getSystemConfig(): Promise<EngineSystemConfigResponseDto> {
		return await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then((res) => res.json())
			.catch((err) => {
				return null;
			});
	}

	public async updateSystemConfigPartial(
		new_sysconf: Partial<EngineSystemConfigResponseDto>
	): Promise<{ success: boolean; message: string; data: EngineSystemConfigResponseDto }> {
		return (await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(new_sysconf)
		})
			.then((res) => res.json())
			.catch((err) => {
				return { success: false, message: messages.RESPONSE_ERROR };
			})) as unknown as { success: boolean; message: string; data: EngineSystemConfigResponseDto };
	}

	public async generateToken(
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
				return await fetch(BackendEndpoints.GENERATE_TOKEN, {
					method: 'POST',
					headers: {
						'Content-Type': 'application/json'
					},
					body: JSON.stringify({
						provider,
						email: data.email,
						password: data.password
					})
				})
					.then((res) => res.json())
					.catch(() => {
						return {
							success: false,
							message: messages.COMMUNICATION_FAILURE
						};
					});
				break;
			default:
				console.log('Requested Generate Token', provider, data);
				break;
		}
	}

	public async refreshToken(refreshToken: string, provider: AuthProviderType) {
		return await fetch(BackendEndpoints.REFRESH_TOKEN, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				refreshToken,
				provider
			})
		})
			.then((res) => res.json())
			.catch(() => {
				return { success: false };
			});
	}

	public async changePassword({
		oldPassword,
		newPassword
	}: {
		oldPassword: string;
		newPassword: string;
	}) {
		return await fetch(BackendEndpoints.CHANGE_PASSWORD, {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({
				oldPassword,
				newPassword
			})
		})
			.then((res) => res.json())
			.catch((e) => {
				return {
					success: false,
					message: messages.COMMUNICATION_FAILURE
				};
			});
	}

	public async getNotificaions({
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

	////////////////////////////////////////// DB MYSQL //////////////////////////////////////////

	public async getMysqlDBs({
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

		return await fetch(url.toString(), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}

	public async searchMysqlDBs(query: string) {
		return await fetch(BackendEndpoints.MYSQL_SEARCH.replace(':query', query), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}

	public async getMysqlDB(
		db_id: string
	): Promise<EngineCommonResponseDto<DBMysql, EngineMySQLGetOneError>> {
		return await fetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', db_id), {
			method: 'GET',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}

	public async deleteMysqlDB(
		db_id: string
	): Promise<EngineCommonResponseDto<null, EngineMySQLGetOneError>> {
		return await fetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', db_id), {
			method: 'DELETE',
			headers: {
				'Content-Type': 'application/json'
			}
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}

	public async updateMysqlDB(
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

		return await fetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', dbModelId), {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(xObj)
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}

	public async revertChanges(dbModelId: string): Promise<EngineCommonResponseDto<DBMysql, null>> {
		// joratali revert changes
		return await fetch(BackendEndpoints.MYSQL_BY_ID.replace(':id', dbModelId) + '/revert', {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify({ status: DBMysqlStatus.OK })
		})
			.then((res) => res.json())
			.catch((e: Error) => {
				console.log(e);
				return {
					success: false,
					message: e?.message ?? messages.RESPONSE_ERROR
				};
			});
	}
}
