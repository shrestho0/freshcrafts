package fresh.crafts.engine.v1.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.entities.WizardMySQLKEventPayload;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.DBMysqlRepository;
import fresh.crafts.engine.v1.utils.enums.DBMysqlCreationStatuses;
import fresh.crafts.engine.v1.utils.enums.DBMysqlSortField;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.WizardMySQLEventCommands;

@Service
public class DBMysqlService {

    @Autowired
    private DBMysqlRepository dbMysqlRepository;

    @Autowired
    private MessageProducer messageProducer;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBMysql dbMysqlRequested) {
        // check if db name already exists

        System.out.println("DBMysqlService.createDatabaseAndUser()" + dbMysqlRequested);
        HashMap<String, String> errors = new HashMap<>();

        // TODO: Validations

        if (dbMysqlRepository.findBydbName(dbMysqlRequested.getDbName()) != null) {
            res.setSuccess(false);
            errors.put("dbName", "Database name already exists");
        }

        if (dbMysqlRepository.findBydbUser(dbMysqlRequested.getDbUser()) != null) {
            res.setSuccess(false);
            errors.put("dbUser", "Database user already exists");
        }

        if (!errors.isEmpty()) {
            res.setErrors(errors);
            return res;
        }

        // String newDBId = dbMysqlRequested.getId();

        try {

            // Status will be set from from here
            dbMysqlRequested.setStatus(DBMysqlCreationStatuses.REQUESTED);

            // Saving requested thing to database
            DBMysql createdX = dbMysqlRepository.save(dbMysqlRequested);

            KEvent kevent = new KEvent();
            WizardMySQLKEventPayload payload = new WizardMySQLKEventPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MYSQL);
            payload.setCommand(WizardMySQLEventCommands.CREATE_USER_AND_DB);
            payload.setDbModelId(createdX.getId());
            payload.setDbName(createdX.getDbName());
            payload.setDbUser(createdX.getDbUser());
            payload.setDbPassword(createdX.getDbPassword());

            kevent.setPayload(payload);

            // sending command with payloads to the mysql_wizard
            messageProducer.sendEvent(kevent);

            // createdX.setStatus(DBMysqlCreationType.REQUESTED);

            res.setPayload(createdX);

            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.out.println("DBMysqlService.createDatabaseAndUser() " + e);
            res.setSuccess(false);
            res.setMessage(e.getMessage());
        }

        return res;
    }

    public CommonResponseDto getDB(CommonResponseDto res, String param) {
        Optional<DBMysql> x = dbMysqlRepository.findById(param);

        if (x.isEmpty()) {
            res.setSuccess(false);
            res.setMessage("DB with id `" + param + "` not found");
            return res;
        }

        res.setPayload(x.get());
        res.setSuccess(true);

        return res;
    }

    public CommonResponseDto searchDBs(CommonResponseDto res, String query) {
        // res.setPayload(dbMysqlRepository.findByNameLi(query, query));
        // TODO: search dbs by dbName, dbUser

        res.setSuccess(true);
        res.setPayload(dbMysqlRepository.searchBydbNameOrdbUser(query));

        return res;
    }

    public Page<DBMysql> getDBs(
            int page,
            int perPage,
            DBMysqlSortField orderBy,
            Sort.Direction sort) {

        Pageable pageable = PageRequest.of(page, perPage, sort, orderBy.getDatabaseFieldName());
        return dbMysqlRepository.findAll(pageable);
        // res.setPayload(pageable);
        // return res;

        // return pageable;
    }

}
