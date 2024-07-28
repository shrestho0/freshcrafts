
export enum AuthProviderType {
    EMAIL_PASSWORD = "EMAIL_PASSWORD",
    OAUTH_GOOGLE = "OAUTH_GOOGLE",
    OAUTH_GITHUB = "OAUTH_GITHUB",
}
export enum SystemWideNoitficationTypes {
    SUCCESS = "SUCCESS",
    INFO = "INFO",
    WARNING = "WARNING",
    ERROR = "ERROR"
}

export enum DBMysqlStatus {
    REQUESTED = "REQUESTED",
    UPDATE_REQUESTED = "UPDATE_REQUESTED",
    OK = "OK",
    FAILED = "FAILED",
    UPDATE_FAILED = "UPDATE_FAILED",
    PENDING_DELETE = "PENDING_DELETE",
}