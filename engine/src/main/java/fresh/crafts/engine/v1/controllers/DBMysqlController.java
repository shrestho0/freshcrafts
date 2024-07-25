package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.services.DBMysqlService;
import fresh.crafts.engine.v1.utils.enums.DBMysqlSortField;

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

    @GetMapping("")
    // FIXME: pagination will be added
    public Page<DBMysql> getDB(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "id") DBMysqlSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {
        // to make the url make more sense
        // page = page - 1;
        // doesn't matter actually

        Page<DBMysql> res = dbMysqlService.getDBs(page, perPage, orderBy, sort);
        // System.err.println("[DEBUG]: DBMysqlController getDBs res: " + res);
        return res;
    }

    @GetMapping("/search/{query}")
    public CommonResponseDto searchDBs(@PathVariable("query") String query) {
        CommonResponseDto res = new CommonResponseDto();
        System.err.println("[DEBUG]: DBMysqlController searchDBs, query: " + query);
        res = dbMysqlService.searchDBs(res, query);
        return res;

    }

    @GetMapping("/{id}")
    public CommonResponseDto getDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMysqlService.getDB(res, id);
        System.err.println("[DEBUG]: DBMysqlController getDB res: " + res);
        return res;
    }

}
