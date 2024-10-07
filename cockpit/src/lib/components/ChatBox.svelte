<!-- <script lang="ts">
	import type { AIChatMessage } from "@/types/entities";
	import { AIChatCommands } from "@/types/enums";
	import {
		Button,
		Checkbox,
		InlineLoading,
		TextInput,
	} from "carbon-components-svelte";
	import { onMount } from "svelte";
	import { slide } from "svelte/transition";

	// States
	export let open: boolean;
	let showCloseChatOptions = false;
	let chatState = {
		status: "idle",
		description: "",
		messages: [],
		inputsDisabled: false,

		userMessage: "",
		includeContext: true,
	} as {
		status: "idle" | "initialized" | "loading" | "error" | "close-options";
		messages: AIChatMessage[];

		description: string;
		inputsDisabled: boolean;

		// Input Bindings
		userMessage: string;
		includeContext: boolean;
	};

	async function getState() {
		const res = await fetch("/chat", {
			method: "PATCH",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ command: AIChatCommands.GET_STATE }),
		})
			.then((res) => res.json())
			.catch((err) => {
				console.error(err);
				chatState.status = "error";
				chatState.description = "Failed to load chat bot";
			});

		if (res.chatInit == true) {
			chatState.status = "initialized";
			chatState.messages = res.messages;
		}else{

		}
	}

	async function sendMessage() {}
	async function closeChat(save: boolean) {
		if (save) {
			console.log("Save and close");
		} else {
			console.log("Just close");
		}

		chatState.status = "idle";
		open = false;
	}

	onMount(async () => {
		chatState.status = "loading";
		chatState.description = "Loading chat bot";
		console.log("onMount");

		// chatState.status = "loading";
		// chatState.description = "Loading chat bot";
		// await getState();
		// chatState.status = "initialized";
	});
</script>

