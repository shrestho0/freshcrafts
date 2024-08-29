import { FILE_UPLOAD_DIR } from "$env/static/private";

import * as fsBase from "fs";
import fs from "fs/promises";
import path from 'path';
import AdmZip from 'adm-zip';
import * as tar from 'tar';

export class FilesHelper {


    private static instance: FilesHelper;
    private basePath = FILE_UPLOAD_DIR
    private projectDir = `${this.basePath}/projects`
    private projectCompressedDirName = 'compresed'
    private projectSourceDirName = 'src'
    // private projectEnvFileDirName = 'env'

    private tempFileDir = `${this.basePath}/temp`

    private constructor() {
        this.ensureDirExists(this.basePath);
        this.ensureDirExists(this.projectDir);
        this.ensureDirExists(this.tempFileDir);
    }

    static getInstance() {
        if (!this.instance) {

            this.instance = new FilesHelper();
        }
        return this.instance;
    }

    private ensureDirExists(dirPath: string) {

        if (!fsBase.existsSync(dirPath)) {
            fsBase.mkdirSync(dirPath, { recursive: true });
        }
        if (!fsBase.existsSync(dirPath)) {
            throw new Error(`Failed to create directory: ${dirPath}`);
        }

    }

    async writeProjectSourceFile(projectId: string, fileName: string, content: string | NodeJS.ArrayBufferView, pVersion: number = 1): Promise<{
        fileName: string;
        filePath: string;
        fileAbsPath: string;
    }> {
        // const filePath = `${this.projectDir}/${fileName}`;
        const dirPath = `${this.projectDir}/${projectId}/v${pVersion}/${this.projectCompressedDirName}`;
        this.ensureDirExists(dirPath);
        const filePath = dirPath + '/' + fileName;
        await fs.writeFile(filePath, content,);

        const fileAbsPath = path.join(process.cwd(), filePath)

        return { fileName, filePath, fileAbsPath };
    }

    async writeProjectEnvFile(projectRootAbsPath: string, content: string): Promise<{
        name: string;
        path: string;
        absPath: string;
    }> {
        // const dirPath = `${this.projectDir}/${projectId}/v${pVersion}/${this.projectEnvFileDirName}`;
        const dirPath = `${projectRootAbsPath}`;
        this.ensureDirExists(dirPath);
        const fileName = '.env';
        const filePath = dirPath + '/' + fileName;
        await fs.writeFile(filePath, content);

        const fileAbsPath = path.join(process.cwd(), filePath)

        return { name: fileName, path: filePath, absPath: fileAbsPath };
    }


    async writeToTempFile(_fileName: string, arg1: string | NodeJS.ArrayBufferView): Promise<{
        fileName: string;
        filePath: string;
        fileAbsPath: string;
    }> {


        // space and special characters are not allowed in file names
        const fileName = `${Date.now()}-${_fileName}`.replace(/[^a-zA-Z0-9.-]/g, '_');
        const filePath = `${this.tempFileDir}/${fileName}`;

        await fs.writeFile(filePath, arg1);

        const fileAbsPath = path.join(process.cwd(), filePath)

        return { fileName, filePath, fileAbsPath };
    }

    async moveFileToProjectDir(projectId: string, destfileName: string, srcfilePath: string, pVersion = 1): Promise<{
        fileName: string;
        filePath: string;
        fileAbsPath: string;
    }> {

        // const dirPath = this.projectDir + '/' + projectId + '/' + this.projectCompressedDirName;
        const dirPath = `${this.projectDir}/${projectId}/v${pVersion}/${this.projectCompressedDirName}`;

        this.ensureDirExists(dirPath);
        const destfilePath = dirPath + '/' + destfileName
        await fs.rename(srcfilePath, destfilePath);

        const fileAbsPath = path.join(process.cwd(), destfilePath)

        return { fileName: destfileName, filePath: destfilePath, fileAbsPath: fileAbsPath }

    }


