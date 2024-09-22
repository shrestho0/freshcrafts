import type { PageServerLoad } from "./$types";


export const load: PageServerLoad = async ({ parent, params, cookies }) => {
    // on delete, project/id will show 404
    // so, settings a cookie to handle this from +error.svelte


    const pData = await parent()
    console.warn('\n[DEBUG]: pData: ', pData, '\n\n');

    // cookies.set('error_fallback', JSON.stringify({
    //     href: `/projects/${proj_id}`,
    //     title: 'Back to project'
    // }), { path: '/', maxAge: 60 * 60 * 1, secure: false, httpOnly: false })

};