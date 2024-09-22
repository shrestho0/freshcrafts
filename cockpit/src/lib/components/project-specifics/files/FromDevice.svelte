<script lang="ts">
	import { browser } from "$app/environment";
	import { enhance } from "$app/forms";
	import { invalidateAll } from "$app/navigation";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import { InternalNewProjectType } from "@/types/enums";
	import ArrowRightIcon from "@/ui/icons/ArrowRightIcon.svelte";
	import {
		AllowedFileExtensions,
		AllowedFileMimeTypes,
		MaxFileSize,
	} from "@/utils/constraints";
	import type { ActionResult } from "@sveltejs/kit";
	import {
		Button,
		FileUploader,
		FileUploaderButton,
		FileUploaderItem,
		Tile,
	} from "carbon-components-svelte";
	import { onMount } from "svelte";

	const fileInvalid = false;
	const files: File[] = [];

	export let rollforward = false;
	export let projectId: undefined | string = undefined;

	let uploadLabelText = "Upload project file";

	// FIXME: Server side errors are not handled

	const savedFile = {
		fileX: undefined,
		project_url: "",
		err_msg: "",

		theHolyFile: {
			file: undefined,
			invalid: false,
			name: "",
			size: 0,
			status: "edit",
			selected: false,
			uploaded: false,
			fromServer: undefined,
		},
		fileUploadStatus: undefined,

		save: (anything: any) => {
			// do something

			if (anything) {
				savedFile.fileX = anything;
			}
			if (savedFile.fileX) {
				try {
					localStorage.setItem(
						"savedFile.fileX",
						JSON.stringify(savedFile.fileX),
					);
					localStorage.setItem(
						"savedFile.theHolyFile",
						JSON.stringify(savedFile.theHolyFile),
					);
					localStorage.setItem(
						"savedFile.fileUploadStatus",
						JSON.stringify(savedFile.fileUploadStatus),
					);
				} catch (e) {
					// console.error(e);
				}
			}
		},
		load: () => {
			// do something
			const fileX = localStorage.getItem("savedFile.fileX");
			const theHolyFile = localStorage.getItem("savedFile.theHolyFile");
			const fileUploadStatus = localStorage.getItem(
				"savedFile.fileUploadStatus",
			);

			try {
				if (fileX) {
					savedFile.fileX = JSON.parse(fileX);
				}
				if (theHolyFile) {
					savedFile.theHolyFile = JSON.parse(theHolyFile);
				}
				if (fileUploadStatus) {
					savedFile.fileUploadStatus = JSON.parse(fileUploadStatus);
				}
			} catch (e) {}
		},
		delete: () => {
			// do something
			savedFile.fileX = undefined;
			localStorage.removeItem("savedFile.theHolyFile");
			localStorage.removeItem("savedFile.fileX");
			localStorage.removeItem("savedFile.fileUploadStatus");
		},
	} as {
		fileX: any;
		project_url: string;
		err_msg: string;
		theHolyFile: {
			size: number;
			name: string | undefined;
			invalid: boolean | undefined;
			status: "uploading" | "edit" | "complete" | undefined;
			errorSubject?: string | undefined;
			errorBody?: string | undefined;
			file: File | undefined;
			selected: boolean;
			uploaded: boolean;
			fromServer:
				| {
						fileAbsolutePath: string;
						fileRelativePath: string;
						fileName: string;
				  }
				| undefined;
		};
		save: (anything: any) => void;
		load: () => void;
		delete: () => void;

		fileUploadStatus:
			| "uploading"
			| "edit"
			| "complete"
			| "leaving_page"
			| undefined;
	};
	const endpoint = "/projects/new";

	async function handleFileAdd(e: CustomEvent<readonly File[]>) {
		console.log("Add", e.detail);
		savedFile.theHolyFile.uploaded = false;
		savedFile.fileUploadStatus = "uploading";

		if (e.detail.length > 0) {
			savedFile.theHolyFile.file = e.detail[0];
			savedFile.theHolyFile.name = e.detail[0].name;
			savedFile.theHolyFile.size = e.detail[0].size;
			savedFile.theHolyFile.file = e.detail[0];
			savedFile.theHolyFile.selected = true;

			if (e.detail[0].size > MaxFileSize) {
				savedFile.theHolyFile.uploaded = false;
				savedFile.theHolyFile.status = "edit";
				savedFile.theHolyFile.invalid = true;

				savedFile.theHolyFile.errorSubject = "File too large";
				savedFile.theHolyFile.errorBody = `The file you are trying to upload is too large. Please upload a file that is less than ${MaxFileSize / 1024 / 1024} MB.`;

				uploadLabelText = "Upload project file";
				return;
			}
			// check file type
			const file = e.detail[0];
			if (!AllowedFileMimeTypes.includes(file.type)) {
				savedFile.theHolyFile.invalid = true;
				savedFile.theHolyFile.errorSubject = "Invalid file type";
				savedFile.theHolyFile.errorBody = `The file you are trying to upload is not supported. Please upload a file with one of the following extensions: ${AllowedFileExtensions.join(
					", ",
				)}.`;
			} else {
				savedFile.theHolyFile.status = "uploading";
				const resX = await uploadToServer(savedFile.theHolyFile.file);
				const {
					success,
					message,
					fileAbsolutePath,
					fileRelativePath,
					fileName,
				} = resX;

				if (success) {
					savedFile.theHolyFile.uploaded = true;
					savedFile.theHolyFile.status = "edit";
					savedFile.theHolyFile.fromServer = {
						fileAbsolutePath,
						fileRelativePath,
						fileName,
					};
					savedFile.save(resX);
				} else {
					savedFile.theHolyFile.uploaded = false;
					savedFile.theHolyFile.status = "edit";
					savedFile.theHolyFile.invalid = true;
					savedFile.theHolyFile.errorSubject = "Upload failed";
					savedFile.theHolyFile.errorBody = message;
				}
			}
			uploadLabelText = "Upload project file";
		}
	}

	async function handleFileDelete() {
		// if file uploaded;
		// delete from server
		// else just remove savedFile.theHolyFile.file()

		if (savedFile.theHolyFile.uploaded) {
			savedFile.theHolyFile.status = "uploading"; // not actually uploading, it's deleting
			const { success, message } = await deleteFromServer(
				savedFile.theHolyFile,
			);
			if (success) {
				savedFile.theHolyFile.uploaded = false;
				savedFile.theHolyFile.status = "complete";
				savedFile.theHolyFile.file = undefined;
				savedFile.theHolyFile.fromServer = undefined;
			} else {
			}
		} else {
		}

		savedFile.theHolyFile.selected = false;
		savedFile.theHolyFile.invalid = false;
		savedFile.theHolyFile.name = "";
		savedFile.theHolyFile.size = 0;
		savedFile.theHolyFile.errorBody = "";
		savedFile.theHolyFile.errorSubject = "";
	}

	async function uploadToServer(file: File | undefined): Promise<any> {
		if (!file) return { success: false, message: "No file provided" };
		//
		const form = new FormData();
		form.append("project_file", file, file.name);

		console.log("Uploading to server", file);
		// send to backend
		const res = await fetch(endpoint, {
			method: "PATCH",
			headers: {
				"x-freshcraft-project-type":
					InternalNewProjectType.LOCAL_FILE_UPLOAD,
			},
			body: form,
		}).then((res) => res.json());
		// return response
		console.log("Upload complete", res);
		return res;
	}

	async function deleteFromServer(thf: typeof savedFile.theHolyFile) {
		if (!thf.file) return;
		console.log("Deleting from server", savedFile.theHolyFile.fromServer);
		const res = await fetch(endpoint, {
			method: "DELETE",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				...savedFile.theHolyFile.fromServer,
			}),
		}).then((res) => res.json());
		savedFile.delete();
		console.log("Deleting from server", res);

		return res;
	}

	onMount(() => {
		savedFile.load();
	});

	let procceeding = false;
	async function proceedToProjectCreation() {
		procceeding = true;
		if (!browser) return;
		// if (savedFile.fileUploadStatus == 'complete') {
		if (savedFile.theHolyFile.selected) {
			window.addEventListener("beforeunload", function (e) {
				if (savedFile.fileUploadStatus == "leaving_page") return null;
				e.preventDefault();
			});
		}
		// }
		procceeding = false;
		// proceed to project creation
		const res = await fetch(endpoint, {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcraft-project-type":
					InternalNewProjectType.CREATE_PROJECT_FROM_LOCAL_FILE,
			},
			body: JSON.stringify({
				...savedFile.theHolyFile.fromServer,
			}),
		}).then((res) => res.json());
		if (res.success) {
			// redirect to /projects/:id/setup
			// window.removeEventListener('beforeunload', function (e) {
			// 	window.location.href = `/projects/${res.project_id}/setup`;
			// });
			// savedGithubStuff.newly_created_project = res?.project;
			const proj = res?.payload;
			savedFile.project_url = `/projects/${proj?.id}/initial-setup`;
			savedFile.fileUploadStatus = "leaving_page";
			savedFile.delete();
			window.location.href = savedFile.project_url;
		} else {
			// FIXME: Fix errors
			savedFile.err_msg = res?.message || "Some error occurred";
		}

		console.log("Proceed to project creation", res);
	}

	async function proceedRollforwardDepCreation() {
		console.log("Proceeding rollforward");
		procceeding = true;
		if (!browser) return;
		// if (savedFile.fileUploadStatus == 'complete') {
		if (savedFile.theHolyFile.selected) {
			// window.addEventListener("beforeunload", function (e) {
			// 	if (savedFile.fileUploadStatus == "leaving_page") return null;
			// 	e.preventDefault();
			// });
		}
		// }
		// proceed to project creation
		const res = await fetch(endpoint, {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
				"x-freshcraft-project-type":
					InternalNewProjectType.ROLLFORWARD_FROM_LOCAL_FILE,
			},
			body: JSON.stringify({
				// ...savedFile.theHolyFile.fromServer,
				fromServer: savedFile.theHolyFile.fromServer,
				projectId,
			}),
		}).then((res) => res.json());
		procceeding = false;

		window.removeEventListener("beforeunload", function (e) {
			console.log("listener removed");
			// return true;
		});

		if (res.success) {
			// redirect to /projects/:id/setup
			// window.removeEventListener("beforeunload", function (e) {
			// 	// window.location.href = `/projects/${res.project_id}/roll`;
			// 	console.log("created");
			// });
			// savedGithubStuff.newly_created_project = res?.project;
			// const proj = res?.payload;
			// savedFile.project_url = `/projects/${proj?.id}/initial-setup`;
			invalidateAll();

			console.log("invalidating all");

			savedFile.delete();
			// window.location.href = savedFile.project_url;
		} else {
			// FIXME: Fix errors
			savedFile.err_msg = res?.message || "Some error occurred";
			savedFile.delete();
		}

		console.log("Proceed to project creation", res);
	}
