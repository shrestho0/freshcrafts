<script lang="ts">
    export let data: {} | null | undefined;
    export let title: string = "";
    data = structuredClone(data);
    export let classes: string = "";
</script>

<!-- PreDebugging -->
<!-- Takes A Data Object and shows inside pre tag with title as string -->

<pre class="border m-8 p-4 bg-gray-50 text-black rounded {classes}">
<h2 class="debug_title">#Debugging{title ? ": " + title : ""}</h2>
{JSON.stringify(data ?? { no_data: true }, null, 2)}
</pre>
