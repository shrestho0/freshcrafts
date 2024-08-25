module.exports = {
  apps: [
    {
      name: "test-app-00",
      instances: 1,
      script: "/home/sherstho/Desktop/freshbeans_poc/pm2-test/test-app-00/index.js",
      interpreter: "/usr/bin/node",
      interpreter_args: "--env-file=/home/sherstho/Desktop/freshbeans_poc/pm2-test/test-app-00/.env",
      error_file: "/home/sherstho/Desktop/freshbeans_poc/pm2-test/logs/error.log",
      out_file: "/home/sherstho/Desktop/freshbeans_poc/pm2-test/logs/out.log",
      log_date_format: "YYYY-MM-DD HH:mm:ss",
      max_memory_restart: "100M",
      restart_delay: 5000,
      source_map_support: true,
      cron_restart: "*/1 * * * *", // every 1 minute
      autorestart: true,
      env_production: {
        "PORT": 6970,
        "HOST": "localhost",
        "ORIGIN": "http://fc.local"
      }
    }
  ]
}