</script>

<br />
<div class="w-full">
	<FileUploaderButton
		bind:labelText={uploadLabelText}
		disabled={savedFile.theHolyFile.selected}
		kind="secondary"
		on:change={handleFileAdd}
		name="project_file"
		accept={AllowedFileExtensions}
	/>
</div>

{#if savedFile.theHolyFile.selected}
	<FileUploaderItem
		invalid={savedFile.theHolyFile.invalid}
		name={savedFile.theHolyFile.name}
		status={savedFile.theHolyFile.status}
		errorSubject={savedFile.theHolyFile.errorSubject}
		errorBody={savedFile.theHolyFile.errorBody}
		on:delete={handleFileDelete}
	/>

	<div class="w-full flex gap-4">
		{#if rollforward}
			<!-- <button
				disabled={procceeding || savedFile.theHolyFile.invalid}
				class="w-full flex gap-2 items-center justify-center bg-[var(--cds-interactive-02)] text-white py-2"
				on:click={proceedRollforwardDepCreation}
			>
				Proceed Rollforward <ArrowRightIcon class="w-5 h-5" />
			</button> -->
			<Button
				disabled={procceeding || savedFile.theHolyFile.invalid}
				class="w-full rounded flex items-center justify-between px-4"
				style="max-width:100%"
				on:click={proceedRollforwardDepCreation}
			>
				Proceed Rollforward Setup <ArrowRightIcon class="w-5 h-5" />
			</Button>
		{:else}
			<!-- <button
				disabled={procceeding || savedFile.theHolyFile.invalid}
				class="w-full flex gap-2 items-center justify-center bg-[var(--cds-interactive-02)] text-white py-2"
				on:click={proceedToProjectCreation}
			>
				Proceed Setup <ArrowRightIcon class="w-5 h-5" />
			</button> -->
			<Button
				disabled={procceeding || savedFile.theHolyFile.invalid}
				class="w-full rounded flex items-center justify-between px-4"
				style="max-width:100%"
				on:click={proceedToProjectCreation}
			>
				Proceed Setup <ArrowRightIcon class="w-5 h-5" />
			</Button>
		{/if}
	</div>
{:else if savedFile.fileUploadStatus == "leaving_page"}
	<Tile>
		<div class="flex flex-col items-center justify-center">
			<div class="text-lg font-normal">
				Redirecting to project setup page.
			</div>
			<a class="text-md py-1" href={savedFile.project_url}
				>Click here if not redirected</a
			>
		</div>
	</Tile>{/if}
<!-- <PreDebug data={savedFile.theHolyFile} />
<PreDebug data={files} /> -->
