package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.services.DBMysqlService;

@RestController
@RequestMapping(value = "/api/v1/db-mysql", consumes = "application/json", produces = "application/json")
public class DBMysqlController {

    @Autowired
    DBMysqlService dbMysqlService;

    @PostMapping("")
    public CommonResponseDto createDB(@RequestBody DBMysql dbMysql) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMysqlService.createDatabaseAndUser(res, dbMysql);
        return res;
    }
}
