package fresh.crafts.engine.v1.services;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.entities.KEventWizardPostgresPayload;
import fresh.crafts.engine.v1.models.DBPostgres;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.DBPostgresRepository;
import fresh.crafts.engine.v1.utils.enums.DBPostgresSortField;
import fresh.crafts.engine.v1.utils.enums.DBPostgresStatus;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardPostgres;

@Service
public class DBPostgresService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private DBPostgresRepository dbPostgresRepository;

    @Autowired
    private KEventService kEventService;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBPostgres dbRequested) {
        // check if db name already exists

        System.err.println("DBPostgresService.createDatabaseAndUser()" +
                dbRequested);

        HashMap<String, String> errors = new HashMap<>();

        // TODO: Validations

        if (dbPostgresRepository.findBydbName(dbRequested.getDbName()) != null) {
            res.setSuccess(false);
            errors.put("dbName", "Database name already exists");
        }

        if (dbPostgresRepository.findBydbUser(dbRequested.getDbUser()) != null) {
            res.setSuccess(false);
            errors.put("dbUser", "Database user already exists");
        }

        if (!errors.isEmpty()) {
            res.setErrors(errors);
            return res;
        }

        // String newDBId = dbRequested.getId();

        try {

            // Status will be set from from here
            dbRequested.setStatus(DBPostgresStatus.REQUESTED);

            // Saving requested thing to database
            DBPostgres createdX = dbPostgresRepository.save(dbRequested);

            KEvent kevent = new KEvent();
            KEventWizardPostgresPayload payload = new KEventWizardPostgresPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_POSTGRES);

            payload.setCommand(KEventCommandsWizardPostgres.CREATE_USER_AND_DB);
            payload.setDbModelId(createdX.getId());
            payload.setDbName(createdX.getDbName());
            payload.setDbUser(createdX.getDbUser());
            payload.setDbPassword(createdX.getDbPassword());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);

            // sending command with payloads to the WIZARD_POSTGRES
            messageProducer.sendEvent(kevent);

            res.setPayload(createdX);

            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.err.println("DBPostgresService.createDatabaseAndUser() " + e);
            res.setSuccess(false);
            res.setMessage(e.getMessage());
        }

        return res;
    }

    /*
     * getDB Response
     * 
     * @param res CommonResponseDto
     * 
     * @param param String
     * 
     * @return CommonResponseDto
     */
    public CommonResponseDto getDB(CommonResponseDto res, String param) {
        Optional<DBPostgres> x = dbPostgresRepository.findById(param);

        if (x.isEmpty()) {
            res.setSuccess(false);
            res.setMessage("Postgres DB with id `" + param + "` not found");
            return res;
        }

        res.setPayload(x.get());
        res.setSuccess(true);

        return res;
    }

    /**
     * Get DB by id
     * 
     * @param dbModelId
     * @return DBPostgres | null
     */
    public DBPostgres getById(String dbModelId) {
        if (dbModelId == null)
            return null;

        Optional<DBPostgres> x = dbPostgresRepository.findById(dbModelId);

        if (x.isEmpty()) {
            return null;
        }

        return x.get();

    }

    public CommonResponseDto searchDBs(CommonResponseDto res, String query) {

        res.setSuccess(true);
        res.setPayload(dbPostgresRepository.searchBydbNameOrdbUser(query));

        return res;
    }

    /**
     * Get all DBs
     * 
     * @param page    int
     * @param perPage int
     * @param orderBy DBPostgresSortField
     * @param sort    Sort.Direction
     * 
     * @return Page<DBPostgres>
     */
    public Page<DBPostgres> getDBs(
            int page,
            int perPage,
            DBPostgresSortField orderBy,
            Sort.Direction sort) {

        Pageable pageable = PageRequest.of(page, perPage, sort,
                orderBy.getDatabaseFieldName());

        return dbPostgresRepository.findAll(pageable);
    }

    /**
     * Update DBPostgres
     *
     * @param dbPostgres
     * @return DBPostgres
     * @throws Exception
     */
    public DBPostgres update(DBPostgres dbPostgres) {
        return dbPostgresRepository.save(dbPostgres);
    }

    /**
     * Update status of the DBPostgres
     *
     * @param id
     * @param status
     * @return DBPostgres | null
     */
    public DBPostgres updateStatus(String id, DBPostgresStatus status) {
        if (id == null || status == null)
            return null;

        Optional<DBPostgres> x = dbPostgresRepository.findById(id);
        if (x.isEmpty())
            return null;

        x.get().setStatus(status);

        return dbPostgresRepository.save(x.get());

    }

    public CommonResponseDto revertChanges(CommonResponseDto res, String id) {
        // DBPostgres db = updateStatus(id, status);
        DBPostgres db = getById(id);
        if (db == null) {
            res.setSuccess(false);
            res.setMessage("DB not found");
            return res;
        }

        db.setStatus(DBPostgresStatus.OK);
        db.setUpdateMessage(null);
        db.setReasonFailed(null);

        dbPostgresRepository.save(db);

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

            Optional<DBPostgres> x = dbPostgresRepository.findById(id);
            if (x.isEmpty()) {
                throw new Exception("DB not found");
            }

            // Update status to PENDING_DELETE
            DBPostgres db = x.get();
            db.setStatus(DBPostgresStatus.PENDING_DELETE);
            db = dbPostgresRepository.save(db);

            System.err.println("[DEBUG]: DBPostgresService.deleteDB() saveddbd" + db);

            // Create event
            KEvent kevent = new KEvent();
            KEventWizardPostgresPayload payload = new KEventWizardPostgresPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_POSTGRES);

            payload.setCommand(KEventCommandsWizardPostgres.DELETE_USER_AND_DB);
            payload.setDbModelId(x.get().getId());
            payload.setDbName(x.get().getDbName());
            payload.setDbUser(x.get().getDbUser());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBPostgresService.deleteDB() kevent " + kevent);

            // sending command with payloads to the WIZARD_POSTGRES
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
    public void delete(DBPostgres requestedDb) {
        dbPostgresRepository.delete(requestedDb);
    }

    public void delete(String id) {
        dbPostgresRepository.deleteById(id);
    }

    public CommonResponseDto updateDB(CommonResponseDto res, String id,
            KEventWizardPostgresPayload payload) {

        try {
            // TODO: check if required fields are present

            DBPostgres db = getById(id);

            if (db == null) {
                res.setSuccess(false);
                res.setMessage("Postgres DB not found");
                return res;
            }

            // no same data update
            // error if olddata is same as newdata
            HashMap<String, String> errors = new HashMap<>();
            if (payload.getNewDBName() != null &&
                    payload.getNewDBName().equals(db.getDbName())) {
                errors.put("newDBName", "newDBName is same as old dbName");
            }
            if (payload.getNewDBUser() != null &&
                    payload.getNewDBUser().equals(db.getDbUser())) {
                errors.put("newDBUser", "newDBUser is same as old dbUser");
            }
            if (payload.getNewUserPassword() != null &&
                    payload.getNewUserPassword().equals(db.getDbPassword())) {
                errors.put("newUserPassword", "newUserPassword is same as old dbPassword");
            }

            // return error if any of the above errors are present
            if (!errors.isEmpty()) {
                res.setSuccess(false);
                res.setErrors(errors);
                return res;
            }

            // update db status to UPDATE_REQUESTED
            db.setStatus(DBPostgresStatus.UPDATE_REQUESTED);

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
                res.setMessage("Postgres DB update failed");
                return res;
            }

            // set actual database details to dbPostgresPayload
            payload.setCommand(KEventCommandsWizardPostgres.UPDATE_USER_AND_DB);
            payload.setDbModelId(id);
            payload.setDbName(db.getDbName());
            payload.setDbUser(db.getDbUser());
            // we don't need password to update it

            // create event
            KEvent kevent = new KEvent();
            // set preflight stuff
            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_POSTGRES);

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBPostgresService.updateDB() kevent " + kevent);

            // sending command with payloads to the WIZARD_POSTGRES
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
        return dbPostgresRepository.countByStatus(DBPostgresStatus.OK);
    }

}
