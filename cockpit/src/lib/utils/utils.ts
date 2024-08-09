import { browser } from '$app/environment';
import type { EngineCommonResponseDto } from '@/types/dtos';
import type { HttpMethod } from '@sveltejs/kit';
import { decodeTime } from 'ulid';
// Client Side Code Only


export function toTitleCase(str: string) {
	// https://stackoverflow.com/questions/196972/convert-string-to-title-case-with-javascript
	return str.toLowerCase().replace(/\b\w/g, (s) => s.toUpperCase());
}

export async function fetchDataSelf(body: any, method: HttpMethod, fallback: any = null) {
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
	['DBMYSQL', '/databases/mysql']
	// more will be added later
]);

export const hintToSSEUrlMap = new Map([
	['DBMYSQL', '/sse/databases/mysql']
	// more will be added later
]);

// import and export all from db-stuff.ts
export * from './db-stuff';
