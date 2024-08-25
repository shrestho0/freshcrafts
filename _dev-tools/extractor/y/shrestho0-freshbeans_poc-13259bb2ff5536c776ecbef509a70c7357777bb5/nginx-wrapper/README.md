> Poc for managing nginx through springboot (partial)

It works fine under root privileges and sometimes using `sudo`,
Server user that runs this script should allow all:nopasswd on sudoers file for peace of mind. There might be other better ways, we'll find out. 

endpoints: 
```
/
/ls - list dir [POST, {"dir": "/some/dir/location/"}]
/nginx/sites-enabled - list of nginx configs 
nginx/sites-enabled/[CONF_FILE_NAME] - returns contents of config file
nginx/conf_check - runs `sudo nginx -t` to check syntax of configs
/nginx/graceful_reload - reloads nginx without downtime (as per nginx doc)

```

TODO: 

- add configs
- remove configs
- modify configs
