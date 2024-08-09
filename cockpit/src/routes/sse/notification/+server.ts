import type { SystemwideNotification } from '@/types/entities';
import { hintToSSEUrlMap } from '@/utils/utils';
import { json, type RequestHandler } from '@sveltejs/kit';
import { writable } from 'svelte/store';
import { produce } from 'sveltekit-sse';

// /**
//  * @param {number} milliseconds
//  * @returns
//  */
// function delay(milliseconds: number | undefined) {
//     return new Promise(function run(resolve) {
//         setTimeout(resolve, milliseconds)
//     })
// }

const NotificationStore = writable<SystemwideNotification | undefined>();

export function POST() {
	return produce(async function start({ emit }) {
		const unsubscribe = NotificationStore.subscribe(async (data) => {
			if (data) {
				const { error } = emit('message', `${JSON.stringify(data)}`);

				if (error) {
					return unsubscribe();
				}
			}
			// await delay(1000)
		});
	});
}

export const GET: RequestHandler = async ({ request }) => {
	console.log('[DEBUG]: GET Request on Notification SSE\nData: ', '\n');

	return json({
		success: true,
		message: 'ss'
	});
};

export const PATCH: RequestHandler = async ({ request, fetch }) => {
	let errorPayload: any;
	try {
		console.log('[DEBUG]: PATCH Request on Notification');

		const data = (await request.json()) as SystemwideNotification;
		errorPayload = data;

		console.warn(
			'[DEBUG]: PATCH Request on Notification SSE\nData: ',
			JSON.stringify(data, null, 2),
			'\n'
		);

		// check if all keys are present
		if (!data.id || !data.message || !data.actionHints || !data.type)
			throw new Error('Invalid payload');

		const [action, locationHint, id] = data?.actionHints?.split('_');
		console.warn(
			'[DEBUG]: PATCH Request on Notification SSE\nAction: ',
			action,
			'\nLocation Hint: ',
			locationHint,
			'\nID: ',
			id
		);

		if (!id || !locationHint || !action) throw new Error('Invalid actionUrlHints');

		if (!data.message) throw new Error('Invalid message.');

		// set notification
		NotificationStore.set(data);

		// Send patch request based on actionHint
		const url = hintToSSEUrlMap.get(locationHint);
		if (!url) throw new Error('Unknown locationHint');
		console.log('${url}/${id}', `${url}/${id}`);
		const res = await fetch(`${url}/${id}`, {
			method: 'PATCH',
			headers: {
				'Content-Type': 'application/json'
			},
			body: JSON.stringify(data)
		})
			.then((res) => res.json())
			.catch((err) => {
				throw new Error(err);
			});
		console.warn(
			`[DEBUG]: Requested ${locationHint}, and received: `,
			JSON.stringify(res, null, 2),
			'\n'
		);
		// NotificationStore.getStore().set(JSON.parse(message))
		return json({ success: true, message: 'Notification sent' });
	} catch (err: any) {
		return json({
			success: false,
			message: err?.message ?? 'Some error occured',
			errorPayload
		});
	}
};

export const DELETE: RequestHandler = async ({ request }) => {
	NotificationStore.set(undefined);
	return json({
		success: true
	});
};
