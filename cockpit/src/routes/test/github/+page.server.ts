import type { PageServerLoad } from "./$types";

export const load: PageServerLoad = async ({ locals, cookies }) => {
    const savedToken = cookies.get("gh_token")
    const savedUser = cookies.get("gh_user")

    if (!savedToken) {
        return {
            user: null,
            repos: []
        }
    }

    const user = JSON.parse(savedUser ?? "{}")
    const repos = await fetch(`https://api.github.com/user/repos?sort=-created&type=owner&per_page=40`, {
        headers: {
            'Accept': 'application/vnd.github+json',
            'Authorization': `Bearer ${savedToken}`,
        }
    }).then(res => res.json()).catch(err => { console.log("gh_repos_err", err); return [] })


    console.log("gh_repos", repos)

    return {
        user,
        repos: repos
    }

};