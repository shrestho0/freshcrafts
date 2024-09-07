<script lang="ts">
import type { EngineSystemConfigResponseDto } from '@/types/dtos';
import CircleCheckBig from '@/ui/icons/CircleCheckBig.svelte';
import CircleX from '@/ui/icons/CircleX.svelte';
import { Table, TableCell, TableRow } from 'carbon-components-svelte';

export let sysConfig: EngineSystemConfigResponseDto;
</script>

{#if sysConfig}
	<div class="w-full max-w-2xl p-4">
		<div class="mt-4">
			<div class="flex flex-col gap-2">
				<h2 class="text-2xl">Account</h2>
				<Table>
					<TableRow>
						<TableCell>Name</TableCell>
						<TableCell>{sysConfig.systemUserName ?? 'Not Set'}</TableCell>
					</TableRow>
					<TableRow>
						<TableCell>Email</TableCell>
						<TableCell>{sysConfig.systemUserEmail ?? 'Not Set'}</TableCell>
					</TableRow>
					<TableRow>
						<TableCell>Password</TableCell>
						<TableCell>{sysConfig.systemUserPasswordHash ?? 'Not Set'}</TableCell>
					</TableRow>
				</Table>
			</div>
			<div class="flex flex-col gap-2">
				<h2 class="text-2xl">OAuth</h2>
				<Table>
					<TableRow>
						<TableCell>Github</TableCell>
						<TableCell>
							{#if sysConfig.systemUserOauthGithubEnabled}
								<CircleCheckBig class="text-green-600 h-5" />
							{:else}
								<CircleX class="text-red-500 h-5" />
							{/if}
						</TableCell>
					</TableRow>
					<TableRow>
						<TableCell>Google</TableCell>
						<TableCell>
							{#if sysConfig.systemUserOauthGoogleEnabled}
								<CircleCheckBig class="text-green-600 h-5" />
							{:else}
								<CircleX class="text-red-500 h-5" />
							{/if}
						</TableCell>
					</TableRow>
				</Table>
			</div>
		</div>
	</div>
{:else}
	Data is not defined
{/if}
