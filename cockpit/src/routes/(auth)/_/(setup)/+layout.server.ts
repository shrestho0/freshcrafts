import { BackendEndpoints } from '@/backend-endpoints';
import type { LayoutServerLoad } from './$types';
import { redirect } from '@sveltejs/kit';
import { EngineConnection } from '@/server/EngineConnection';
export const load: LayoutServerLoad = async ({ locals, cookies }) => {
	// check if already setup
	// if not, okay,
	// if yes, add setup complete to cookie
	// // and redirect to dashboard

	// const res = await fetch(BackendEndpoints.SETUP_SYSCONFIG, {
	// 	headers: {
	// 		'Content-Type': 'application/json'
	// 	}
	// })
	// 	.then((res) => res.json())
	// 	.catch((err) => {
	// 		return null;
	// 	});

	const sysConf = await EngineConnection.getInstance().getSystemConfig();
	console.log("sysconf", sysConf);
	// console.log(sysConf);
	let allowed = sysConf != null && Object.keys(sysConf).includes("systemUserSetupComplete") && sysConf.systemUserSetupComplete === false

	if (!allowed) {
		return redirect(307, '/dashboard');
	}


};
