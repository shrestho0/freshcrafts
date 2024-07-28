import { browser } from "$app/environment";
import { PUBLIC_MYSQL_CONNECTION_URL_LOCAL, PUBLIC_MYSQL_CONNECTION_URL_REMOTE, PUBLIC_POSTGRES_CONNECTION_URL_LOCAL } from "$env/static/public"
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { HttpMethod } from "@sveltejs/kit";
import { decodeTime } from "ulid"
// Client Side Code Only


/**
 * Generate connection string for MySQL, Postgres or MongoDB
 * @param dbType - Type of database (mysql, postgres, mongo)
 * @param dbData - Database data 
 * @param connType - Connection type (remote, local)
 * @returns Connection string
 * @category Utils
 * 
*/
export function generateConnectionString(dbType: 'mysql' | 'postgres' | 'mongo', dbData: any, connType: 'remote' | 'local' = 'remote') {
    if (dbType == 'mysql') {

        const connUrl = connType === 'remote' ? PUBLIC_MYSQL_CONNECTION_URL_REMOTE : PUBLIC_MYSQL_CONNECTION_URL_LOCAL

        return dbData["dbUser"] + ':' + dbData["dbPassword"] + '@' + connUrl + connType + "/" + dbData["dbName"]
    }


    return "";
}


export function toTitleCase(str: string) {
    // https://stackoverflow.com/questions/196972/convert-string-to-title-case-with-javascript
    return str.toLowerCase().replace(/\b\w/g, s => s.toUpperCase());

}


export async function fetchDataSelf(body: any, method: HttpMethod, fallback: any = null) {
    if (!browser) return;

    const res: EngineCommonResponseDto = await fetch('', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(body)
    })
        .then((res) => res.json())
        .catch((err) => {
            console.error(err);
            return fallback;
        });

    return res;
}



export async function delay(ms: number = 500) { await new Promise((resolve) => setTimeout(resolve, ms)); }


export function ulidToDate(ulidX: string) {
    try {
        const unixTime = decodeTime(ulidX);
        return new Date(unixTime)
    } catch (e) {
        console.error(e)
        return undefined
    }
}

// date to sometimes ago
export function humanizedTimeDifference(date: Date) {
    // 
    if (!date) return "Some time ago";

    const diff = Date.now() - date.getTime();
    const seconds = Math.floor(diff / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return days + "d ago";
    if (hours > 0) return hours + "h ago";
    if (minutes > 0) return minutes + "m ago";
    if (seconds > 0) return seconds + "s ago";
    return "Just now";
}


export const hintToWebUrlMap = new Map([
    ['DBMYSQL', '/databases/mysql'],
    // more will be added later
])

export const hintToSSEUrlMap = new Map([
    ['DBMYSQL', '/sse/databases/mysql'],
    // more will be added later
])
