import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ locals }) => {
    console.log("...dashboard")
};