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
	<circle cx="11" cy="15.5" r="1.5"></circle><path
		d="M12,12H10V8h2a2,2,0,0,0,0-4H10A2.0023,2.0023,0,0,0,8,6v.5H6V6a4.0045,4.0045,0,0,1,4-4h2a4,4,0,0,1,0,8Z"
	></path><path
		d="M22.4479,21.0337A10.971,10.971,0,0,0,19.9211,4.7446l-.999,1.73A8.9967,8.9967,0,1,1,5,14H3a10.9916,10.9916,0,0,0,18.0338,8.4478L28.5859,30,30,28.5859Z"
	></path>
</svg>
