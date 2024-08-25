
###
# CHECKED: env file is reloaded on auto restart
# CHECKED: env file is reloaded on manual restart
# CHECKED: rsa keys are loaded perfectly from env file
# CHECKED: PM2 file env has greater presedence over .env file
# CHECKED: env var takes over
# 
# 
###

autocannon -c 300 -w 3 http://localhost:6970


pm2 start ecosystem.config.js --env production
pm2 stop ecosystem.config.js


pm2 reload ecosystem.config.js 
pm2 restart ecosystem.config.js --env production

pm2 delete ecosystem.config.js