    async decompressProjectSource(absPath: string, projectId: string, pVersion: number = 1): Promise<{
        extracted: string;
        extractedAbs: string;
    }> {
        const ext = path.extname(absPath);
        const dirPath = `${this.projectDir}/${projectId}/v${pVersion}/${this.projectCompressedDirName}`;

        if (ext === '.zip') {
            const extracted = this.extractZip(absPath, dirPath) ?? '';
            const extractedAbs = extracted ? path.join(process.cwd(), extracted) : '';
            return { extracted, extractedAbs }
        } else if (ext == '.gz' || ext == '.tar' || ext == '.xz') {
            const extracted = await this.extractTarGz(absPath, dirPath) ?? '';
            const extractedAbs = extracted ? path.join(process.cwd(), extracted) : '';
            return { extracted, extractedAbs }
        }
        return { extracted: '', extractedAbs: '' }

    }

    async deleteFile(filePath: string) {
        await fs.unlink(filePath);
    }


    async deleteProjectFiles(projectId: string) {
        const projectDir = `${this.projectDir}/${projectId}`;
        await fs.rm(projectDir, { recursive: true, force: true });
    }


    // Function to extract .zip files
    private extractZip(zipFilePath: string, extractDir: string) {
        try {

            extractDir = extractDir.replace(this.projectCompressedDirName, this.projectSourceDirName)
            const zip = new AdmZip(zipFilePath);
            zip.extractAllTo(extractDir, true);
            this.moveSingleFileToParentDir(extractDir);
            console.log(`.zip files extracted to ${extractDir}`);
            return extractDir;
        } catch (err: any) {
            console.error(`Failed to extract .zip file: ${err?.message}`);
        }
        return null;
    }

    // Function to extract .tar.gz files
    private async extractTarGz(tarGzFilePath: string, extractDir: string) {
        try {

            extractDir = extractDir.replace(this.projectCompressedDirName, this.projectSourceDirName)
            // Ensure the directory exists
            fsBase.mkdirSync(extractDir, { recursive: true });
            // Extract tar.gz file
            await tar.extract({
                file: tarGzFilePath,
                cwd: extractDir
            });

            this.moveSingleFileToParentDir(extractDir);
            console.log(`.tar.gz files extracted to ${extractDir}`);
            return extractDir;
        } catch (err: any) {
            console.error(`Failed to extract .tar.gz file: ${err?.message}`);
        }
        return null;
    }




    // If decompressed directory has only one file, move it to the parent directory
    private moveSingleFileToParentDir(directoryPath: string) {
        const files = fsBase.readdirSync(directoryPath);
        if (files.length === 1) {
            const fileName = files[0];
            const oldPath = path.join(directoryPath, fileName);
            const newPath = path.join(path.dirname(directoryPath), fileName);
            console.log(`Moving file ${fileName} to ${path.dirname(directoryPath)}`);
            fsBase.renameSync(oldPath, newPath);
            fsBase.rmdirSync(directoryPath);
            fsBase.renameSync(newPath, directoryPath);
        }

        return directoryPath;
    }



    async listFilesRecursive(filesDirAbsPath: string) {
        let iota = 0;
        let total_levels = 0;

        async function getDirectoryStructure(dirPath: string, level = 0, maxDepth = 5) {
            const structure: any[] = [];

            // Stop recursion if max depth is reached
            if (level >= maxDepth) {
                return structure;
            }

            const ignore = [
                ".git",
                ".gitignore",
                ".DS_Store",
                "node_modules",
                "package-lock.json",
                "yarn.lock",
                "README.md",
                ".svelte-kit",
                ".vscode",
                "__pycache__",
                ".mvn",
                "target",
                ".idea",
            ]

            // list directories and files
            // ignore the list

            const files = await fs.readdir(dirPath);
            for (const file of files) {
                if (ignore.includes(file)) {
                    continue;
                }

                const filePath = path.join(dirPath, file);
                const stats = await fs.stat(filePath);
                const isDir = stats.isDirectory();
                const isFile = stats.isFile();

                if (isDir) {
                    const children = await getDirectoryStructure(filePath, level + 1, maxDepth);
                    structure.push({
                        text: file, children, isDir, disabled: !isDir, isFile, level, id: iota++
                    });
                } else if (isFile) {
                    structure.push({ text: file, isDir, isFile, disabled: !isDir, level, id: iota++ });
                }

                if (level > total_levels) {
                    total_levels = level;
                }



            }
            return structure;
        }

        const structure = await getDirectoryStructure(filesDirAbsPath);
        return {
            total_levels,
            iota: iota--,
            structure,
        };

    }



    joinPath(...paths: string[]) {
        return path.join(...paths);
    }

}