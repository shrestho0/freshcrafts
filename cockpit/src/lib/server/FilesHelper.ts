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
    private projectProdDirName = 'prod'
    private projectLogsDirName = 'logs'
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

    async writeProjectEnvFile(fileDirPath: string, rootDirPath: string, content: string): Promise<{
        name: string;
        path: string;
        absPath: string;
    }> {
        // const dirPath = `${this.projectDir}/${projectId}/v${pVersion}/${this.projectEnvFileDirName}`;
        // const dirPath = `${projectRootAbsPath}`;
        // this.ensureDirExists(projectRoot);

        const fileName = '.env';
        // const filePath = dirPath + '/' + fileName;
        const filePath = path.join(fileDirPath, rootDirPath, fileName);

        await fs.writeFile(filePath, content);

        const fileAbsPath = path.join(process.cwd(), filePath)

        return { name: fileName, path: filePath, absPath: fileAbsPath };
    }

    async writeProjectEcoSystemFile(projectId: string, pVersion: number = 1, projectName: string, projectDomain: string, buildDirPath: string, envFilePath: string): Promise<{
        ecoSystemFilePath: string;
        ecoSystemFileAbsPath: string,
        outLogFileAbsPath: string,
        errorLogFileAbsPath: string,
        nginxConfFilePath: string;
        nginxConfFileAbsPath: string;
        logsDir: string;
    }> {
        // project/id[]/v[]/prod
        // project/id[]/v[]/prod/logs
        const filesDir = `${this.projectDir}/${projectId}/v${pVersion}`;
        const prodDir = path.join(filesDir, this.projectProdDirName);
        this.ensureDirExists(prodDir);

        const logsDir = path.join(prodDir, this.projectLogsDirName);
        this.ensureDirExists(logsDir);

        const outFilePath = path.join(logsDir, 'out.log');
        const errorFilePath = path.join(logsDir, 'error.log');
        const outLogFileAbsPath = path.join(process.cwd(), outFilePath);
        const errorLogFileAbsPath = path.join(process.cwd(), errorFilePath);

        const fileName = 'ecosystem.config.js';
        const filePath = path.join(prodDir, fileName);
        const content = this.getEcoSystemTemplateFileContent()
            .replace('{PROJECT_NAME}', projectName)
            .replace('{INSTANCES}', '1')
            .replace('{SCRIPT_PATH}', path.join(buildDirPath, 'index.js'))
            .replace('{ENV_FILE_PATH}', envFilePath)
            .replace('{ERROR_LOG_FILE_PATH}', errorLogFileAbsPath)
            .replace('{OUT_LOG_FILE_PATH}', outLogFileAbsPath)
            .replace('{MAX_MEMORY_RESTART}', '100M')
            .replace('{RESTART_DELAY}', '1000')
            .replace('{SOURCE_MAP_SUPPORT}', 'true')
            .replace('{CRON_RESTART}', '0 0 * * *')
            .replace('{AUTORESTART}', 'true')
            .replace('{PORT}', '{PORT}')
            .replace('{HOST}', '0.0.0.0')
            .replace('{ORIGIN}', `https://${projectDomain}`);

        await fs.writeFile(filePath, content);

        const fileAbsPath = path.join(process.cwd(), filePath)

        // write nginx config file
        const nginxConfFileName = `${projectId}.conf`;
        const nginxConfFilePath = path.join(prodDir, nginxConfFileName);
        const nginxConfContent = this.nginxConfigTemplateContent
            .replace('{SERVER_NAME}', projectDomain)
            .replace('{PORT}', '{PORT}');
        await fs.writeFile(nginxConfFilePath, nginxConfContent);

        const nginxConfFileAbsPath = path.join(process.cwd(), nginxConfFilePath);


        return {
            ecoSystemFilePath: filePath,
            ecoSystemFileAbsPath: fileAbsPath,
            logsDir: path.join(filesDir, this.projectLogsDirName),
            outLogFileAbsPath,
            errorLogFileAbsPath,
            nginxConfFilePath,
            nginxConfFileAbsPath,
        };
    }





    async writeToTempFile(_fileName: string, arg1: string | NodeJS.ArrayBufferView): Promise<{
        fileName: string;
        filePath: string;
        fileAbsPath: string;
    }> {


        // space and special characters are not allowed in file names
        const fileName = `${Date.now()}-${_fileName}`.replace(/[^a-zA-Z0-9.-]/g, '_');
        const filePath = `${this.tempFileDir}/${fileName}`;

        this.ensureDirExists(path.dirname(filePath));

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


    getEcoSystemTemplateFileContent() {
        return `
        // This file is auto generated

        module.exports = {
            apps: [
            {
                name: "{PROJECT_NAME}",
                instances: {INSTANCES},
                script: "{SCRIPT_PATH}",
                interpreter: "/usr/bin/node",
                interpreter_args: "--env-file={ENV_FILE_PATH}",
                error_file: "{ERROR_LOG_FILE_PATH}",
                out_file: "{OUT_LOG_FILE_PATH}",
                log_date_format: "YYYY-MM-DD HH:mm:ss",
                max_memory_restart: "{MAX_MEMORY_RESTART}",
                restart_delay: {RESTART_DELAY},
                source_map_support: {SOURCE_MAP_SUPPORT},
                cron_restart: "{CRON_RESTART}",
                autorestart: {AUTORESTART},
                env: {
                    "PORT": {PORT},
                    "HOST": "{HOST}",
                    "ORIGIN": "{ORIGIN}",
                }
            }]
            }
        `
    }

    get nginxConfigTemplateContent() {
        return `

        server {
            listen 80;
            server_name {SERVER_NAME} ;
        
            location / {
                        
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header x-forwarded-for $proxy_add_x_forwarded_for;
                proxy_set_header x-forwarded-proto $scheme;
        
                client_max_body_size 100M;
        
                proxy_pass http://localhost:{PORT};
            }
        }
        `
    }

    async readFile(filePath: string) {
        return await fs.readFile(filePath, 'utf-8');
    }

}