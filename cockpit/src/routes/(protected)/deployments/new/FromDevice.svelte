<script lang="ts">
import { enhance } from '$app/forms';
import { AllowedFileExtensions, AllowedFileMimeTypes, MaxFileSize } from '@/utils/constraints';
import type { ActionResult } from '@sveltejs/kit';
import {
	Button,
	FileUploader,
	FileUploaderButton,
	FileUploaderItem
} from 'carbon-components-svelte';
import { FileInput } from 'lucide-svelte';

let fileUploadStatus: 'uploading' | 'edit' | 'complete' | undefined = undefined;
const fileInvalid = false;
const files: File[] = [];
const theHolyFile = {
	file: undefined,
	invalid: false,
	name: '',
	size: 0,
	status: 'edit',
	selected: false,
	uploaded: false,
	fromServer: undefined
} as {
	size: number;
	name: string | undefined;
	invalid: boolean | undefined;
	status: 'uploading' | 'edit' | 'complete' | undefined;
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

let uploadLabelText = 'Upload project file';

async function handleFileAdd(e: CustomEvent<readonly File[]>) {
	console.log('Add', e.detail);
	fileUploadStatus = 'uploading';
	if (e.detail.length > 0) {
		theHolyFile.file = e.detail[0];
		theHolyFile.name = e.detail[0].name;
		theHolyFile.size = e.detail[0].size;
		theHolyFile.file = e.detail[0];
		theHolyFile.selected = true;

		if (e.detail[0].size > MaxFileSize) {
			theHolyFile.invalid = true;
			theHolyFile.errorSubject = 'File too large';
			theHolyFile.errorBody = `The file you are trying to upload is too large. Please upload a file that is less than ${MaxFileSize / 1024 / 1024} MB.`;
		}
		// check file type
		const file = e.detail[0];
		if (!AllowedFileMimeTypes.includes(file.type)) {
			theHolyFile.invalid = true;
			theHolyFile.errorSubject = 'Invalid file type';
			theHolyFile.errorBody = `The file you are trying to upload is not supported. Please upload a file with one of the following extensions: ${AllowedFileExtensions.join(
				', '
			)}.`;
		} else {
			theHolyFile.status = 'uploading';
			const { success, message, fileAbsolutePath, fileRelativePath, fileName } =
				await uploadToServer(theHolyFile.file);

			if (success) {
				theHolyFile.uploaded = true;
				theHolyFile.status = 'edit';
				theHolyFile.fromServer = {
					fileAbsolutePath,
					fileRelativePath,
					fileName
				};
			} else {
				theHolyFile.uploaded = false;
				theHolyFile.status = 'edit';
				theHolyFile.invalid = true;
				theHolyFile.errorSubject = 'Upload failed';
				theHolyFile.errorBody = message;
			}
		}
		uploadLabelText = 'Upload project file';
	}
}

async function handleFileDelete() {
	// if file uploaded;
	// delete from server
	// else just remove theHolyFile.file()

	if (theHolyFile.uploaded) {
		theHolyFile.status = 'uploading'; // not actually uploading, it's deleting
		const { success, message } = await deleteFromServer(theHolyFile);
		if (success) {
			theHolyFile.uploaded = false;
			theHolyFile.status = 'complete';
			theHolyFile.file = undefined;
			theHolyFile.fromServer = undefined;
		} else {
		}
	} else {
	}

	theHolyFile.selected = false;
	theHolyFile.invalid = false;
	theHolyFile.name = '';
	theHolyFile.size = 0;
	theHolyFile.errorBody = '';
	theHolyFile.errorSubject = '';
}

async function uploadToServer(file: File | undefined): Promise<any> {
	if (!file) return { success: false, message: 'No file provided' };
	//
	const form = new FormData();
	form.append('project_file', file, file.name);

	console.log('Uploading to server', file);
	// send to backend
	const res = await fetch('', {
		method: 'PATCH',
		// headers: {
		// 	'Content-Type': 'multipart/form-data'
		// },
		body: form
	}).then((res) => res.json());
	// return response
	console.log('Upload complete', res);
	return res;
}
async function deleteFromServer(thf: typeof theHolyFile) {
	if (!thf.file) return;
	console.log('Deleting from server', theHolyFile.fromServer);
	const res = await fetch('', {
		method: 'DELETE',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			...theHolyFile.fromServer
		})
	}).then((res) => res.json());
	console.log('Deleting from server', res);
	return res;
}
</script>

{JSON.stringify(theHolyFile, null, 2)}
{JSON.stringify(files, null, 2)}

<br />
<div class="w-full">
	<FileUploaderButton
		bind:labelText={uploadLabelText}
		disabled={theHolyFile.selected}
		kind="secondary"
		on:change={handleFileAdd}
		name="project_file"
		accept={AllowedFileExtensions}
	/>

	<!-- accept={AllowedFileExtensions} -->
</div>

{#if theHolyFile.selected}
	<FileUploaderItem
		invalid={theHolyFile.invalid}
		name={theHolyFile.name}
		status={theHolyFile.status}
		errorSubject={theHolyFile.errorSubject}
		errorBody={theHolyFile.errorBody}
		on:delete={handleFileDelete}
	/>
{/if}
