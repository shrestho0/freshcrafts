<script lang="ts">
    import PreDebug from "@/components/dev/PreDebug.svelte";
    import type { Project, ProjectDeployment } from "@/types/entities.js";
    import { InternalDocGenerationCommands } from "@/types/enums";
    import { Button } from "carbon-components-svelte";

    export let data;
    /**
     * It must have project and activeDeployment
     */
    let project: Project = data.project!;
    let activeDeployment: ProjectDeployment = data.activeDeployment!;

    let generationRequestStuff = {
        status: "idle",
    } as {
        status: "idle" | "requested" | "generated" | "failed";
    };
    async function generateDoc() {
        generationRequestStuff.status = "requested";
        const res = await fetch("", {
            method: "PATCH",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                command: InternalDocGenerationCommands.GENERATE_DOC,
                project: project,
                activeDeployment: activeDeployment,
            }),
        });

        handleGenerateDoc(res);
    }

    async function handleGenerateDoc(res: Response) {
        const dataReturned = await res.json().catch((e) => {
            return {
                success: false,
                message: "Failed to parse response",
            };
        });

        if (dataReturned.success) {
            generationRequestStuff.status = "generated";
        } else {
            generationRequestStuff.status = "failed";
        }
    }
</script>

<PreDebug data={{ generationRequestStuff }} />
Generated Docs Generate New Doc

<Button on:click={generateDoc}>Generate New Doc</Button>
