<script>
export let size = 16;

export let title = undefined;

$: labelled = $$props['aria-label'] || $$props['aria-labelledby'] || title;
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
	<path d="M2 6H4V26H2zM6 4H8V28H6zM14 22H26V24H14zM14 16H26V18H14z"></path><path
		d="M29.7,9.3l-7-7C22.5,2.1,22.3,2,22,2H12c-1.1,0-2,0.9-2,2v24c0,1.1,0.9,2,2,2h16c1.1,0,2-0.9,2-2V10	C30,9.7,29.9,9.5,29.7,9.3z M22,4.4l5.6,5.6H22V4.4z M28,28H12V4h8v6c0,1.1,0.9,2,2,2h6V28z"
	></path>
</svg>
