import { FILE_UPLOAD_DIR } from '$env/static/private';
import { EngineConnection } from '@/server/EngineConnection';
import { InternalNewProjectType } from '@/types/enums';
import messages from '@/utils/messages';
import { fail, json, type RequestHandler } from '@sveltejs/kit';
import fs from 'fs/promises';
import path from 'path';
import { ulid } from 'ulid';

import { Octokit } from "@octokit/core";


export const GET: RequestHandler = async ({ locals, url, request, fetch }) => {
	const q = url.searchParams.get('q');
	let per_page: any = url.searchParams.get('per_page') || '10';
	let page: any = url.searchParams.get('page') || '1';

	per_page = parseInt(per_page);
	page = parseInt(page);

	// return github repo list

	const project_type = request.headers.get('x-freshcraft-project-type');
	if (project_type === InternalNewProjectType.GITHUB_REPO) {

		// gh token

		const sysConf = await EngineConnection.getInstance().getSystemConfig()
		if (!sysConf) return json({ success: false, message: messages.ENGINE_SYSCONF_MISSING }, { status: 506 });
		const gh_token = sysConf?.systemUserOauthGithubData?.access_token;
		if (!gh_token) return json({ success: false, message: messages.GITHUB_TOKEN_MISSING }, { status: 401 });

		// console.log(gh_token);

		// console.log(sysConf)

		// get command of what to do

		// get repos
		// https://docs.github.com/en/rest/repos/repos?apiVersion=2022-11-28#list-repositories-for-the-authenticated-user
		const octokit = new Octokit({ auth: gh_token });

		try {

			// const gh_repos_url = `https://api.github.com/user/repos`;
			// const rdata = await fetch(gh_repos_url, {
			// 	headers: {
			// 		'Accept': `application/vnd.github+json`,
			// 		'Authorization': `Bearer ${gh_token}`,
			// 		// 'X-GitHub-Api-Version': `2022-11-28`
			// 	}
			// }).then(r => r.json());

			// if (!q) {

			const rdata = await octokit.request('GET /user/repos', {
				affiliation: 'owner',
				sort: 'pushed',
				per_page: per_page,
				page: page,
			})


			let hasNext = false;

			if (rdata.headers.link) {
				const linkHeader = rdata.headers.link;
				const pagesRemaining = linkHeader.includes(`rel=\"next\"`);
				console.log('pagesRemaining', pagesRemaining);
				hasNext = pagesRemaining;
			}


			console.log('rdata', rdata?.data?.length, Boolean(rdata?.headers?.link));

			// try {
			// 	const x = await octokit.request('GET /installation/repositories', {
			// 		per_page: 100,
			// 		page: 1,
			// 	})
			// 	console.log('x', x)
			// } catch (err: any) {
			// 	console.log('xx', err)
			// }

			// const nextPattern = /(?<=<)([\S]*)(?=>; rel="Next")/i;
			// const linkHeader = rdata.headers.link;
			// const pagesRemaining = linkHeader && linkHeader.includes(`rel=\"next\"`);
			// const 

			// send paginatated, that's simple, don't waste your freaking time




			// console.log("from gh", rdata)
			return json({ success: true, message: "Retrieved gh repos", repos: rdata, total_repos: rdata.data.length, hasNext, });
			// } else {

			// 	const rdata = await octokit.request('GET /search/repositories', {
			// 		q,
			// 		sort: 'updated',
			// 		per_page: per_page,
			// 		page: page,


			// 	})
			// }
		} catch (err: any) {
			// console.error('err', err);
			console.log(err?.response?.data);
			const data = err?.response?.data
			if (data.status === 401) {
				return json({ success: false, message: "Github token is invalid" }, { status: 401 });
			}

			return json({ success: false, message: "Failed to retrieve gh repos" }, { status: 400 });
		}

	}

	return json({ message: 'Invalid request' }, { status: 400 });

};

export const PATCH: RequestHandler = async ({ request }) => {

	try {

		// // get content type
		// const contentType = request.headers.get('content-type');
		// console.log(contentType);
		const project_type = request.headers.get('x-freshcraft-project-type');


		let data;
		if (project_type === InternalNewProjectType.LOCAL_FILE) {
			data = Object.fromEntries(await request.formData()) as {
				project_file: File | undefined;
			};

			if (!data?.project_file) {
				return json({ success: false, message: 'No file found. Field `project_file` required' });
			}

			const { project_file } = data;

			console.log(project_file);

			const fileName = `${ulid()}-${project_file.name}`;

			// if no directory exists, create it
			await fs.mkdir(FILE_UPLOAD_DIR, { recursive: true });


			const filePath = path.join(
				// process.cwd(),
				// '../data/uploads',
				FILE_UPLOAD_DIR,
				fileName
				// `${ulid()}-${project_file.name}`
				// `${crypto.randomUUID()}.${(project_file as Blob).type.split('/')[1]}`
			);
			// check file type
			await fs.writeFile(filePath, Buffer.from(await (project_file as Blob).arrayBuffer()));

			return json({
				success: true,
				message: 'File uploaded successfully',
				fileAbsolutePath: process.cwd() + filePath,
				fileRelativePath: filePath,
				fileName: fileName
			});
		} else if (project_type === InternalNewProjectType.GITHUB_REPO) {
			data = await request.json();
			// github stuff

			return json({
				success: true,
				message: 'Not implemented yet',
			});
		} else {
			return json({ message: 'Invalid project type' }, { status: 400 });
		}

	} catch (err: any) {
		return json({ success: false, message: err?.message ?? 'Some error' });
	}
};

export const DELETE: RequestHandler = async ({ request }) => {
	const { fileName } = (await request.json()) as {
		success: boolean;
		fileAbsolutePath: string;
		fileRelativePath: string;
		fileName: string;
	};

	if (!fileName) {
		return json({ success: false, message: 'Invalid Request. Check fileName param' });
	}

	try {
		const filePath = path.join(FILE_UPLOAD_DIR, fileName);

		await fs.unlink(filePath);
	} catch (err: any) {
		return json({ success: false, message: err?.message ?? 'Failed to delete file: ' + fileName });
	}

	return json({
		success: true,
		message: 'File deleted successfully. File: ' + fileName
	});
};
