import { browser } from "$app/environment";
import { PUBLIC_MYSQL_CONNECTION_URL_LOCAL, PUBLIC_MYSQL_CONNECTION_URL_REMOTE, PUBLIC_POSTGRES_CONNECTION_URL_LOCAL } from "$env/static/public"
import type { EngineCommonResponseDto } from "@/types/dtos";
import type { HttpMethod } from "@sveltejs/kit";

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