import type { AuthProviderType, DBMongoStatus, DBMysqlStatus, DBPostgresStatus, ProjectStatus, ProjectType, SystemWideNoitficationTypes } from './enums';

export type SystemUser = {
	name: string;
	email: string;
	provider: AuthProviderType;
};

export interface SystemwideNotification {
	id: string;
	message: string;
	// actionUrlHints: 'GOTO_MYSQL__VIEW', // convert hints to url
	actionHints: string; // convert hints to url
	markedAsRead: false;
	type: SystemWideNoitficationTypes;
}

/**
 * Entities and Models will come as it is in the Engine for simplicity
 */
export type DBMysql = {
	id: string;
	dbName: string;
	dbUser: string;
	dbPassword: string;
	status: DBMysqlStatus;
	reasonFailed: string;
	updateMessage: string;
	lastModifiedDate: string;
};
export type DBPostgres = {
	id: string;
	dbName: string;
	dbUser: string;
	dbPassword: string;
	status: DBPostgresStatus;
	reasonFailed: string;
	updateMessage: string;
	lastModifiedDate: string;
};

export type DBMongo = {
	id: string;
	dbName: string;
	dbUser: string;
	dbPassword: string;
	status: DBMongoStatus;
	reasonFailed: string;
	updateMessage: string;
	lastModifiedDate: string;
};


// export class SystemWideNotification {
//     public id: string = ''
//     public message: string = ''
//     public actionUrlHints: string = '' // convert hints to url
//     public markedAsRead: boolean = false;
//     public type: SystemWideNoitficationTypes | null = null

// }


export type Project = {
	id: string,
	projectUniqueName: string,
	type: ProjectType,
	status: ProjectStatus,
	totalVersions: number,
	activeDeploymentId: string,
	portAssigned: number,
	// githubRepo: null,
	// domain: null
}
