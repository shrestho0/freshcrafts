<script lang="ts">
import { browser } from '$app/environment';
import type { EngineCommonResponseDto } from '@/types/dtos';
import type { AIChatMessage } from '@/types/entities';
import { AIChatCommands } from '@/types/enums';
import { Button, InlineLoading, TextArea, TextInput } from 'carbon-components-svelte';
import { Label } from 'carbon-icons-svelte';
import { afterUpdate, onMount, setContext } from 'svelte';
import { slide } from 'svelte/transition';
import SvelteMarkdown from 'svelte-markdown';
import { page } from '$app/stores';
import { invalidateAll } from '$app/navigation';
export let open: boolean;
let chatStarted = false;

let messages: AIChatMessage[] = [];
$: console.log('messages', messages);
let loading = false;
let loadingMessage = '';
let showCloseChatOptions = false;
let hasError = false;
let errorMessage = '';

let chatContainer: HTMLDivElement;

$: {
	if (open && !chatStarted) {
		initChat();
		chatStarted = true;
	} else if (open && chatStarted) {
		console.log('chat already started. getting messages');
		getChatMessage();
	}
}

function ensureContainerBottom() {
	if (chatContainer) {
		chatContainer.scrollTo({ top: 1000000, behavior: 'smooth' });
	}
}
async function initChat() {
	loading = true;
	loadingMessage = 'Initializing chat...';
	const res = await fetch('/chat', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ command: AIChatCommands.INIT_CHAT })
	}).then((res) => res.json());

	if (res.success) {
		messages = res?.payload;
		// ensureContainerBottom();
	} else {
		hasError = true;
		errorMessage = res?.message ?? 'An error occurred while fetching messages.';
	}

	loading = false;
	loadingMessage = '';
}

async function getChatMessage() {
	loading = true;
	loadingMessage = 'Fetching messages...';
	const res: EngineCommonResponseDto = await fetch('/chat', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ command: AIChatCommands.GET_MESSAGES })
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	if (res.success) {
		messages = res?.payload;
		// ensureContainerBottom();
	} else {
		hasError = true;
		errorMessage = res?.message ?? 'An error occurred while fetching messages.';
	}
	loading = false;
	loadingMessage = '';
}

let userMessage = '';
let inputsDisabled = false;
async function sendMessage() {
	loading = true;
	loadingMessage = 'Getting response...';
	inputsDisabled = true;
	const res: EngineCommonResponseDto = await fetch('/chat', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({ command: AIChatCommands.SEND_MESSAGE, data: userMessage.trim() })
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	userMessage = '';
	if (res.success) {
		messages = res?.payload;
		// ensureContainerBottom();
	} else {
		hasError = true;
		errorMessage = res?.message ?? 'An error occurred while fetching messages.';
	}
	loading = false;
	loadingMessage = '';
	inputsDisabled = false;
}
async function closeChat(save: boolean) {
	loading = true;
	messages = [];
	loadingMessage = 'Closing chat...';

	const res: EngineCommonResponseDto = await fetch('/chat', {
		method: 'PATCH',
		headers: {
			'Content-Type': 'application/json'
		},
		body: JSON.stringify({
			command: save ? AIChatCommands.CLOSE_CHAT_WITH_SYNC : AIChatCommands.CLOSE_CHAT_WITHOUT_SYNC
		})
	})
		.then((res) => res.json())
		.catch((e) => console.error(e));

	if (res?.success) {
		open = false;
		chatStarted = false;
		showCloseChatOptions = false;
		messages = [];
		// if (save) {
		// 	localStorage.setItem('chat_saved', 'true');
		// }
	} else {
		hasError = true;
		errorMessage = res?.message ?? 'An error occurred while closing chat.';
	}
}

afterUpdate(() => {
	ensureContainerBottom();
});
onMount(() => {
	ensureContainerBottom();
});
</script>

{#if open}
	<div
		transition:slide={{ duration: 300, axis: 'x' }}
		class="chat-container fixed h-screen w-[500px] z-[99999999] top-12 pb-12 right-0 bg-gray-200 flex flex-col items-center justify-between"
	>
		<div
			class="top w-full pl-4 mb-3 flex items-center justify-between"
			style="border-bottom:2px solid blue;"
		>
			<h1 class="text-2xl">FreshChat</h1>
			<Button
				on:click={() => {
					showCloseChatOptions = true;
				}}>Close</Button
			>
		</div>

		{#if hasError}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<Label class="w-5 h-5" />
				<h2>{errorMessage}</h2>
			</div>
		{:else if loading && messages?.length === 0}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<div class="animate-spin w-5 h-5 border-t-2 border-b-2 border-gray-900"></div>
				<h2>{loadingMessage}</h2>
			</div>
		{:else if showCloseChatOptions}
			<div class="flex items-center w-full flex-col gap-3 justify-center items-center">
				<!-- save chat and close-->
				{#if messages?.length > 1}
					<Button class="w-full" kind="tertiary" on:click={async () => await closeChat(true)}
						>Save and close</Button
					>
				{/if}
				<Button class="w-full" kind="danger-tertiary" on:click={async () => await closeChat(false)}
					>Just close it</Button
				>
				<Button
					class="w-full"
					on:click={() => {
						showCloseChatOptions = false;
					}}>Continue Chat</Button
				>
				<!-- just close it -->
			</div>
		{:else if messages.length > 0}
			<div class="w-full h-[100%]" style="overflow-y:auto;" bind:this={chatContainer}>
				{#each messages as message}
					<div
						title={message.timestamp}
						class="p-2 w-full flex items-center gap-2 {message.role === 'user'
							? 'justify-end'
							: 'justify-start'} "
					>
						<div
							class="p-2 bg-gray-300 rounded-lg {message.role === 'user'
								? ' bg-[var(--cds-interactive-01)] text-white '
								: ' bg-gray-300 '} break-all"
						>
							<SvelteMarkdown source={message.content} />
							<!-- {message.content} -->
						</div>
					</div>
				{/each}
			</div>
		{:else}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<Label class="w-5 h-5" />
				<h2>No messages yet.</h2>
			</div>
		{/if}

		<form on:submit|preventDefault={sendMessage} class="bottom p-4 w-full flex flex-col gap-2">
			<TextInput
				placeholder="type your message"
				bind:value={userMessage}
				disabled={inputsDisabled}
			/>
			<div class="flex items-center justify-between">
				<h2>
					{#if loading && messages?.length > 0}
						<InlineLoading description={loadingMessage} status="active" />
					{:else}
						Type your message.
					{/if}
				</h2>
				<Button type="submit" disabled={inputsDisabled}>Send</Button>
			</div>
		</form>
	</div>
{/if}
