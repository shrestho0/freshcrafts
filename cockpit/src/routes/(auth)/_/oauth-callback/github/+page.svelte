<script lang="ts">
	import { browser } from "$app/environment";
	import { goto } from "$app/navigation";
	import ArrowRightIcon from "@/ui/icons/ArrowRightIcon.svelte";
	import { Button } from "carbon-components-svelte";
	import { onMount } from "svelte";

	export let data;
	onMount(() => {
		if (browser) {
			if (data?.success) {
				try {
					if (
						data?.closeWindow ||
						(window.opener && window.opener !== window)
					) {
						window.close(); // TODO: uncomment this line
					}
				} catch (e) {
					console.log("error closing window", e);
				}

				window.location.href = "/dashboard"; // TODO: uncomment this line
			} else {
				console.log("data from oauth redirect page", data);
			}
		}
	});
</script>

<div class=" w-full flex flex-1 h-screen pb-64">
	{#if data?.success}
		<div class="flex flex-col items-center justify-center w-full">
			<h1 class="text-2xl font-bold">Github OAuth Callback</h1>
			<p class="text-lg">You will be redirected or closed shortly...</p>
			<a
				class=" bx--btn--primary p-2 text-[16px] rounded my-8"
				href="/dashboard">Click here if you are not redirected</a
			>
		</div>
	{:else}
		<div class="flex flex-col items-center justify-center w-full">
			<h1 class="text-2xl font-bold">Github OAuth Callback</h1>
			<p class="text-lg">{data?.message}</p>
			<a
				class=" bx--btn--primary p-2 text-[16px] rounded my-8"
				href="/dashboard">Back to dashboard</a
			>
		</div>
	{/if}
</div>

<!-- OAUTH CALLBACK PAGE -->
<!-- <pre>
	{JSON.stringify(data, null, 2)}
</pre> -->
