<script lang="ts">
	import { Button } from "carbon-components-svelte";
	import { toast } from "svelte-sonner";
</script>

<h1>Protected /</h1>

<Button
	kind="primary"
	on:click={() => {
		toast.info("Hello World", { duration: 50000, position: "top-left" });
		toast.warning("Hello World", {
			duration: 50000,
			position: "top-center",
		});
		toast.error("Hello World", { duration: 50000, position: "top-right" });
	}}
>
	Toast Info
</Button>
