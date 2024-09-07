<script lang="ts">
import ExpandableSection from '@/components/ExpandableSection.svelte';
import { TextArea, TextInput, Tile } from 'carbon-components-svelte';
import { EnvVarsUtil } from '@/utils/utils';

let expanded = true;
export let options: any;
export let selectedOptionIdx: any;
export let envFileContent: any;
export let envKV: any;

function filterEnv(idx: number) {
	envKV = envKV.filter((_: any, i: number) => i !== idx);
}
</script>

<div class="select-none">
	<ExpandableSection bind:open={expanded} title="Environment Variables">
		<Tile>
			<div class="w-full flex gap-0 items-center justify-between">
				{#each options as option, idx}
					<button
						role="tab"
						tabindex="-1"
						class="w-full focus:outline-none text-center bx--tile bx--tile--selectable"
						class:bx--tile--is-selected={selectedOptionIdx === idx}
						on:click={() => {
							if (selectedOptionIdx === idx) return;
							selectedOptionIdx = idx;

							if (selectedOptionIdx === 0) {
								envFileContent = EnvVarsUtil.kvToContent(envKV);
							} else {
								envKV = EnvVarsUtil.contentToKV(envFileContent);
							}
						}}
					>
						<span class="bx--tile__checkmark"
							><svg
								xmlns="http://www.w3.org/2000/svg"
								viewBox="0 0 32 32"
								fill="currentColor"
								preserveAspectRatio="xMidYMid meet"
								width="16"
								height="16"
								role="img"
								aria-label="Tile checkmark"
								><title>Tile checkmark</title><path
									d="M16,2A14,14,0,1,0,30,16,14,14,0,0,0,16,2ZM14,21.5908l-5-5L10.5906,15,14,18.4092,21.41,11l1.5957,1.5859Z"
								></path><path
									fill="none"
									d="M14 21.591L9 16.591 10.591 15 14 18.409 21.41 11 23.005 12.585 14 21.591z"
									data-icon-path="inner-path"
								></path></svg
							></span
						> <span class="bx--tile-content">{option}</span>
					</button>
				{/each}
			</div>
			<div class="body">
				{#if selectedOptionIdx == 0}
					<TextArea
						placeholder={`\nKEY1=value1\nKEY2=value2\n...`}
						bind:value={envFileContent}
						helperText="Put content of your .env file here"
					/>
				{:else if (selectedOptionIdx = 1)}
					<div class="add-more-btn pt-3 text-right">
						<button
							class="  w-full bx--btn--secondary bx--btn--sm"
							on:click={() => {
								envKV = [...envKV, { key: '', value: '' }];
							}}
						>
							Add Key-Value Pair 🔥
						</button>
					</div>
					<div class="flex flex-col gap-2 py-3">
						{#each envKV as x, idx}
							<div class="flex flex-1 gap-2">
								<TextInput size="sm" bind:value={x.key} placeholder="KEY" />
								<TextInput size="sm" bind:value={x.value} placeholder="value" />
								<button
									class="bx--btn bx--btn--secondary bx--btn--sm"
									on:click={() => filterEnv(idx)}
								>
									Remove
								</button>
							</div>
						{/each}
					</div>
				{/if}
			</div>
		</Tile>
	</ExpandableSection>
</div>
