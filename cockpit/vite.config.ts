import { sveltekit } from '@sveltejs/kit/vite';
import { searchForWorkspaceRoot } from 'vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],

	server: {
		port: 10001,

		fs: {
			strict: false,
			// allow: [searchForWorkspaceRoot(process.cwd()), '../../fc_data/']
		}
	},
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}']
	}
});
