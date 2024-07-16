import { FILE_UPLOAD_DIR } from '$env/static/private';
import { fail, json, type RequestHandler } from '@sveltejs/kit';
import fs from 'fs/promises'
import path from 'path'
import { ulid } from 'ulid';

export const PATCH: RequestHandler = async ({ request }) => {

    try {
        const data = Object.fromEntries(await request.formData()) as {
            project_file: File | undefined
        }

        // console.log(await request.formData())

        if (!(data?.project_file)) {
            return json({ success: false, message: "No file found. Field `project_file` required" })
        }

        const { project_file } = data

        console.log(project_file)

        const fileName = `${ulid()}-${project_file.name}`;
        const filePath = path.join(
            // process.cwd(),
            // '../data/uploads',
            FILE_UPLOAD_DIR,
            fileName,
            // `${ulid()}-${project_file.name}`
            // `${crypto.randomUUID()}.${(project_file as Blob).type.split('/')[1]}`
        )
        // check file type
        await fs.writeFile(filePath, Buffer.from(await (project_file as Blob).arrayBuffer()))

        return json({
            success: true,
            message: "File uploaded successfully",
            fileAbsolutePath: process.cwd() + filePath,
            fileRelativePath: filePath,
            fileName: fileName,
        })
    } catch (err: any) {
        return json({ success: false, message: err?.message ?? "Some error" })
    }
}

export const DELETE: RequestHandler = async ({ request }) => {
    const {
        fileName
    } = await request.json() as {
        success: boolean,
        fileAbsolutePath: string
        fileRelativePath: string
        fileName: string
    }

    if (!fileName) {
        return json({ success: false, message: "Invalid Request. Check fileName param" })
    }

    try {

        const filePath = path.join(
            FILE_UPLOAD_DIR,
            fileName,
        )

        await fs.unlink(filePath)

    } catch (err: any) {
        return json({ success: false, message: err?.message ?? "Failed to delete file: " + fileName })
    }

    return json({
        success: true,
        message: "File deleted successfully. File: " + fileName
    })



}