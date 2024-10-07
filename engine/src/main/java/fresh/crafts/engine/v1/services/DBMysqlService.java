package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.entities.KEventWizardMySQLPayload;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.DBMysqlRepository;
import fresh.crafts.engine.v1.utils.enums.DBMysqlStatus;
import fresh.crafts.engine.v1.utils.enums.DBMysqlSortField;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardMySQL;

@Service
public class DBMysqlService {

    @Autowired
    private DBMysqlRepository dbMysqlRepository;

    @Autowired
    private MessageProducer messageProducer;
    @Autowired
    private KEventService kEventService;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBMysql dbMysqlRequested) {
        // check if db name already exists

        System.err.println("DBMysqlService.createDatabaseAndUser()" + dbMysqlRequested);
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
            dbMysqlRequested.setStatus(DBMysqlStatus.REQUESTED);

            // Saving requested thing to database
            // FIXME: Uncomment next line; tempoary db no save
            DBMysql createdX = dbMysqlRepository.save(dbMysqlRequested);
            // FIXME: Delete next line; tempoary db no save
            // DBMysql createdX = dbMysqlRequested;

            KEvent kevent = new KEvent();
            KEventWizardMySQLPayload payload = new KEventWizardMySQLPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MYSQL);

            payload.setCommand(KEventCommandsWizardMySQL.CREATE_USER_AND_DB);
            payload.setDbModelId(createdX.getId());
            payload.setDbName(createdX.getDbName());
            payload.setDbUser(createdX.getDbUser());
            payload.setDbPassword(createdX.getDbPassword());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);

            // sending command with payloads to the mysql_wizard
            messageProducer.sendEvent(kevent);

            // createdX.setStatus(DBMysqlCreationType.REQUESTED);

            res.setPayload(createdX);

            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.err.println("DBMysqlService.createDatabaseAndUser() " + e);
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

    public DBMysql getById(String dbModelId) {
        if (dbModelId == null)
            return null;

        Optional<DBMysql> x = dbMysqlRepository.findById(dbModelId);

        if (x.isEmpty()) {
            return null;
        }

        return x.get();

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

    // Updates
    public DBMysql update(DBMysql dbMysql) {
        return dbMysqlRepository.save(dbMysql);
    }

    /**
     * Update status of the DBMysql
     * 
     * @param id
     * @param status
     * @return DBMysql | null [if fails]
     */
    public DBMysql updateStatus(String id, DBMysqlStatus status) {
        if (id == null || status == null)
            return null;

        Optional<DBMysql> x = dbMysqlRepository.findById(id);
        if (x.isEmpty())
            return null;

        x.get().setStatus(status);

        return dbMysqlRepository.save(x.get());

    }

    public CommonResponseDto revertChanges(CommonResponseDto res, String id) {
        // DBMysql db = updateStatus(id, status);
        DBMysql db = getById(id);
        if (db == null) {
            res.setSuccess(false);
            res.setMessage("DB not found");
            return res;
        }

        db.setStatus(DBMysqlStatus.OK);
        db.setUpdateMessage(null);
        db.setReasonFailed(null);

        dbMysqlRepository.save(db);

        res.setSuccess(true);
        res.setPayload(db);
        return res;
    }

    /*
     * Delete DB
     * Updates db status to PENDING_DELETE
     * Sends a event to wizard to delete the db
     */
    public CommonResponseDto deleteDB(CommonResponseDto res, String id) {
        try {
            if (id == null) {
                throw new IllegalArgumentException("DB id not found");
            }

            Optional<DBMysql> x = dbMysqlRepository.findById(id);
            if (x.isEmpty()) {
                throw new Exception("DB not found");
            }

            // Update status to PENDING_DELETE
            DBMysql db = x.get();
            db.setStatus(DBMysqlStatus.PENDING_DELETE);
            db = dbMysqlRepository.save(db);

            System.err.println("[DEBUG]: DBMysqlService.deleteDB() saveddbd" + db);

            // Create event
            KEvent kevent = new KEvent();
            KEventWizardMySQLPayload payload = new KEventWizardMySQLPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MYSQL);

            payload.setCommand(KEventCommandsWizardMySQL.DELETE_USER_AND_DB);
            payload.setDbModelId(x.get().getId());
            payload.setDbName(x.get().getDbName());
            payload.setDbUser(x.get().getDbUser());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBMysqlService.deleteDB() kevent " + kevent);

            // sending command with payloads to the mysql_wizard
            messageProducer.sendEvent(kevent);

            res.setMessage("DB deletion requested");
            res.setSuccess(true);
            return res;
        } catch (Exception e) {
            res.setSuccess(false);
            res.setMessage(e.getMessage());
            return res;
        }
    }

    // Delete
    public void delete(DBMysql requestedDbMysql) {
        dbMysqlRepository.delete(requestedDbMysql);
    }

    public void delete(String id) {
        dbMysqlRepository.deleteById(id);
    }

    public CommonResponseDto updateDB(CommonResponseDto res, String id, KEventWizardMySQLPayload payload) {

        try {

            // get db by id
            // if (id == null) {
            // throw new IllegalArgumentException("DB id not found");
            // } // pathvariable id won't be null

            // check if required fields are present

            DBMysql db = getById(id);

            if (db == null) {
                res.setSuccess(false);
                res.setMessage("DB not found");
                return res;
            }

            // no same data update
            // error if olddata is same as newdata
            HashMap<String, String> errors = new HashMap<>();
            if (payload.getNewDBName() != null && payload.getNewDBName().equals(db.getDbName())) {
                errors.put("newDBName", "newDBName is same as old dbName");
            }
            if (payload.getNewDBUser() != null && payload.getNewDBUser().equals(db.getDbUser())) {
                errors.put("newDBUser", "newDBUser is same as old dbUser");
            }
            if (payload.getNewUserPassword() != null && payload.getNewUserPassword().equals(db.getDbPassword())) {
                errors.put("newUserPassword", "newUserPassword is same as old dbPassword");
            }

            // return error if any of the above errors are present
            if (!errors.isEmpty()) {
                res.setSuccess(false);
                res.setErrors(errors);
                return res;
            }

            // update db status to UPDATE_REQUESTED
            // this.updateStatus(db.getId(), DBMysqlStatus.UPDATE_REQUESTED);
            db.setStatus(DBMysqlStatus.UPDATE_REQUESTED);
            // Check what to update and set message accdordingly.
            String msg = "Requested update for: ";
            if (payload.getNewDBName() != null) {
                msg += "dbName, ";
            }
            if (payload.getNewDBUser() != null) {
                msg += "dbUser, ";
            }
            if (payload.getNewUserPassword() != null) {
                msg += "dbPassword, ";
            }
            // remove last comma and space
            msg = msg.substring(0, msg.length() - 2);
            msg += " is being processed";
            db.setUpdateMessage(msg);
            db = update(db);
            if (db == null) {
                res.setSuccess(false);
                res.setMessage("DB update failed");
                return res;
            }

            // set actual database details to dbMysqlPayload
            payload.setCommand(KEventCommandsWizardMySQL.UPDATE_USER_AND_DB);
            payload.setDbModelId(id);
            payload.setDbName(db.getDbName());
            payload.setDbUser(db.getDbUser());
            // we don't need password to update it

            // create event
            KEvent kevent = new KEvent();
            // set preflight stuff
            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MYSQL);

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBMysqlService.updateDB() kevent " + kevent);

            // sending command with payloads to the mysql_wizard
            messageProducer.sendEvent(kevent);

            res.setSuccess(true);
            res.setMessage(msg);
            // res.setPayload(db); // response ee payload er dorkar nei, msg will work
        } catch (Exception e) {
            res.setSuccess(false);
            res.setMessage(e.getMessage());

        }
        return res;
    }

    public long getActiveDBCount() {
        return dbMysqlRepository.countByStatus(DBMysqlStatus.OK);
    }

}
