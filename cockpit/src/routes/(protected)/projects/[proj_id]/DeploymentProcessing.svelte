<script lang="ts">
import { InlineLoading } from 'carbon-components-svelte';
import { CheckCircle, XCircle } from 'lucide-svelte';
// let messages = currentDeployment?.partialDeploymentMsg.split('\n') ?? [];
export let messages: string[];
messages = messages.map((msg: string) => msg.trim());
messages = messages.filter((msg: string) => msg.trim() !== '');
// remove duplicate
messages = messages.filter((msg: string, idx: number) => {
	if (idx === 0) return true;
	return msg !== messages[idx - 1];
});

let filtered = [];

for (let i = 0; i < messages.length; i++) {
	if (
		messages[i].startsWith('[RUNNING]') &&
		i + 1 < messages.length &&
		messages[i + 1].match(/\[SUCCESS|FAILURE\]/)
	) {
		continue;
	}
	filtered.push(messages[i]);
}
</script>

<div class="flex flex-col space-y-2">
	{#each filtered as message, idx}
		<InlineLoading
			description={message?.replace(/\[.*\]/, '')}
			status={message.startsWith('[RUNNING]')
				? 'active'
				: message.startsWith('[SUCCESS]')
					? 'finished'
					: message.startsWith('[FAILURE]')
						? 'error'
						: 'inactive'}
		/>
	{/each}
</div>
