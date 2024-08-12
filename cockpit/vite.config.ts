import { sveltekit } from '@sveltejs/kit/vite';
import { searchForWorkspaceRoot } from 'vite';
import { defineConfig } from 'vitest/config';

export default defineConfig({
	plugins: [sveltekit()],

	server: {
		port: 10001,
		fs: {
			allow: [searchForWorkspaceRoot(process.cwd()), '../data/uploads/']
		}
	},
	test: {
		include: ['src/**/*.{test,spec}.{js,ts}']
	}
});
