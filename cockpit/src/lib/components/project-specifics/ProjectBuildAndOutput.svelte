<script lang="ts">
import ExpandableSection from '@/components/ExpandableSection.svelte';
import CarbonInformation from '@/ui/icons/CarbonInformation.svelte';
import { TextArea, TextInput, Tile, TooltipIcon } from 'carbon-components-svelte';

export let buildCommand: string;
export let outputDir: string;
export let installCommand: string;
export let postInstall: string;

let expanded = true;
</script>

<div class="select-none">
	<ExpandableSection bind:open={expanded} title="Build and output settings">
		<!-- <BuildSettingsInputs
			bind:buildCommand={projectSetup.buildCommand}
			bind:outputDir={projectSetup.outputDir}
			bind:installCommand={projectSetup.installCommand}
			bind:postInstall={projectSetup.postInstall}
		/> -->

		<Tile class="  ">
			<div>
				<div class="flex gap-1 pt-3 items-center">
					Install Command
					<TooltipIcon
						tooltipText="Command to install dependencies."
						direction="top"
						icon={CarbonInformation}
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
						icon={CarbonInformation}
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
						icon={CarbonInformation}
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
						icon={CarbonInformation}
						class="h-4 w-4"
					/>
				</div>
				<TextArea bind:value={postInstall} />
			</div>
		</Tile>
	</ExpandableSection>
</div>
