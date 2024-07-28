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
import fresh.crafts.engine.v1.entities.KEventWizardPostgresPayload;
import fresh.crafts.engine.v1.models.DBPostgres;
import fresh.crafts.engine.v1.services.DBPostgresService;
import fresh.crafts.engine.v1.utils.enums.DBPostgresSortField;

@RestController
@RequestMapping(value = "/api/v1/db-postgres", consumes = "application/json", produces = "application/json")
public class DBPostgresController {

    @Autowired
    DBPostgresService dbPostgresService;

    @PostMapping("")
    public CommonResponseDto createDB(@RequestBody DBPostgres dbPostgres) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbPostgresService.createDatabaseAndUser(res, dbPostgres);
        return res;
    }

    @GetMapping("")
    // FIXME: pagination will be added
    public Page<DBPostgres> getDB(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") DBPostgresSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort) {

        // to make the url make more sense
        // page = page - 1;
        // doesn't matter actually

        Page<DBPostgres> res = dbPostgresService.getDBs(page, pageSize, orderBy,
                sort);

        return res;
    }

    @GetMapping("/search/{query}")
    public CommonResponseDto searchDBs(@PathVariable("query") String query) {
        CommonResponseDto res = new CommonResponseDto();
        // System.err.println("[DEBUG]: DBPostgresController searchDBs, query: " +
        // query);
        res = dbPostgresService.searchDBs(res, query);
        return res;

    }

    @GetMapping("/{id}")
    public CommonResponseDto getDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbPostgresService.getDB(res, id);
        // System.err.println("[DEBUG]: DBPostgresController getDB res: " + res);
        return res;
    }

    @DeleteMapping("/{id}")
    public CommonResponseDto deleteDB(@PathVariable("id") String id) {
        CommonResponseDto res = new CommonResponseDto();
        res = dbPostgresService.deleteDB(res, id);
        return res;
    }

    @PatchMapping("/{id}")
    public CommonResponseDto updateDB(@PathVariable("id") String id,
            @RequestBody KEventWizardPostgresPayload dbPostgresPayload) {
        CommonResponseDto res = new CommonResponseDto();
        System.err.println("=====================================");
        System.err.println("[DEBUG]: DBPostgresController updateDB, id: " + id + ",dbPostgres: " + dbPostgresPayload);
        res = dbPostgresService.updateDB(res, id, dbPostgresPayload);
        System.err.println("=====================================");
        return res;
    }

    @PatchMapping("/{id}/revert")
    public CommonResponseDto revertDB(@PathVariable("id") String id) {

        System.err.println("[DEBUG]: DBPostgresController revertDB, id: " + id);
        CommonResponseDto res = new CommonResponseDto();
        res = dbPostgresService.revertChanges(res, id);
        return res;
    }

}
