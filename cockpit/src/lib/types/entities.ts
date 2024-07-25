import type { AuthProviderType, SystemWideNoitficationTypes } from "./enums";

export type SystemUser = {
    name: string;
    email: string;
    provider: AuthProviderType;
}



export interface SystemwideNotification {
    id: string,
    message: string;
    // actionUrlHints: 'GOTO_MYSQL__VIEW', // convert hints to url
    actionUrlHints: string, // convert hints to url
    markedAsRead: false,
    type: SystemWideNoitficationTypes
}


/**
 * Entities and Models will come as it is in the Engine for simplicity
 */
export type DBMysql = {
    id: string;
    dbName: string;
    dbUser: string;
    dbPassword: string;
    status: null;
    reasonFailed: null;
}


// export class SystemWideNotification {
//     public id: string = ''
//     public message: string = ''
//     public actionUrlHints: string = '' // convert hints to url
//     public markedAsRead: boolean = false;
//     public type: SystemWideNoitficationTypes | null = null

// }