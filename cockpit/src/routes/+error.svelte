<script lang="ts">
	import { page } from '$app/stores';
	import { Grid, Row, Column, Button } from 'carbon-components-svelte';
	import { ArrowRight, ArrowLeft } from 'carbon-icons-svelte';
</script>

<div class="w-full h-screen flex items-center justify-center">
	<Grid>
		<Row>
			<Column class="flex  flex-col gap-3">
				<h1 class="text-4xl font-bold text-center">{$page.status}</h1>
				<p class="text-center">{$page?.error?.message}</p>
				<!-- <p class="text-center">{JSON.stringify($page)}</p> -->

				<!-- back to home button -->
				<a
					tabindex="0"
					href="/"
					class="bx--btn bx--btn--tertiary flex items-center justify-between w-full gap-3"
				>
					<ArrowLeft />
					<span class=" "> Back to Home </span>
				</a></Column
			>
		</Row>
	</Grid>
</div>
