

// Import required libraries
import AdmZip from 'adm-zip';
import * as tar from 'tar';
import fs from 'fs';
import path from 'path';

// File paths
const zipFilePath = path.join('x.zip');
const tarGzFilePath = path.join('y.tar.gz');

// Directories to extract files
const extractDirZip = path.join('x');
const extractDirTar = path.join('y');

// Function to extract .zip files
function extractZip(zipFilePath, extractDir) {
    const zip = new AdmZip(zipFilePath);
    zip.extractAllTo(extractDir, true);
    console.log(`.zip files extracted to ${extractDir}`);
}

// Function to extract .tar.gz files
async function extractTarGz(tarGzFilePath, extractDir) {
    try {
        // Ensure the directory exists
        fs.mkdirSync(extractDir, { recursive: true });

        // Extract tar.gz file
        await tar.extract({
            file: tarGzFilePath,
            cwd: extractDir
        });

        console.log(`.tar.gz files extracted to ${extractDir}`);
    } catch (err) {
        console.error(`Failed to extract .tar.gz file: ${err.message}`);
    }
}

// Extract files
extractTarGz(tarGzFilePath, extractDirTar);
// Extract files
extractZip(zipFilePath, extractDirZip);

// If decompressed directory has only one file, move it to the parent directory
function moveSingleFileToParentDir(directoryPath) {
    const files = fs.readdirSync(directoryPath);
    if (files.length === 1) {
        const fileName = files[0];
        const oldPath = path.join(directoryPath, fileName);
        const newPath = path.join(path.dirname(directoryPath), fileName);
        console.log(`Moving file ${fileName} to ${path.dirname(directoryPath)}`);
        fs.renameSync(oldPath, newPath);

        fs.rmdirSync(directoryPath);
        fs.renameSync(newPath, directoryPath);

    }
}

// Move single files to parent directory
moveSingleFileToParentDir(extractDirZip);
moveSingleFileToParentDir(extractDirTar);