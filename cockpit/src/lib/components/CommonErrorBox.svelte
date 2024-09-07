<script lang="ts">
import OctagonX from '@/ui/icons/OctagonX.svelte';

export let error_msg: string = '';
</script>

<div class="w-full flex flex-col items-center justify-center bx--tile py-8">
	<OctagonX class=" h-12 w-12" />
	<div class="text-center text-[var(--cds-interactive-02)]">
		<div class="text-lg font-normal">{error_msg}</div>
	</div>
	<slot></slot>
</div>
