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
	<path d="M22 27.18L19.41 24.59 18 26 22 30 30 22 28.59 20.59 22 27.18z"></path><path
		d="M25,5H22V4a2.0058,2.0058,0,0,0-2-2H12a2.0058,2.0058,0,0,0-2,2V5H7A2.0058,2.0058,0,0,0,5,7V28a2.0058,2.0058,0,0,0,2,2h9V28H7V7h3v3H22V7h3V18h2V7A2.0058,2.0058,0,0,0,25,5ZM20,8H12V4h8Z"
	></path>
</svg>
