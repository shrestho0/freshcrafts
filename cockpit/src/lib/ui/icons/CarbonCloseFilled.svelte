<script>
	export let size = 16;

	export let title = undefined;

	$: labelled = $$props["aria-label"] || $$props["aria-labelledby"] || title;
</script>

<svg
	{...$$restProps}
	xmlns="http://www.w3.org/2000/svg"
	viewBox="0 0 32 32"
	fill="currentColor"
	width="16"
	height="16"
	preserveAspectRatio="xMidYMid meet"
>
	<path
		d="M16,2C8.2,2,2,8.2,2,16s6.2,14,14,14s14-6.2,14-14S23.8,2,16,2z M21.4,23L16,17.6L10.6,23L9,21.4l5.4-5.4L9,10.6L10.6,9	l5.4,5.4L21.4,9l1.6,1.6L17.6,16l5.4,5.4L21.4,23z"
	></path><path
		d="M14.4 16L9 10.6 10.6 9 16 14.4 21.4 9 23 10.6 17.6 16 23 21.4 21.4 23 16 17.6 10.6 23 9 21.4 14.4 16"
		data-icon-path="inner-path"
		opacity="0"
	></path>
</svg>
