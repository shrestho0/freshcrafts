<script>
export let size = 16;

export let title = undefined;
</script>

<svg
	xmlns="http://www.w3.org/2000/svg"
	viewBox="0 0 32 32"
	fill="currentColor"
	preserveAspectRatio="xMidYMid meet"
	width={size}
	height={size}
	{...$$restProps}
>
	{#if title}<title>{title}</title>{/if}
	<path
		d="M2 26H30V28H2zM25.4 9c.8-.8.8-2 0-2.8 0 0 0 0 0 0l-3.6-3.6c-.8-.8-2-.8-2.8 0 0 0 0 0 0 0l-15 15V24h6.4L25.4 9zM20.4 4L24 7.6l-3 3L17.4 7 20.4 4zM6 22v-3.6l10-10 3.6 3.6-10 10H6z"
	></path>
</svg>
