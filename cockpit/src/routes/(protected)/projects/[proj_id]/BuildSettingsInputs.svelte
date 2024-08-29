<script lang="ts">
import { TextArea, TextInput, Tile, TooltipIcon } from 'carbon-components-svelte';
import { Information } from 'carbon-icons-svelte';

export let buildCommand: string;
export let outputDir: string;
export let installCommand: string;
export let postInstall: string;
</script>

<Tile class="  ">
	<div>
		<div class="flex gap-1 pt-3 items-center">
			Install Command
			<TooltipIcon
				tooltipText="Command to install dependencies."
				direction="top"
				icon={Information}
				class="h-4 w-4"
			/>
		</div>
		<TextInput bind:value={installCommand} />
	</div>
	<div>
		<div class="flex gap-1 pt-3 items-center">
			Build Command
			<TooltipIcon
				tooltipText="Command to build/compile your code."
				direction="top"
				icon={Information}
				class="h-4 w-4"
			/>
		</div>
		<TextInput bind:value={buildCommand} />
	</div>

	<div>
		<div class="flex gap-1 pt-3 items-center">
			Build/Output Directory
			<TooltipIcon
				tooltipText="Build/Ouput directory relative to the project root."
				direction="top"
				icon={Information}
				class="h-4 w-4"
			/>
		</div>
		<TextInput bind:value={outputDir} />
	</div>

	<div>
		<div class="flex gap-1 pt-3 items-center">
			Post Install Commands (if any)
			<TooltipIcon
				tooltipText="Post install commands like `npx prisma migrate dev --init something` "
				direction="top"
				icon={Information}
				class="h-4 w-4"
			/>
		</div>
		<TextArea bind:value={postInstall} />
	</div>
</Tile>
