// export const AllowedFileTypes = [
//     { extension: ".tar", mimeType: "application/x-tar" },
//     { extension: ".tar.gz", mimeType: "application/gzip" },
//     { extension: ".tar.bz2", mimeType: "application/x-bzip2" },
//     { extension: ".zip", mimeType: "application/zip" },
// ];

export const AllowedFileMimeTypes = [
	'application/x-tar',
	'application/gzip',
	// 'application/x-bzip2',
	'application/zip',
	// 'application/x-xz',
];

export const AllowedFileExtensions = ['.tar', '.tar.gz', '.zip', '.gz',];

export const MaxFileSize = 1024 * 1024 * 100; // 100MB
