<script lang="ts">
import { browser } from '$app/environment';
import { invalidateAll } from '$app/navigation';
import { page } from '$app/stores';
import { source } from 'sveltekit-sse';

if (browser) {
	// (data?.payload?.status == DBMysqlStatus.REQUESTED ||
	// 	data?.payload?.status == DBMysqlStatus.PENDING_DELETE)
	console.log('sse hit hobe');
	const { proj_id } = $page.params;

	const sse_url = `/sse/notification`;

	const value = source(sse_url).select('message');
	if (value) {
		value.subscribe((noti) => {
			console.log('notif', noti);
		});
		// setTimeout(() => {
		//     const v = value
		// 	invalidateAll();
		// }, 1000);
		// console.log('Value at /project/[id]', );
	}
}
</script>

<slot />
