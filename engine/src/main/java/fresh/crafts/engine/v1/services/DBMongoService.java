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
import fresh.crafts.engine.v1.entities.KEventWizardMongoPayload;
import fresh.crafts.engine.v1.models.DBMongo;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.DBMongoRepository;
import fresh.crafts.engine.v1.utils.enums.DBMongoSortField;
import fresh.crafts.engine.v1.utils.enums.DBMongoStatus;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsWizardMongo;

@Service
public class DBMongoService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private DBMongoRepository dbMongoRepository;

    @Autowired
    private KEventService kEventService;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBMongo dbRequested) {
        // check if db name already exists

        System.err.println("dbMongoService.createDatabaseAndUser()" +
                dbRequested);

        HashMap<String, String> errors = new HashMap<>();

        // TODO: Validations

        if (dbMongoRepository.findBydbName(dbRequested.getDbName()) != null) {
            res.setSuccess(false);
            errors.put("dbName", "Database name already exists");
        }

        if (dbMongoRepository.findBydbUser(dbRequested.getDbUser()) != null) {
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
            dbRequested.setStatus(DBMongoStatus.REQUESTED);

            // Saving requested thing to database
            DBMongo createdX = dbMongoRepository.save(dbRequested);

            KEvent kevent = new KEvent();
            KEventWizardMongoPayload payload = new KEventWizardMongoPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MONGO);

            payload.setCommand(KEventCommandsWizardMongo.CREATE_USER_AND_DB);
            payload.setDbModelId(createdX.getId());
            payload.setDbName(createdX.getDbName());
            payload.setDbUser(createdX.getDbUser());
            payload.setDbPassword(createdX.getDbPassword());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);

            // sending command with payloads to the WIZARD_MONGO
            messageProducer.sendEvent(kevent);

            res.setPayload(createdX);

            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.err.println("dbMongoService.createDatabaseAndUser() " + e);
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
        Optional<DBMongo> x = dbMongoRepository.findById(param);

        if (x.isEmpty()) {
            res.setSuccess(false);
            res.setMessage("MongoDB Database with id `" + param + "` not found");
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
     * @return DBMongo | null
     */
    public DBMongo getById(String dbModelId) {
        if (dbModelId == null)
            return null;

        Optional<DBMongo> x = dbMongoRepository.findById(dbModelId);

        if (x.isEmpty()) {
            return null;
        }

        return x.get();

    }

    public CommonResponseDto searchDBs(CommonResponseDto res, String query) {

        res.setSuccess(true);
        res.setPayload(dbMongoRepository.searchBydbNameOrdbUser(query));

        return res;
    }

    /**
     * Get all DBs
     * 
     * @param page    int
     * @param perPage int
     * @param orderBy dbMongoSortField
     * @param sort    Sort.Direction
     * 
     * @return Page<DBMongo>
     */
    public Page<DBMongo> getDBs(
            int page,
            int perPage,
            DBMongoSortField orderBy,
            Sort.Direction sort) {

        Pageable pageable = PageRequest.of(page, perPage, sort,
                orderBy.getDatabaseFieldName());

        return dbMongoRepository.findAll(pageable);
    }

    public DBMongo update(DBMongo dbMongo) {
        return dbMongoRepository.save(dbMongo);
    }

    /**
     * Update status of the DBMongo
     *
     * @param id
     * @param status
     * @return DBMongo | null
     */
    public DBMongo updateStatus(String id, DBMongoStatus status) {
        if (id == null || status == null)
            return null;

        Optional<DBMongo> x = dbMongoRepository.findById(id);
        if (x.isEmpty())
            return null;

        x.get().setStatus(status);

        return dbMongoRepository.save(x.get());

    }

    public CommonResponseDto revertChanges(CommonResponseDto res, String id) {
        // DBMongo db = updateStatus(id, status);
        DBMongo db = getById(id);
        if (db == null) {
            res.setSuccess(false);
            res.setMessage("DB not found");
            return res;
        }

        db.setStatus(DBMongoStatus.OK);
        db.setUpdateMessage(null);
        db.setReasonFailed(null);

        dbMongoRepository.save(db);

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

            Optional<DBMongo> x = dbMongoRepository.findById(id);
            if (x.isEmpty()) {
                throw new Exception("DB not found");
            }

            // Update status to PENDING_DELETE
            DBMongo db = x.get();
            db.setStatus(DBMongoStatus.PENDING_DELETE);
            db = dbMongoRepository.save(db);

            System.err.println("[DEBUG]: dbMongoService.deleteDB() saveddbd" + db);

            // Create event
            KEvent kevent = new KEvent();
            KEventWizardMongoPayload payload = new KEventWizardMongoPayload();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MONGO);

            payload.setCommand(KEventCommandsWizardMongo.DELETE_USER_AND_DB);
            payload.setDbModelId(x.get().getId());
            payload.setDbName(x.get().getDbName());
            payload.setDbUser(x.get().getDbUser());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: dbMongoService.deleteDB() kevent " + kevent);

            // sending command with payloads to the WIZARD_MONGO
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
    public void delete(DBMongo requestedDb) {
        dbMongoRepository.delete(requestedDb);
    }

    public void delete(String id) {
        dbMongoRepository.deleteById(id);
    }

    public CommonResponseDto updateDB(CommonResponseDto res, String id,
            KEventWizardMongoPayload payload) {

        try {
            // TODO: check if required fields are present

            DBMongo db = getById(id);

            if (db == null) {
                res.setSuccess(false);
                res.setMessage("MongoDB Database not found");
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
            db.setStatus(DBMongoStatus.UPDATE_REQUESTED);

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
                res.setMessage("MongoDB Database update failed");
                return res;
            }

            // set actual database details to dbMongoPayload
            payload.setCommand(KEventCommandsWizardMongo.UPDATE_USER_AND_DB);
            payload.setDbModelId(id);
            payload.setDbName(db.getDbName());
            payload.setDbUser(db.getDbUser());
            // we don't need password to update it

            // create event
            KEvent kevent = new KEvent();
            // set preflight stuff
            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.WIZARD_MONGO);

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: dbMongoService.updateDB() kevent " + kevent);

            // sending command with payloads to the WIZARD_MONGO
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

}
