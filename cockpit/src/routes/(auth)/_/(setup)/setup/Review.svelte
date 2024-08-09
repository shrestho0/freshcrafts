<script lang="ts">
import type { SetupPageAccountData, SetupPageOauthData } from '@/types/internal';
import {
	PasswordInput,
	Table,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
	TextInput
} from 'carbon-components-svelte';
import { TaskComplete } from 'carbon-icons-svelte';
import { CircleCheckBig, CircleX } from 'lucide-svelte';

export let account;
export let oauth;

let accountData = account.data as SetupPageAccountData;
let oauthData = oauth.data as SetupPageOauthData;
</script>

<!-- {JSON.stringify(account)} -->
<!-- {JSON.stringify(oauth)} -->

<div class="w-full max-w-2xl p-4">
	<div class="mt-4">
		<div class="flex flex-col gap-2">
			<h2 class="text-2xl">Account</h2>
			<Table>
				<TableRow>
					<TableCell>Email</TableCell>
					<TableCell>{accountData.email}</TableCell>
				</TableRow>
				<TableRow>
					<TableCell>Password</TableCell>
					<TableCell>{accountData.password}</TableCell>
				</TableRow>
			</Table>
		</div>
		<div class="flex flex-col gap-2">
			<h2 class="text-2xl">OAuth</h2>
			<Table>
				<TableRow>
					<TableCell>Github</TableCell>
					<TableCell>
						{#if oauthData.githubStatus === 'connected'}
							<CircleCheckBig class="text-green-600 h-5" />
						{:else}
							<CircleX class="text-red-500 h-5" />
						{/if}
					</TableCell>
				</TableRow>
				<TableRow>
					<TableCell>Google</TableCell>
					<TableCell>
						{#if oauthData.googleStatus === 'connected'}
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
