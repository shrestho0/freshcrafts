<script lang="ts">
import { browser } from '$app/environment';
import { goto } from '$app/navigation';
import { page } from '$app/stores';
import type { SystemwideNotification } from '@/types/entities';
import { source } from 'sveltekit-sse';

export let notificationStuff;

let pathname = $page.url.pathname;

const notiSSEUrl = `/sse/notification`;
let value = source(notiSSEUrl).select('message');
if (browser && value) {
	value.subscribe(async (data: string) => {
		console.log(data);
		if (data) {
			const notification: SystemwideNotification = JSON.parse(data);
			console.log('Notification', notification);
			// if (!notification.markedAsRead) {

			if (notificationStuff) {
				notificationStuff.notifications.push(notification);
				notificationStuff.hasNew = true;
				notificationStuff.notifications = [...notificationStuff.notifications, notification];
			}

			if (notification.actionHints?.startsWith('REDIRECT_PROJECTS_')) {
				const [_, __, proj_id] = notification.actionHints.split('_');
				if (proj_id && pathname.startsWith(`/projects/${proj_id}`)) {
					console.log('needda redirect');

					const res = await fetch(`/sse/notifications`, {
						method: 'DELETE',
						headers: {
							'Content-Type': 'application/json'
						}
					})
						.then((res) => res.json())
						.catch((e) => console.error(e));
					console.log('noti delete res', res);
					goto('/projects/all');
				}
				if (pathname.startsWith('/projects')) {
					console.log('pathname', pathname);
				}
			}
		}
	});
}
</script>
