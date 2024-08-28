export enum AuthProviderType {
	EMAIL_PASSWORD = 'EMAIL_PASSWORD',
	OAUTH_GOOGLE = 'OAUTH_GOOGLE',
	OAUTH_GITHUB = 'OAUTH_GITHUB'
}
export enum SystemWideNoitficationTypes {
	SUCCESS = 'SUCCESS',
	INFO = 'INFO',
	WARNING = 'WARNING',
	ERROR = 'ERROR'
}

export enum DBMysqlStatus {
	REQUESTED = 'REQUESTED',
	UPDATE_REQUESTED = 'UPDATE_REQUESTED',
	OK = 'OK',
	FAILED = 'FAILED',
	UPDATE_FAILED = 'UPDATE_FAILED',
	PENDING_DELETE = 'PENDING_DELETE'
}
export enum DBPostgresStatus {
	REQUESTED = 'REQUESTED',
	UPDATE_REQUESTED = 'UPDATE_REQUESTED',
	OK = 'OK',
	FAILED = 'FAILED',
	UPDATE_FAILED = 'UPDATE_FAILED',
	PENDING_DELETE = 'PENDING_DELETE'
}
export enum DBMongoStatus {
	REQUESTED = 'REQUESTED',
	UPDATE_REQUESTED = 'UPDATE_REQUESTED',
	OK = 'OK',
	FAILED = 'FAILED',
	UPDATE_FAILED = 'UPDATE_FAILED',
	PENDING_DELETE = 'PENDING_DELETE'
}

export enum InternalNewProjectType {
	LOCAL_FILE_UPLOAD = 'LOCAL_FILE_UPLOAD',
	LIST_GITHUB_REPO = 'LIST_GITHUB_REPO',
	CREATE_PROJECT_FROM_GITHUB = 'CREATE_FROM_GITHUB',
	CREATE_PROJECT_FROM_LOCAL_FILE = 'CREATE_FROM_FILE'
}
export enum ProjectType {
	LOCAL_FILES = 'LOCAL_FILES',
	GITHUB_REPO = 'GITHUB_REPO',
}

export enum ProjectSetupCommand {
	CHECK_UNIQUE_NAME,
	LIST_SOURCE_FILES,
	DEPLOY_PROJECT,
	DELETE_INCOMPLETE_PROJECT
}


export enum ProjectStatus {

	PROCESSING_SETUP = 'PROCESSING_SETUP',
	PROCESSING_DEPLOYMENT = 'PROCESSING_DEPLOYMENT',
	PROCESSING_UPDATE = 'PROCESSING_UPDATE',
	PROCESSING_ROLLBACK = 'PROCESSING_ROLLBACK',
	PROCESSING_REDEPLOYMENT = 'PROCESSING_REDEPLOYMENT',
	PROCESSING_DELETION = 'PROCESSING_DELETION',

	ACTIVE = 'ACTIVE',
	INACTIVE = 'INACTIVE',

}


export enum ProjectDeploymentStatus {
	// Requested creation is set from engine
	PRE_CREATION = 'PRE_CREATION',
	READY_FOR_DEPLOYMENT = 'READY_FOR_DEPLOYMENT',

	REQUESTED_CREATION = 'REQUESTED_CREATION',
	BUILDING_PROJECT = 'BUILDING_PROJECT',
	SETTING_UP_PM2 = 'SETTING_UP_PM2',
	SETTING_UP_NGINX = 'SETTING_UP_NGINX',
	SETTING_UP_SSL = 'SETTING_UP_SSL',
	COMPLETED_CREATION = 'COMPLETED_CREATION',

	REQUESTED_DELETION = 'REQUESTED_DELETION',
	DELETING_DEPLOYMENT = 'DELETING_DEPLOYMENT',
	// on this event, engine will delete the project and send notification
	// accordingly
	COMPLETED_DELETION = 'COMPLETED_DELETION',

	FAILED_BUILD = 'FAILED_BUILD',
	FAILED_PM2_SETUP = 'FAILED_PM2_SETUP',
	FAILED_NGINX_SETUP = 'FAILED_NGINX_SETUP',
	FAILED_SSL_SETUP = 'FAILED_SSL_SETUP',

}


export enum AIChatCommands {
	INIT_CHAT = 'INIT_CHAT',
	SEND_MESSAGE = 'SEND_MESSAGE',
	GET_MESSAGES = 'GET_MESSAGES',
	CLOSE_CHAT_WITH_SYNC = 'CLOSE_CHAT_WITH_DB_SYNC',
	CLOSE_CHAT_WITHOUT_SYNC = 'CLOSE_CHAT_WITHOUT_DB_SYNC',
}