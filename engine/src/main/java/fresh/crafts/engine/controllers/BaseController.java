package fresh.crafts.engine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1")
public class BaseController {

    @GetMapping(path={"/", ""})
    public ResponseEntity<Object> index() {

        return ResponseEntity.ok("Welcome to Fresh Crafts Engine API");

    }

}
