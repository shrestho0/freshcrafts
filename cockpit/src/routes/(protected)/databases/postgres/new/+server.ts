import { BackendEndpoints } from '@/backend-endpoints.js';
import { json } from '@sveltejs/kit';
import { ulid } from 'ulid';

export const POST = async ({ request }) => {
	const data = await request.json();
	console.log(data);

	// for (let i = 0; i < 1000000; i++) {
	//     console.log(i);
	// }

	const res = await fetch(BackendEndpoints.POSTGRES_NEW, {
		method: 'POST',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ ...data })
	}).then((res) => res.json());

	console.log(res);

	return json(res);
};
