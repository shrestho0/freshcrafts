package fresh.crafts.engine.v1.controllers;

import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.services.TestTestService;

@RestController
@RequestMapping(value = "/api/v1/test", consumes = "application/json", produces = "application/json")

class TestTestController {

    @Autowired
    TestTestService testTestService;

    @GetMapping("/1")
    public HashMap<String, String> test1() {
        HashMap<String, String> res = new HashMap<>();
        res.put("hello", "world");
        return testTestService.test1(res);
    }

}
