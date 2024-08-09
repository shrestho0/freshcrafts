package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.entities.KEventWizardMongoPayload;
import fresh.crafts.engine.v1.models.DBMongo;
import fresh.crafts.engine.v1.services.DBMongoService;
import fresh.crafts.engine.v1.utils.enums.DBMongoSortField;

@RestController
@RequestMapping(value = "/api/v1/db-mongo", consumes = "application/json", produces = "application/json")
public class DBMongoController {

    @Autowired
    DBMongoService dbMongoService;

    @PostMapping("")
    public CommonResponseDto createDB(@RequestBody DBMongo dbMongo) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMongoService.createDatabaseAndUser(res, dbMongo);
        return res;
    }

    @GetMapping("")
    public Page<DBMongo> getDB(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") DBMongoSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {

        Page<DBMongo> res = dbMongoService.getDBs(page, pageSize, orderBy,
                sort);

        return res;
    }

    @GetMapping("/search/{query}")
    public CommonResponseDto searchDBs(@PathVariable("query") String query) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMongoService.searchDBs(res, query);
        return res;

    }

    @GetMapping("/{id}")
    public CommonResponseDto getDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMongoService.getDB(res, id);

        return res;
    }

    @DeleteMapping("/{id}")
    public CommonResponseDto deleteDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbMongoService.deleteDB(res, id);
        return res;
    }

    @PatchMapping("/{id}")
    public CommonResponseDto updateDB(@PathVariable("id") String id,
            @RequestBody KEventWizardMongoPayload dbMongoPayload) {
        CommonResponseDto res = new CommonResponseDto();
        System.err.println("=====================================");
        System.err.println("[DEBUG]: DBMongoController updateDB, id: " + id + ",dbMongo: " + dbMongoPayload);
        res = dbMongoService.updateDB(res, id, dbMongoPayload);
        System.err.println("=====================================");
        return res;
    }

    @PatchMapping("/{id}/revert")
    public CommonResponseDto revertDB(@PathVariable("id") String id) {

        System.err.println("[DEBUG]: DBMongoController revertDB, id: " + id);
        CommonResponseDto res = new CommonResponseDto();
        res = dbMongoService.revertChanges(res, id);
        return res;
    }

}
