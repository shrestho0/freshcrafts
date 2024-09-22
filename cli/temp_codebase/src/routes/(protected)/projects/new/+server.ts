import { EngineConnection } from '@/server/EngineConnection';
import { InternalNewProjectType, ProjectType } from '@/types/enums';
import messages from '@/utils/messages';
import { json, type RequestHandler } from '@sveltejs/kit';
import { ulid } from '@/utils/ulid';

import { GithubWebhookHelper } from '@/server/GithubWebhookHelper';
import { FilesHelper } from '@/server/FilesHelper';
import type { ProjectDeployment, ProjectDeploymentFile, ProjectDeploymentSource } from '@/types/entities';



export const PATCH: RequestHandler = async ({ request }) => {
	const fileHelper = FilesHelper.getInstance();

	try {

		// // get content type
		// const contentType = request.headers.get('content-type');
		// console.log(contentType);
		const project_type = request.headers.get('x-freshcraft-project-type');


		let data;
		if (project_type === InternalNewProjectType.LOCAL_FILE_UPLOAD) {
			data = Object.fromEntries(await request.formData()) as {
				project_file: File | undefined;
			};

			if (!data?.project_file) {
				return json({ success: false, message: 'No file found. Field `project_file` required' });
			}

			const { project_file } = data;

			console.log(project_file);



			// const fileName = `${ulid()}-${project_file.name}`;

			// // if no directory exists, create it
			// await fs.mkdir(FILE_UPLOAD_DIR, { recursive: true });


			// const filePath = path.join(
			// 	// process.cwd(),
			// 	// '../data/uploads',
			// 	FILE_UPLOAD_DIR,
			// 	fileName
			// 	// `${ulid()}-${project_file.name}`
			// 	// `${crypto.randomUUID()}.${(project_file as Blob).type.split('/')[1]}`
			// );
			// // check file type
			// await fs.writeFile(filePath, Buffer.from(await (project_file as Blob).arrayBuffer()));

			// const fileHelper = FileHelper.getInstance();
			const _fileName = `${ulid()?.slice(0, 6)}-${project_file.name}`;
			// saving file
			const { fileName, filePath, fileAbsPath } = await fileHelper.writeToTempFile(_fileName, Buffer.from(await (project_file as Blob).arrayBuffer()))


			return json({
				success: true,
				message: 'File uploaded successfully',
				fileAbsolutePath: fileAbsPath,
				fileRelativePath: filePath,
				fileName: fileName
			});
		} else if (project_type === InternalNewProjectType.CREATE_PROJECT_FROM_GITHUB) {
			const repo = await request.json();
			console.log("REPO->?", repo);

			// github stuff

			let ooctokit = GithubWebhookHelper.getInstance().getOctokitInstance()
			if (!ooctokit) {
				return json({ success: false, message: 'Failed to create octokit instance' });
			}

			if (repo?.downloads_url) {
				try {
					const sysConf = await EngineConnection.getInstance().getSystemConfig()
					if (!sysConf) return json({ success: false, message: 'Failed to get system config' });
					// if (!sysConf) return json({ success: false, message: 'Failed to get system config' });
					const gh_token = sysConf.systemUserOauthGithubData?.user_access_token
					// 
					// ooctokit = GithubWebhookHelper.getInstance().getOctokitInstance()
					try {
						// FIXME: change only the last part of the url, not the whole thing
						const tar_download_url = repo.downloads_url?.replace("/downloads", "/tarball")
						// const download = await ooctokit.request('GET ' + tar_download_url)
						const download = await fetch(tar_download_url, {
							headers: {
								'Authorization': `token ${gh_token}`
							}
						}).then(res => res.blob())

						// const fileHelper = FileHelper.getInstance();
						const projectId: string = ulid();
						const _fileName = `${repo.name}-${ulid()?.slice(0, 6)}.tar.gz`;
						// saving file
						const { fileName, filePath, fileAbsPath } = await fileHelper.writeProjectSourceFile(projectId, _fileName, Buffer.from(await download.arrayBuffer()))

						repo.is_private = repo?.private;
						// extract files to src
						const { extracted, extractedAbs } = await fileHelper.decompressProjectSource(fileAbsPath, projectId);
						console.log("DECOMPRESSED", extracted, extractedAbs);

						const projectCreationRepoData = {
							type: ProjectType.GITHUB_REPO,
							newProjectId: projectId,
							projectDir: fileHelper.getProjectDir(projectId),
							file: {
								name: fileName,
								path: filePath,
								absPath: fileAbsPath,
							} as ProjectDeploymentFile,

							src: {
								filesDirPath: extracted,
								filesDirAbsPath: extractedAbs,
							} as ProjectDeploymentSource,

							github_repo: repo,
							github_tar_download_url: tar_download_url,
						}

						// console.log(projectCreationRepoData);

						// send to engine

						// const engineConn = EngineConnection.getInstance()
						const x = await EngineConnection.getInstance().initProject(projectCreationRepoData);
						console.log("-------------------\n", x, "\n-------------------");

						return json(x);

					} catch (err) {
						console.log(err)
					}
				} catch (err) {
					console.log(err)
				}
			}


			return json({
				success: true,
				message: 'Failed to create project from github',
			}, { status: 400 });
		} else if (project_type == InternalNewProjectType.CREATE_PROJECT_FROM_LOCAL_FILE) {

			data = await request.json();

			// console.log("FILE>>>>>>>>>>>>CREATE_PROJ", data);

			const projectId: string = ulid();

			// extract files to src
			const { fileName, filePath, fileAbsPath } = await fileHelper.moveFileToProjectDir(projectId, data?.fileName, data?.fileAbsolutePath)
			const { extracted, extractedAbs } = await fileHelper.decompressProjectSource(fileAbsPath, projectId);

			console.log("DECOMPRESSED", extracted, extractedAbs);

			const projectCreationRepoData = {
				type: ProjectType.LOCAL_FILES,
				newProjectId: projectId,
				projectDir: fileHelper.getProjectDir(projectId),
				file: {
					// name: data?.fileName,
					// path: data?.fileRelativePath,
					// absPath: data?.fileAbsolutePath,
					name: fileName,
					path: filePath,
					absPath: fileAbsPath,
				} as ProjectDeploymentFile,

				src: {
					filesDirPath: extracted,
					filesDirAbsPath: extractedAbs,
				} as ProjectDeploymentSource,

				github_repo: null,
				github_tar_download_url: null,
			}

			// Decompress file

			console.log(projectCreationRepoData);
			const x = await EngineConnection.getInstance().initProject(projectCreationRepoData);
			console.log("-------------------\n", x, "\n-------------------");
			return json(x);
			// return json({ success: true, message: 'Not implemented yet' });
		} else if (project_type === InternalNewProjectType.ROLLFORWARD_FROM_LOCAL_FILE) {

			///////////////////////// ROLLBACK PRE_PROCESSING /////////////////////////


			data = await request.json();
			const pdata = await EngineConnection.getInstance().getProject(data.projectId)
			const project = pdata.project
			if (!pdata || !project) {
				return json({ success: false, message: "Failed to retrieve project with id: " + data.projectId })
			}
			console.log("project", project, "\nfdata", data)
			const newVersion = project.totalVersions + 1
			const fromServer = data.fromServer;
			const { fileName, filePath, fileAbsPath } = await fileHelper.moveFileToProjectDir(data.projectId, fromServer?.fileName, fromServer?.fileAbsolutePath, newVersion)
			const { extracted, extractedAbs } = await fileHelper.decompressProjectSource(fileAbsPath, data.projectId, newVersion);

			// create new deployment

			const newCurrDep = await EngineConnection.getInstance().rollforwardProjectPreProcessing(project.id, {
				version: newVersion,
				rawFile: {
					name: fileName,
					path: filePath,
					absPath: fileAbsPath,
				},
				src: {
					filesDirPath: extracted,
					filesDirAbsPath: extractedAbs,
				},
				github_repo: null,
				github_tar_download_url: null,
			} as unknown as Partial<ProjectDeployment>)


			return json(newCurrDep)
			// console.log("newCurrDep",)


			// return json({
			// 	success: false,
			// 	message: "Being implimented"
			// })


		} else if (project_type === InternalNewProjectType.LIST_GITHUB_REPO) {


			const responseObj = {
				success: false,
				message: messages.GITHUB_UNHANDLED_ERROR,
				total_count: 0,
				repos: [],
			} as {
				success: boolean;
				message: string;
				total_count: number;
				repos: any[];
			}
			const oOctokit = GithubWebhookHelper.getInstance().getOctokitInstance(null)
			if (!oOctokit) {
				responseObj.message = messages.GITHUB_OCTOKIT_CREATE_ERROR
				return json(responseObj);
			}

			const installation_request = await oOctokit.request('GET /app/installations', {
				headers: {
					'X-GitHub-Api-Version': '2022-11-28',
				}
			})

			const reduced_installation_request = installation_request.data.map((installation: any) => {
				return {
					access_tokens_url: installation.access_tokens_url,
					repositories_url: installation.repositories_url,
				};
			})

			// console.log('installation_request', JSON.stringify(installation_request, null, 2));
			// console.log('access_tokens', reduced_installation_request);

			if (reduced_installation_request.length === 0) {
				responseObj.message = messages.GITHUB_INSTALLATION_RETREIVAL_ERROR
				return json(responseObj)

			}

			for await (const installation_min of reduced_installation_request) {
				const some_access_token = await oOctokit.request('POST ' + installation_min.access_tokens_url,
					{
						headers: {
							'X-GitHub-Api-Version': '2022-11-28'
						}
					});

				const token = some_access_token?.data?.token
				console.log('[DEBUG] [ACCESS TOKEN]', token);
				if (!token) throw new Error('[DEBUG] [ACCESS TOKEN] No token found');

				const newOcto = GithubWebhookHelper.getInstance().getOctokitInstance(token)

				// console.log(reporepo.headers)
				const nextPattern = /(?<=<)([\S]*)(?=>; rel="Next")/i;
				let pagesRemaining: any = true;

				let url = installation_min.repositories_url;

				while (pagesRemaining) {
					const reporepo = await newOcto?.request('GET ' + url, {
						headers: {
							'X-GitHub-Api-Version': '2022-11-28'
						},
						per_page: 100,

					})

					if (!reporepo?.data?.total_count) continue

					responseObj.total_count = reporepo?.data?.total_count
					responseObj.repos = [...responseObj.repos, ...reporepo?.data?.repositories]

					// data = [...data, ...parsedData];

					const linkHeader: string | undefined = reporepo.headers.link;
					if (!linkHeader) break;


					pagesRemaining = linkHeader && linkHeader.includes(`rel=\"next\"`)
					// console.log(linkHeader)
					if (pagesRemaining) {
						url = linkHeader.match(nextPattern);
						if (url.length > 0) url = url[0];
						// console.log(url)
						// if (!url) pagesRemaining = false;
					}
				}

			}

			responseObj.success = true;
			responseObj.message = 'Github repos retrieved successfully';
			console.log('[DEBUG] [REPOS]', responseObj.total_count);
			console.log('[DEBUG] [REPOS LEN]', responseObj.repos.length);

			// sort by pushed_at
			responseObj.repos = responseObj.repos.sort((a, b) => {
				return new Date(b.pushed_at).getTime() - new Date(a.pushed_at).getTime();
			})

			return json(responseObj);

		} else {
			return json({ message: 'Invalid command' });
		}


	} catch (err: any) {
		return json({ success: false, message: err?.message ?? 'Some error' });
	}
};

export const DELETE: RequestHandler = async ({ request }) => {
	const fileHelper = FilesHelper.getInstance();

	const { fileName, fileAbsolutePath, fileRelativePath } = (await request.json()) as {
		success: boolean;
		fileAbsolutePath: string;
		fileRelativePath: string;
		fileName: string;
	};

	if (!fileName) {
		return json({ success: false, message: 'Invalid Request. Check fileName param' });
	}

	try {
		// const filePath = path.join(FILE_UPLOAD_DIR, fileName);

		// await fs.unlink(filePath);
		await fileHelper.deleteFile(fileRelativePath);

	} catch (err: any) {
		return json({ success: false, message: err?.message ?? 'Failed to delete file: ' + fileName });
	}

	return json({
		success: true,
		message: 'File deleted successfully. File: ' + fileName
	});
};