{#if open}
	<div
		transition:slide={{ duration: 300, axis: "x" }}
		class="chat-container fixed h-screen w-full sm:w-[40%] z-[99999999] top-12 pb-12 right-0 bg-gray-200 flex flex-col items-center justify-between"
	>
		{JSON.stringify(chatState, null, 2)}
		<div
			class="top w-full pl-4 mb-3 flex items-center justify-between"
			style="border-bottom:2px solid blue;"
		>
			<h1 class="text-2xl">FreshChat</h1>
			<Button
				on:click={() => {
					chatState.status = "close-options";
				}}>Close</Button
			>
		</div>

		{#if chatState.status == "close-options"}
			<div
				class="flex items-center w-full flex-col gap-3 justify-center items-center"
			>
				{#if chatState.messages?.length > 1}
					<Button
						class="w-full"
						kind="tertiary"
						on:click={async () => await closeChat(true)}
						>Save and close</Button
					>
				{/if}
				<Button
					class="w-full"
					kind="danger-tertiary"
					on:click={async () => await closeChat(false)}
					>Just close it</Button
				>
				<Button
					class="w-full"
					on:click={() => {
						showCloseChatOptions = false;
					}}>Continue Chat</Button
				>
			</div>
		{/if}

		<form
			on:submit|preventDefault={sendMessage}
			class="bottom p-4 w-full flex flex-col gap-2"
		>
			<div>
				<TextInput
					placeholder="type your message"
					bind:value={chatState.userMessage}
					disabled={chatState.inputsDisabled}
				/>
				<div class="flex gap-2 items-center">
					Include context
					<Checkbox
						bind:checked={chatState.includeContext}
						disabled={chatState.inputsDisabled}
					/>
				</div>
			</div>
			<div class="flex items-center justify-between">
				<h2>
					{#if chatState.status == "loading" && chatState.messages?.length > 0}
						<InlineLoading
							description={chatState.description}
							status="active"
						/>
					{:else}
						Type your message.
					{/if}
				</h2>
				<Button type="submit" disabled={chatState.inputsDisabled}
					>Send</Button
				>
			</div>
		</form>
	</div>
{/if} -->

<script lang="ts">
	import type { EngineCommonResponseDto } from "@/types/dtos";
	import type { AIChatMessage } from "@/types/entities";
	import { AIChatCommands } from "@/types/enums";
	import {
		Button,
		InlineLoading,
		TextArea,
		TextInput,
		Checkbox,
	} from "carbon-components-svelte";
	import { afterUpdate, onMount, setContext } from "svelte";
	import { slide } from "svelte/transition";
	import CarbonLabel from "@/ui/icons/CarbonLabel.svelte";

	import SvelteMarkdown from "svelte-markdown";
	import MdCodeBlock from "./MDCodeBlock.svelte";

	export let open: boolean;
	// let chatStarted = localStorage.getItem("chat_started") === "true";

	let chatStarted = false;

	let messages: AIChatMessage[] = [];
	$: console.log("messages", messages);
	let loading = false;
	let loadingMessage = "";
	let showCloseChatOptions = false;
	let hasError = false;
	let errorMessage = "";

	let chatContainer: HTMLDivElement;

	const svelteMarkdownRenderers = {
		code: MdCodeBlock,
	};

	$: {
		if (!open) {
			// ignore
		} else if (open && !chatStarted) {
			initChat();
			chatStarted = true;
		} else if (open && chatStarted) {
			console.log("chat already started. getting messages");
			getChatMessage();
		}
	}

	function ensureContainerBottom() {
		if (chatContainer) {
			chatContainer.scrollTo({ top: 1000000, behavior: "smooth" });
		}
	}

	async function initChat() {
		loading = true;
		loadingMessage = "Initializing chat...";
		const res = await fetch("/chat", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ command: AIChatCommands.INIT_CHAT }),
		}).then((res) => res.json());

		if (res.success) {
			messages = res?.payload;
			// ensureContainerBottom();
		} else {
			hasError = true;
			errorMessage =
				res?.message ?? "An error occurred while fetching messages.";
		}

		loading = false;
		loadingMessage = "";
	}

	async function getChatMessage() {
		loading = true;
		loadingMessage = "Fetching messages...";
		const res: EngineCommonResponseDto = await fetch("/chat", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({ command: AIChatCommands.GET_MESSAGES }),
		})
			.then((res) => res.json())
			.catch((e) => {
				// console.error(e);
				return {
					success: false,
					message: "An error occurred while fetching messages.",
				};
			});

		if (res?.success) {
			messages = res?.payload;
		} else {
			hasError = true;
			errorMessage =
				res?.message ?? "An error occurred while fetching messages.";
		}
		loading = false;
		loadingMessage = "";
		// showCloseChatOptions = true; // no idea where it came from
	}

	let userMessage = "";
	let inputsDisabled = false;
	let includeContext = true;
	async function sendMessage() {
		loading = true;
		loadingMessage = "Getting response...";
		inputsDisabled = true;
		const res: EngineCommonResponseDto = await fetch("/chat", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				command: AIChatCommands.SEND_MESSAGE,
				data: userMessage.trim(),
				includeContext,
			}),
		})
			.then((res) => res.json())
			.catch((e) => console.error(e));

		userMessage = "";
		if (res.success) {
			messages = res?.payload;
			// ensureContainerBottom();
		} else {
			hasError = true;
			errorMessage =
				res?.message ?? "An error occurred while fetching messages.";
		}
		loading = false;
		loadingMessage = "";
		inputsDisabled = false;
	}
	async function closeChat(save: boolean) {
		loading = true;
		messages = [];
		loadingMessage = "Closing chat...";

		const res: EngineCommonResponseDto = await fetch("/chat", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				command: save
					? AIChatCommands.CLOSE_CHAT_WITH_SYNC
					: AIChatCommands.CLOSE_CHAT_WITHOUT_SYNC,
			}),
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
			errorMessage =
				res?.message ?? "An error occurred while closing chat.";
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
		transition:slide={{ duration: 300, axis: "x" }}
		class="chat-container fixed h-screen w-full sm:w-[40%] z-[99999999] top-12 pb-12 right-0 bg-[var(--cds-ui-01)] flex flex-col items-center justify-between"
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

		{#if showCloseChatOptions}
			<div
				class="flex items-center w-full flex-col gap-3 justify-center items-center"
			>
				{#if messages?.length > 1}
					<Button
						class="w-full"
						kind="tertiary"
						on:click={async () => await closeChat(true)}
						>Save and close</Button
					>
				{/if}
				<Button
					class="w-full"
					kind="danger-tertiary"
					on:click={async () => await closeChat(false)}
					>Just close it</Button
				>
				<Button
					class="w-full"
					on:click={() => {
						showCloseChatOptions = false;
					}}>Continue Chat</Button
				>
			</div>
		{:else if hasError}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<CarbonLabel class="w-5 h-5" />
				<h2>{errorMessage}</h2>
			</div>
		{:else if loading && messages?.length === 0}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<div
					class="animate-spin w-5 h-5 border-t-2 border-b-2 border-gray-900"
				></div>
				<h2>{loadingMessage}</h2>
			</div>
		{:else if messages.length > 0}
			<div
				class="w-full h-[100%]"
				style="overflow-y:auto;"
				bind:this={chatContainer}
			>
				{#each messages as message}
					<div
						title={message.timestamp}
						class="p-2 w-full flex items-center gap-2

						{message.role === 'user' ? 'justify-end' : 'justify-start'} "
					>
						<div
							class="p-2 rounded-lg {message.role === 'user'
								? ' bg-[var(--cds-interactive-01)] text-white '
								: ' bg-gray-300 dark:bg-[var(--cds-ui-04)] '} break-all"
						>
							<SvelteMarkdown
								source={message.content}
								renderers={svelteMarkdownRenderers}
							/>
						</div>
					</div>
				{/each}
			</div>
		{:else}
			<div class="p-4 w-full flex items-center justify-center gap-2">
				<CarbonLabel class="w-5 h-5" />
				<h2>No messages yet.</h2>
			</div>
		{/if}

		<form
			on:submit|preventDefault={sendMessage}
			class="bottom p-4 w-full flex flex-col gap-2"
		>
			<div>
				<TextInput
					placeholder="type your message"
					bind:value={userMessage}
					disabled={inputsDisabled}
				/>
				<div class="flex gap-2 items-center">
					Include context
					<Checkbox
						bind:checked={includeContext}
						disabled={inputsDisabled}
					/>
				</div>
			</div>
			<div class="flex items-center justify-between">
				<h2>
					{#if loading && messages?.length > 0}
						<InlineLoading
							description={loadingMessage}
							status="active"
						/>
					{:else}
						Type your message.
					{/if}
				</h2>
				<Button type="submit" disabled={inputsDisabled}>Send</Button>
			</div>
		</form>
	</div>
{/if}
