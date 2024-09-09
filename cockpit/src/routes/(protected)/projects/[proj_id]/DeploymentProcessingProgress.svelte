<script lang="ts">
	import { browser } from "$app/environment";
	import { invalidateAll } from "$app/navigation";
	import { page } from "$app/stores";
	import PreDebug from "@/components/dev/PreDebug.svelte";
	import type { ProjectDeployment } from "@/types/entities";
	import { InlineLoading } from "carbon-components-svelte";
	import { source } from "sveltekit-sse";
	import DeploymentProcessingHistory from "./DeploymentProcessingHistory.svelte";
	import DepInfo from "@/components/project-specifics/DepInfo.svelte";
</script>
