<script lang="ts">
import { browser } from '$app/environment';

import {
	Row,
	Column,
	TextInput,
	PasswordInput,
	InlineNotification
} from 'carbon-components-svelte';
import { ArrowRight, LogoGithub } from 'carbon-icons-svelte';
import { onMount } from 'svelte';
import HeaderUnAuthenticated from '@/components/HeaderUnAuthenticated.svelte';
import { applyAction, enhance } from '$app/forms';
import { goto, invalidateAll } from '$app/navigation';
import type { ActionResult } from '@sveltejs/kit';
import { AuthProviderType } from '@/types/enums';

export let data;

const { githubLoginUrl, googleLoginUrl } = data;

// let githubUrl = initializeAndReturnGithubUrl();
// let googleUrl = initializeAndReturnGoogleUrl();

onMount(() => {
	if (browser) {
		document.cookie = 'fromPage=login; path=/; max-age=0'; //
	}
});
// onDestroy(() => {
// 	if (browser) {
// 		document.cookie = 'fromPage=login; path=/; max-age=0 ';
// 	}
// });
let error_message = '';

function enhancedEmailPasswordLogin() {
	return async ({ result }: { result: ActionResult }) => {
		console.log(result);
		switch (result.type) {
			case 'success':
				await applyAction(result);
				invalidateAll();
				break;
			case 'failure':
				error_message = result?.data?.message ?? 'Failed to login';
				break;
			case 'redirect':
				window.location.href = result.location;
				// invalidateAll();
				// goto(result.location);
				break;
		}
	};
}
</script>

<div class="h-full">
	<div class="app-container h-full">
		<div class="outer table absolute h-full w-full">
			<div class="middle align-middle table-cell h-full">
				<div class="inner">
					<HeaderUnAuthenticated />
					<div class="login-container h-full m-auto">
						<div class="login-grid h-full bx--grid">
							<div class="main-row bx--row h-full w-full text-center md:text-left my-0 mx-auto">
								<div class="form-area bx--col-lg-7 bx--offset-lg-0">
									<div>
										<Row>
											<Column class="w-full    h-full">
												<div class="">
													<div class="bx--row">
														<div class="bx--col">
															<div class="text-left text-[2rem] font-medium space-x-3">
																Log in to FreshCrafts
															</div>
														</div>
													</div>
												</div>
												<div class="login-form">
													{#if data?.providers?.length > 0}
														{#if data.providers?.includes(AuthProviderType.EMAIL_PASSWORD)}
															<form
																class="bx--form regular-auth-container"
																name="loginForm"
																method="POST"
																action=""
																use:enhance={enhancedEmailPasswordLogin}
															>
																{#if error_message}
																	<InlineNotification
																		title="Error:"
																		subtitle={error_message}
																		hideCloseButton
																		class="py-0 my-2 bg-[#393939] text-white  "
																	/>
																{/if}
																<Row>
																	<Column class="flex flex-col gap-2">
																		<TextInput
																			size="xl"
																			class="w-full"
																			name="x-email"
																			type="email"
																			labelText="Email"
																			placeholder="hello@example.com"
																		/>

																		<PasswordInput
																			size="xl"
																			class="w-full"
																			name="x-password"
																			type="password"
																			labelText="Password"
																			placeholder="*************"
																		/>

																		<button
																			type="submit"
																			class="w-full flex justify-between items-center max-w-full bx--btn--primary text-left px-4 min-h-[3rem]"
																			>Sign In
																			<ArrowRight />
																		</button>
																	</Column>
																</Row>
															</form>
														{/if}
														<div class="oauth-container oauth-container">
															<div class=" w-full max-w-full gap-3 flex flex-col">
																{#if data.providers?.includes(AuthProviderType.OAUTH_GITHUB)}
																	<button
																		on:click={() => {
																			if (githubLoginUrl)
																				window.location.href = githubLoginUrl?.toString();
																		}}
																		class="w-full flex justify-between items-center max-w-full bx--btn--secondary dark:bx--btn--tertiary text-left px-4 min-h-[3rem]"
																		>Sign in with Github
																		<LogoGithub class="w-6 h-6" />
																	</button>
																{/if}
																{#if data.providers?.includes(AuthProviderType.OAUTH_GOOGLE)}
																	<button
																		class="w-full flex justify-between items-center max-w-full bx--btn--secondary dark:bx--btn--tertiary text-left px-4 min-h-[3rem]"
																		on:click={() => {
																			if (googleLoginUrl)
																				window.location.href = googleLoginUrl?.toString();
																		}}
																	>
																		Sign in with Google
																		<svg
																			xmlns="http://www.w3.org/2000/svg"
																			x="0px"
																			y="0px"
																			class="w-6 h-6 fill-current"
																			viewBox="0 0 30 30"
																		>
																			<path
																				d="M 15.003906 3 C 8.3749062 3 3 8.373 3 15 C 3 21.627 8.3749062 27 15.003906 27 C 25.013906 27 27.269078 17.707 26.330078 13 L 25 13 L 22.732422 13 L 15 13 L 15 17 L 22.738281 17 C 21.848702 20.448251 18.725955 23 15 23 C 10.582 23 7 19.418 7 15 C 7 10.582 10.582 7 15 7 C 17.009 7 18.839141 7.74575 20.244141 8.96875 L 23.085938 6.1289062 C 20.951937 4.1849063 18.116906 3 15.003906 3 z"
																			></path>
																		</svg>
																	</button>
																{/if}
															</div>
														</div>
													{:else}
														<InlineNotification
															title="Error:"
															subtitle={data?.message ?? 'No login providers available'}
															hideCloseButton
															class="py-0 my-2 bg-[#393939] text-white  "
														/>
													{/if}
												</div>
											</Column>
										</Row>
									</div>
								</div>
								<div class="right-pannel relative bx--col-lg-9 bx--offset-lg-0">
									<div class="canvas-container relative h-full">
										<div
											class="login-canvas"
											style="background-image: url(/illustration-final.svg);"
										></div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</div>

<style>
.inner {
	height: 100%;
	background-position: left 480px;
	background-repeat: no-repeat;
	margin-left: auto;
	margin-right: auto;
	position: relative;
	padding-bottom: 50px;
	min-width: 320px;
	min-height: 338px;
	overflow: hidden;
	max-width: 99rem;
}
.heading-container {
	padding-bottom: 2rem;
	margin-bottom: 1rem;
	border-bottom: 1px solid #e0e0e0;
}
.oauth-container,
.regular-auth-container {
	border-top: 1px solid #e0e0e0;
	padding-top: 1rem;
	margin-top: 1rem;
}

.login-container .right-pannel .canvas-container .login-canvas {
	position: absolute;
	left: 0;
	width: 100%;
	min-width: 1000px;
	height: 100%;
	background-repeat: no-repeat;
	background-position-x: left;
	background-position-y: bottom;
}
.login-form form .bx--btn {
	width: 100%;
	max-width: 100%;
}
@media (min-width: 42rem) {
	.login-container {
		margin: auto;
	}
}
.inner,
.login-container {
	height: 100%;
}
.login-container .form-area {
	height: 100%;
	margin-top: 48px;
	margin-bottom: 144px;
	padding-top: 2.5rem;
	max-width: 448px;
	min-height: 610px;
}
@media (min-width: 66rem) {
	.login-container .form-area {
		margin-bottom: 48px;
	}
}
</style>
