/**
 * We'll push data here and sse will send to client
 */

import { writable } from 'svelte/store';

type SomeSSEStoreData = {
	message: string;
	time: number;
};

// Goes to log
const SomeSSEStoreDataBus: SomeSSEStoreData[] = [];

const SomeSSEStore = writable<SomeSSEStoreData>();

SomeSSEStore.subscribe((value) => {
	console.log('Some SSE Store Updated');
	// SomeSSEStoreDataBus.push(value);
});

export default SomeSSEStore;
export type { SomeSSEStoreData };
