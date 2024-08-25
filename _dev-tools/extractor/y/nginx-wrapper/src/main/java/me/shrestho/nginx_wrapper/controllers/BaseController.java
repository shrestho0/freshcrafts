package me.shrestho.nginx_wrapper.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import me.shrestho.nginx_wrapper.services.BaseService;

@RestController
public class BaseController {

    @Autowired
    private BaseService baseService;

    @GetMapping("/")
    public String index() {
        return baseService.sayHello();
    }

    // test
    @PostMapping("/ls")
    public String listDir(@RequestBody Map<String,String> body) {
        try{
            String dir = body.get("dir");
            if (dir == null) {
                return "command nei";
            }
            System.out.println("DIRECTORY "+ dir);
            return baseService.listDir(dir);
        }catch(Exception e){
            // 
            e.printStackTrace();
        }
        return baseService.sayHello();
    }

    /// NGINX Stuff ///
    @GetMapping("/nginx/sites-enabled")
    public String getNginxSitesEnabled(){
        try {
            return baseService.listDir("/etc/nginx/sites-enabled/");
            
        } catch (Exception e) {
            return "something went wrong :3";
        }
    }

    @GetMapping("/nginx/sites-enabled/{conf_name}")
    public String getNginxConfFileUsingName(@PathVariable String conf_name){
        try {
            return baseService.readFile("/etc/nginx/sites-enabled/"+conf_name);
        } catch (Exception e) {
            e.printStackTrace();
            return "something went wrong";
        }
    }

    @GetMapping("/nginx/conf_check")
    public String getConfStatus(){
        try {
            return baseService.checkNginxStatus();
        } catch (Exception e) {
            e.printStackTrace(); // nginx fails with exit code 1 for syntax issues
            return "failed to check status or actually failed";
        }
    }

    @GetMapping("/nginx/graceful_reload")
    public String gracefulReload(){
        try {
            baseService.reloadNginx();
            return "nginx reload successful"; 
        } catch (Exception e) {
            return "failed to reload nginx";
        }
    }

}
