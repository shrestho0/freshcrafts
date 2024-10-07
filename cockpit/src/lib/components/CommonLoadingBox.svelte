<script>
	import { InlineLoading } from "carbon-components-svelte";
	export let message;
</script>

<div class="w-full flex flex-col items-center justify-center bx--tile">
	<InlineLoading />
	<div class="text-center">
		<div class="text-lg font-normal">{message}</div>
	</div>
</div>
