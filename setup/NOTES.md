

sudo systemctl daemon-reload

init

sudo cp -r templates/fc_test.service /lib/systemd/system/fc_test.service
sudo systemctl status fc_test       



```
# update 
sudo systemctl stop fc_test 
# do update stuff
sudo systemctl start fc_test

```