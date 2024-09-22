package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.entities.KEventPayloadRedWiz;
import fresh.crafts.engine.v1.models.DBRedis;
import fresh.crafts.engine.v1.services.DBRedisService;
import fresh.crafts.engine.v1.utils.enums.DBRedisSortField;

@RestController
@RequestMapping(value = "/api/v1/db-redis", consumes = "application/json", produces = "application/json")
public class DBRedisController {

    @Autowired
    DBRedisService dbRedisService;

    @PostMapping("")
    public CommonResponseDto createDB(@RequestBody DBRedis DBRedis) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbRedisService.createDatabaseAndUser(res, DBRedis);
        return res;
    }

    @GetMapping("")
    public Page<DBRedis> getDB(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") DBRedisSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {

        Page<DBRedis> res = dbRedisService.getDBs(page, pageSize, orderBy,
                sort);

        return res;
    }

    @GetMapping("/search/{query}")
    public CommonResponseDto searchDBs(@PathVariable("query") String query) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbRedisService.searchDBs(res, query);
        return res;

    }

    @GetMapping("/{id}")
    public CommonResponseDto getDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbRedisService.getDB(res, id);

        return res;
    }

    @DeleteMapping("/{id}")
    public CommonResponseDto deleteDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbRedisService.deleteDB(res, id);
        return res;
    }

    @PatchMapping("/{id}")
    public CommonResponseDto updateDB(@PathVariable("id") String id,
            @RequestBody KEventPayloadRedWiz DBRedisPayload) {
        CommonResponseDto res = new CommonResponseDto();
        System.err.println("=====================================");
        System.err.println("[DEBUG]: DBRedisController updateDB, id: " + id + ",DBRedis: " + DBRedisPayload);

        res = dbRedisService.updateDB(res, id, DBRedisPayload);
        System.err.println("=====================================");
        return res;
    }

    @PatchMapping("/{id}/revert")
    public CommonResponseDto revertDB(@PathVariable("id") String id) {

        System.err.println("[DEBUG]: DBRedisController revertDB, id: " + id);
        CommonResponseDto res = new CommonResponseDto();
        res = dbRedisService.revertChanges(res, id);
        return res;
    }

}
