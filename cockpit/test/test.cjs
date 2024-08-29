
module.exports = {
    apps: [
        {
            name: "test2",
            instances: 1,
            script: "/media/shrestho/github/tjf/freshcrafts/fc_data/projects/01J6EJT4WGBC4HKF5PAA3J1DT5/v1/src/build/index.js",
            interpreter: "/usr/bin/node",
            interpreter_args: "--env-file=/media/shrestho/github/tjf/freshcrafts/fc_data/projects/01J6EJT4WGBC4HKF5PAA3J1DT5/v1/src/.env",
            error_file: "/media/shrestho/github/tjf/freshcrafts/fc_data/projects/01J6EJT4WGBC4HKF5PAA3J1DT5/v1/prod/logs/error.log",
            out_file: "/media/shrestho/github/tjf/freshcrafts/fc_data/projects/01J6EJT4WGBC4HKF5PAA3J1DT5/v1/prod/logs/out.log",
            log_date_format: "YYYY-MM-DD HH:mm:ss",
            max_memory_restart: "100M",
            restart_delay: 1000,
            source_map_support: true,
            cron_restart: "0 0 * * *",
            autorestart: true,
            env: {
                "PORT": 50956,
                "HOST": "0.0.0.0",
                "ORIGIN": "https://test2.freshcrafts.local",
            }
        }]
}
