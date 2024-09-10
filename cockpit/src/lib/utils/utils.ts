import { browser } from '$app/environment';
import type { EngineCommonResponseDto } from '@/types/dtos';
import { decodeTime } from '@/utils/ulid/';
// Client Side Code Only


export function toTitleCase(str: string) {
	// https://stackoverflow.com/questions/196972/convert-string-to-title-case-with-javascript
	return str.toLowerCase().replace(/\b\w/g, (s) => s.toUpperCase());
}

export async function fetchDataSelf(body: any, method: any, fallback: any = null) {
	if (!browser) return;

	const res: EngineCommonResponseDto = await fetch('', {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify(body)
	})
		.then((res) => res.json())
		.catch((err) => {
			console.error(err);
			return fallback;
		});

	return res;
}

export async function delay(ms: number = 500) {
	await new Promise((resolve) => setTimeout(resolve, ms));
}

export function ulidToDate(ulidX: string | undefined) {
	if (!ulidX) return undefined;

	try {
		const unixTime = decodeTime(ulidX);
		return new Date(unixTime);
	} catch (e) {
		console.error(e);
		return undefined;
	}
}

export function ISOToDate(ISODate: string | undefined) {
	if (!ISODate) return undefined;

	try {
		return new Date(ISODate);
	} catch (e) {
		console.error(e);
		return undefined;
	}
}

// date to sometimes ago
export function humanizedTimeDifference(date: Date | undefined) {
	//
	if (!date) return 'Some time ago';

	const diff = Date.now() - date.getTime();
	const seconds = Math.floor(diff / 1000);
	const minutes = Math.floor(seconds / 60);
	const hours = Math.floor(minutes / 60);
	const days = Math.floor(hours / 24);

	if (days > 0) return days + 'd ago';
	if (hours > 0) return hours + 'h ago';
	if (minutes > 0) return minutes + 'm ago';
	if (seconds > 0) return seconds + 's ago';
	return 'Just now';
}

export const hintToWebUrlMap = new Map([
	['DBMYSQL', '/databases/mysql'],
	['DBMONGO', '/databases/mongodb'],
	['DBPOSTGRES', '/databases/postgres'],


	// more will be added later
]);

export const hintToSSEUrlMap = new Map([
	['DBMYSQL', '/sse/databases/mysql']
	// more will be added later
]);

// import and export all from db-stuff.ts
export * from './db-stuff';


export class EnvVarsUtil {
	/**
	 * Utility class to handle environment variables
	 * Part of this code has been taken from motdotla/dotenv (https://github.com/motdotla/dotenv) 
	 * which is main repository for dotenv npm package (https://www.npmjs.com/package/dotenv)
	 * Copyright (c) 2015, Scott Motte
	 * All rights reserved.
	 * @credit motdotla<https://github.com/motdotla>
	 * @license https://github.com/motdotla/dotenv/blob/master/LICENSE
	 */

	private static LINE = /(?:^|^)\s*(?:export\s+)?([\w.-]+)(?:\s*=\s*?|:\s+?)(\s*'(?:\\'|[^'])*'|\s*"(?:\\"|[^"])*"|\s*`(?:\\`|[^`])*`|[^#\r\n]+)?\s*(?:#.*)?(?:$|$)/mg

	// Parse src into an Object
	static parse(src: string): {
		[key: string]: string
	} {
		const obj: {
			[key: string]: string
		} = {}

		// Convert buffer to string
		let lines = src.toString()

		// Convert line breaks to same format
		lines = lines.replace(/\r\n?/mg, '\n')

		let match
		while ((match = this.LINE.exec(lines)) != null) {
			const key = match[1]

			// Default undefined or null to empty string
			let value = (match[2] || '')

			// Remove whitespace
			value = value.trim()

			// Check if double quoted
			const maybeQuote = value[0]

			// Remove surrounding quotes
			value = value.replace(/^(['"`])([\s\S]*)\1$/mg, '$2')

			// Expand newlines if double quoted
			if (maybeQuote === '"') {
				value = value.replace(/\\n/g, '\n')
				value = value.replace(/\\r/g, '\r')
			}

			// Add to object
			obj[key] = value
		}

		return obj
	}

	// Stringify object into src
	static stringify(obj: {
		[key: string]: string
	}) {
		let src = ''
		for (const key in obj) {
			const value = obj[key]
			src += `${key}=${value}\n`
		}
		return src
	}



	static parseArr(content: string): string[] {
		const obj = this.parse(content)
		const arr = []
		for (const key in obj) {
			const value = obj[key]
			arr.push(`${key}=${value}`)
		}

		return arr

	}


	/// Project specific
	static kvToContent(kv: { key: string; value: string }[]) {
		return kv
			.filter(({ key, value }) => key && value)
			.map(({ key, value }) => `${key}=${value}`)
			.join('\n');
	}

	static contentToKV(content: string) {
		const x = EnvVarsUtil.parse(content);
		return Object.entries(x).map(([key, value]) => ({ key, value }));
	}


}

// Project Utils