import { PUBLIC_MONGO_CONNECTION_HOST_LOCAL, PUBLIC_MONGO_CONNECTION_HOST_REMOTE, PUBLIC_MONGO_CONNECTION_PORT_LOCAL, PUBLIC_MONGO_CONNECTION_PORT_REMOTE, PUBLIC_MYSQL_CONNECTION_HOST_LOCAL, PUBLIC_MYSQL_CONNECTION_HOST_REMOTE, PUBLIC_MYSQL_CONNECTION_PORT_LOCAL, PUBLIC_MYSQL_CONNECTION_PORT_REMOTE, PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL, PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE, PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL, PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE, PUBLIC_REDIS_CONNECTION_HOST_LOCAL, PUBLIC_REDIS_CONNECTION_HOST_REMOTE, PUBLIC_REDIS_CONNECTION_PORT_LOCAL, PUBLIC_REDIS_CONNECTION_PORT_REMOTE } from "$env/static/public";
import type { DBMongo, DBMysql, DBPostgres, DBRedis } from "@/types/entities";


///// Mysql Specific /////

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



///// Postgres Specific /////


export function generatePostgreSQLShellCode(dbData: DBPostgres, remote: boolean) {
    const connHost = remote ? PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE : PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE : PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL;
    // return `mysql -u ${dbData.dbUser} -p"${dbData.dbPassword}" -h ${connHost} -P ${connPort} ${dbData.dbName}`;
    return `psql -U ${dbData.dbUser} -h ${connHost} -p ${connPort} ${dbData.dbName}`;
}

export function generatePostgreSQLConnUrl(dbData: DBPostgres, remote: boolean) {
    const connUrl = remote ? PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE : PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE : PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL;
    // return `mysql://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;
    return `postgresql://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;

}

export function generatePostgreSQLEnv(dbData: DBPostgres, remote: boolean) {
    const connUrl = remote ? PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE : PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE : PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL;
    const POSTGRES_DB_NAME = dbData.dbName
    const POSTGRES_USER = dbData.dbUser
    const POSTGRES_PASSWORD = dbData.dbPassword
    // return `MYSQL_DB_NAME=${MYSQL_DB_NAME}\nMYSQL_USER=${MYSQL_USER}\nMYSQL_PASSWORD=${MYSQL_PASSWORD}\nMYSQL_HOST=${connUrl}\nMYSQL_PORT=${connPort}`;
    return `POSTGRES_DB_NAME=${POSTGRES_DB_NAME}\nPOSTGRES_USER=${POSTGRES_USER}\nPOSTGRES_PASSWORD=${POSTGRES_PASSWORD}\nPOSTGRES_HOST=${connUrl}\nPOSTGRES_PORT=${connPort}`;
}

export function getPostgreSQLConnectionHost(remote: boolean) {
    return remote ? PUBLIC_POSTGRES_CONNECTION_HOST_REMOTE : PUBLIC_POSTGRES_CONNECTION_HOST_LOCAL;
}
export function getPostgreSQLConnectionPort(remote: boolean) {
    return remote ? PUBLIC_POSTGRES_CONNECTION_PORT_REMOTE : PUBLIC_POSTGRES_CONNECTION_PORT_LOCAL;
}





///// Mongo Specific /////


export function generateMongoDBShellCode(dbData: DBMongo, remote: boolean) {
    const connHost = remote ? PUBLIC_MONGO_CONNECTION_HOST_REMOTE : PUBLIC_MONGO_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MONGO_CONNECTION_PORT_REMOTE : PUBLIC_MONGO_CONNECTION_PORT_LOCAL;
    // return `mysql -u ${dbData.dbUser} -p"${dbData.dbPassword}" -h ${connHost} -P ${connPort} ${dbData.dbName}`;
    // return `psql -U ${dbData.dbUser} -h ${connHost} -p ${connPort} ${dbData.dbName}`;
    return `mongo --username ${dbData.dbUser} --password ${dbData.dbPassword} --host ${connHost} --port ${connPort} ${dbData.dbName}`;
}

export function generateMongoDBConnUrl(dbData: DBMongo, remote: boolean) {
    const connUrl = remote ? PUBLIC_MONGO_CONNECTION_HOST_REMOTE : PUBLIC_MONGO_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MONGO_CONNECTION_PORT_REMOTE : PUBLIC_MONGO_CONNECTION_PORT_LOCAL;
    // return `mysql://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;
    // return `postgresql://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;
    return `mongodb://${dbData.dbUser}:${dbData.dbPassword}@${connUrl}:${connPort}/${dbData.dbName}`;
}


export function generateMongoDBEnv(dbData: DBMongo, remote: boolean) {
    const connUrl = remote ? PUBLIC_MONGO_CONNECTION_HOST_REMOTE : PUBLIC_MONGO_CONNECTION_HOST_LOCAL;
    const connPort = remote ? PUBLIC_MONGO_CONNECTION_PORT_REMOTE : PUBLIC_MONGO_CONNECTION_PORT_LOCAL;
    const MONGO_DB_NAME = dbData.dbName
    const MONGO_USER = dbData.dbUser
    const MONGO_PASSWORD = dbData.dbPassword
    // return `MYSQL_DB_NAME=${MYSQL_DB_NAME}\nMYSQL_USER=${MYSQL_USER}\nMYSQL_PASSWORD=${MYSQL_PASSWORD}\nMYSQL_HOST=${connUrl}\nMYSQL_PORT=${connPort}`;
    return `MONGO_DBNAME=${MONGO_DB_NAME}\nMONGO_USER=${MONGO_USER}\nMONGO_PASSWORD=${MONGO_PASSWORD}\nMONGO_HOST=${connUrl}\nMONGO_PORT=${connPort}`;
}

export function getMongoDBConnectionHost(remote: boolean) {
    return remote ? PUBLIC_MONGO_CONNECTION_HOST_REMOTE : PUBLIC_MONGO_CONNECTION_HOST_LOCAL;
}
export function getMongoDBConnectionPort(remote: boolean) {
    return remote ? PUBLIC_MONGO_CONNECTION_PORT_REMOTE : PUBLIC_MONGO_CONNECTION_PORT_LOCAL;
}


///// Redis Specific /////

export function generateRedisShellCode(dbData: DBRedis, remote: boolean) {
    // redis-cli -h 127.0.0.1 -p 16379 --user test01 --pass test01_up 
    return `redis-cli -h ${getRedisConnectionHost(remote)} -p ${getRedisConnectionPort(remote)} --user ${dbData.username} --pass ${dbData.password}`

}
export function generateRedisConnUrl(dbData: DBRedis, remote: boolean) {
    //redis-cli -u redis://test01:test01_up@127.0.0.1:16379
    return `redis://${dbData.username}:${dbData.password}@${getRedisConnectionHost(remote)}:${getRedisConnectionPort(remote)}`

}
export function generateRedisEnv(dbData: DBRedis, remote: boolean) {
    return `REDIS_PREFIX=${dbData.dbPrefix}\nREDIS_USER=${dbData.username}\nREDIS_PASSWORD=${dbData.password}\nREDIS_HOST=${getRedisConnectionHost(remote)}\nREDIS_PORT=${getRedisConnectionPort(remote)}`;
}

export function getRedisConnectionHost(remote: boolean) {
    return remote ? PUBLIC_REDIS_CONNECTION_HOST_REMOTE : PUBLIC_REDIS_CONNECTION_HOST_LOCAL
}
export function getRedisConnectionPort(remote: boolean) {
    return remote ? PUBLIC_REDIS_CONNECTION_PORT_REMOTE : PUBLIC_REDIS_CONNECTION_PORT_LOCAL
}
