import { PUBLIC_MYSQL_CONNECTION_HOST_LOCAL, PUBLIC_MYSQL_CONNECTION_HOST_REMOTE, PUBLIC_MYSQL_CONNECTION_PORT_LOCAL, PUBLIC_MYSQL_CONNECTION_PORT_REMOTE } from "$env/static/public";
import type { DBMysql } from "@/types/entities";

export function generateMySQLShellCode(dbData: DBMysql, remote: boolean) {
    const connHost = remote ? PUBLIC_MYSQL_CONNECTION_HOST_REMOTE : PUBLIC_MYSQL_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MYSQL_CONNECTION_PORT_REMOTE : PUBLIC_MYSQL_CONNECTION_PORT_LOCAL;
    return `mysql -u ${dbData.dbUser} -p"${dbData.dbPassword}" -h ${connHost} -P ${connPort} ${dbData.dbName}`;
}

export function generateMySQLConnUrl(dbData: DBMysql, remote: boolean) {
    const connUrl = remote ? PUBLIC_MYSQL_CONNECTION_HOST_REMOTE : PUBLIC_MYSQL_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MYSQL_CONNECTION_PORT_REMOTE : PUBLIC_MYSQL_CONNECTION_PORT_LOCAL;
    return `mysql://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;

}


export function generateMySQLEnv(dbData: DBMysql, remote: boolean) {
    const connUrl = remote ? PUBLIC_MYSQL_CONNECTION_HOST_REMOTE : PUBLIC_MYSQL_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MYSQL_CONNECTION_PORT_REMOTE : PUBLIC_MYSQL_CONNECTION_PORT_LOCAL;
    const MYSQL_DB_NAME = dbData.dbName
    const MYSQL_USER = dbData.dbUser
    const MYSQL_PASSWORD = dbData.dbPassword
    return `MYSQL_DB_NAME=${MYSQL_DB_NAME}\nMYSQL_USER=${MYSQL_USER}\nMYSQL_PASSWORD=${MYSQL_PASSWORD}\nMYSQL_HOST=${connUrl}\nMYSQL_PORT=${connPort}`;
}

export function getMySQLConnectionHost(remote: boolean) {
    return remote ? PUBLIC_MYSQL_CONNECTION_HOST_REMOTE : PUBLIC_MYSQL_CONNECTION_HOST_LOCAL;
}
export function getMySQLConnectionPort(remote: boolean) {
    return remote ? PUBLIC_MYSQL_CONNECTION_PORT_REMOTE : PUBLIC_MYSQL_CONNECTION_PORT_LOCAL;
}