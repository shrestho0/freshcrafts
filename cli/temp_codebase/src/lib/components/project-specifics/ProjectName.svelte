<script lang="ts">
	import { InternalProjectSetupCommand } from "@/types/enums";
	import { InlineLoading, TextInput } from "carbon-components-svelte";

	export let projectNameStatus: string;
	export let projectName: string;
	export let projectNameErrorMessage: string;

	let timer: NodeJS.Timeout;
	let _projectName = "";
	const handleNameInput = (e: KeyboardEvent) => {
		const v = (e.target as HTMLInputElement).value;
		debouncedNameSearch(v);
	};
	const debouncedNameSearch = (v: string | null) => {
		projectNameStatus = "checking";
		clearTimeout(timer);
		timer = setTimeout(async () => {
			// check project name and do stuff
			// _projectName = v;
			if (!v) {
				projectNameStatus = "initially_idle";
				return;
			}
			const res = await fetch("/projects", {
				method: "PATCH",
				headers: {
					"Content-Type": "application/json",
					"x-freshcrafts-project-setup-command":
						InternalProjectSetupCommand.CHECK_UNIQUE_NAME.toString(),
				},
				body: JSON.stringify({
					projName: v.trim(),
				}),
			})
				.then((res) => res.json())
				.catch((e) => console.error(e));
			console.log("name::", res);
			if (res?.success) {
				projectNameStatus = "ok";
				projectName = v;
			} else {
				projectNameStatus = "invalid";
				projectNameErrorMessage =
					res?.message || "Invalid project name";
			}

			console.warn("res", res);
		}, 750);
	};
</script>

<div>
	<div>Project Name</div>
	<TextInput on:keyup={handleNameInput} placeholder="anonymous-monkey" />
	<div class="">
		{#if projectNameStatus != "initially_idle"}
			<InlineLoading
				status={projectNameStatus === "checking"
					? "active"
					: projectNameStatus === "ok"
						? "finished"
						: projectNameStatus === "invalid"
							? "error"
							: "inactive"}
				description={projectNameStatus === "checking"
					? "Checking..."
					: projectNameStatus === "ok"
						? "Available"
						: (projectNameErrorMessage ?? "")}
			/>
		{/if}
	</div>
</div>
