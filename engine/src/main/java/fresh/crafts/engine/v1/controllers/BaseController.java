package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/api/v1", consumes = "application/json", produces="application/json")
class BaseController {

    @GetMapping("")
    public HashMap<String, String> index(){
        HashMap<String, String> res = new HashMap<>();
        
        res.put("description","Engine Service Index Page" );
        res.put("documentation","/api/v1/api-docs" );

        return res;
    }

    
}
