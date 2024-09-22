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
import fresh.crafts.engine.v1.entities.KEventPayloadRedWiz;
import fresh.crafts.engine.v1.models.DBRedis;
import fresh.crafts.engine.v1.models.KEvent;
import fresh.crafts.engine.v1.repositories.DBRedisRepository;
import fresh.crafts.engine.v1.utils.enums.DBRedisSortField;
import fresh.crafts.engine.v1.utils.enums.DBRedisStatus;
import fresh.crafts.engine.v1.utils.enums.KEventCommandsRedWiz;
import fresh.crafts.engine.v1.utils.enums.KEventProducers;

@Service
public class DBRedisService {

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private DBRedisRepository dbRedisRepository;

    @Autowired
    private KEventService kEventService;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBRedis dbRequested) {
        // check if db name already exists

        System.err.println("DBRedisService.createDatabaseAndUser()" +
                dbRequested);

        HashMap<String, String> errors = new HashMap<>();

        // TODO: Validations

        if (dbRedisRepository.findBydbPrefix(dbRequested.getDbPrefix()) != null) {
            res.setSuccess(false);
            errors.put("dbPrefix", "Database prefix already exists");
        }

        if (dbRedisRepository.findByUsername(dbRequested.getUsername()) != null) {
            res.setSuccess(false);
            errors.put("username", "Database user already exists");
        }

        if (!errors.isEmpty()) {
            res.setErrors(errors);
            return res;
        }

        // String newDBId = dbRequested.getId();

        try {

            // Status will be set from from here
            dbRequested.setStatus(DBRedisStatus.REQUESTED);

            // Saving requested thing to database
            DBRedis createdX = dbRedisRepository.save(dbRequested);

            KEvent kevent = new KEvent();
            KEventPayloadRedWiz payload = new KEventPayloadRedWiz();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.REDWIZ);

            payload.setCommand(KEventCommandsRedWiz.ALLOW_PREFIX_TO_USER);
            payload.setDbModelId(createdX.getId());
            payload.setDbPrefix(createdX.getDbPrefix());
            // payload.setDbName(createdX.getDbName());
            // payload.setDbUser(createdX.getDbUser());
            payload.setUsername(createdX.getUsername());
            // payload.setDbPassword(createdX.getDbPassword());
            payload.setPassword(createdX.getPassword());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);

            // sending command with payloads to the REDWIZ
            messageProducer.sendEvent(kevent);

            res.setPayload(createdX);

            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.err.println("DBRedisService.createDatabaseAndUser() " + e);
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
        Optional<DBRedis> x = dbRedisRepository.findById(param);

        if (x.isEmpty()) {
            res.setSuccess(false);
            res.setMessage("Redis Database with id `" + param + "` not found");
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
     * @return DBRedis | null
     */
    public DBRedis getById(String dbModelId) {
        if (dbModelId == null)
            return null;

        Optional<DBRedis> x = dbRedisRepository.findById(dbModelId);

        if (x.isEmpty()) {
            return null;
        }

        return x.get();

    }

    public CommonResponseDto searchDBs(CommonResponseDto res, String query) {

        res.setSuccess(true);
        res.setPayload(dbRedisRepository.searchBydbPrefixOrUsername(query));

        return res;
    }

    /**
     * Get all DBs
     * 
     * @param page    int
     * @param perPage int
     * @param orderBy DBRedisSortField
     * @param sort    Sort.Direction
     * 
     * @return Page<DBRedis>
     */
    public Page<DBRedis> getDBs(
            int page,
            int perPage,
            DBRedisSortField orderBy,
            Sort.Direction sort) {

        Pageable pageable = PageRequest.of(page, perPage, sort,
                orderBy.getDatabaseFieldName());

        return dbRedisRepository.findAll(pageable);
    }

    public DBRedis update(DBRedis DBRedis) {
        return dbRedisRepository.save(DBRedis);
    }

    /**
     * Update status of the DBRedis
     *
     * @param id
     * @param status
     * @return DBRedis | null
     */
    public DBRedis updateStatus(String id, DBRedisStatus status) {
        if (id == null || status == null)
            return null;

        Optional<DBRedis> x = dbRedisRepository.findById(id);
        if (x.isEmpty())
            return null;

        x.get().setStatus(status);

        return dbRedisRepository.save(x.get());

    }

    public CommonResponseDto revertChanges(CommonResponseDto res, String id) {
        // DBRedis db = updateStatus(id, status);
        DBRedis db = getById(id);
        if (db == null) {
            res.setSuccess(false);
            res.setMessage("DB not found");
            return res;
        }

        db.setStatus(DBRedisStatus.OK);
        db.setUpdateMessage(null);
        db.setReasonFailed(null);

        dbRedisRepository.save(db);

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

            Optional<DBRedis> x = dbRedisRepository.findById(id);
            if (x.isEmpty()) {
                throw new Exception("DB not found");
            }

            // Update status to PENDING_DELETE
            DBRedis db = x.get();
            db.setStatus(DBRedisStatus.PENDING_DELETE);
            db = dbRedisRepository.save(db);

            System.err.println("[DEBUG]: DBRedisService.deleteDB() saveddbd" + db);

            // Create event
            KEvent kevent = new KEvent();
            KEventPayloadRedWiz payload = new KEventPayloadRedWiz();

            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.REDWIZ);

            payload.setCommand(KEventCommandsRedWiz.REVOKE_ACCESS_FROM_USER);
            payload.setDbModelId(x.get().getId());

            payload.setDbPrefix(x.get().getDbPrefix());
            payload.setUsername(x.get().getUsername());

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBRedisService.deleteDB() kevent " + kevent);

            // sending command with payloads to the REDWIZ
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
    public void delete(DBRedis requestedDb) {
        dbRedisRepository.delete(requestedDb);
    }

    public void delete(String id) {
        dbRedisRepository.deleteById(id);
    }

    public CommonResponseDto updateDB(CommonResponseDto res, String id,
            KEventPayloadRedWiz payload) {

        try {
            // TODO: check if required fields are present

            DBRedis db = getById(id);

            if (db == null) {
                res.setSuccess(false);
                res.setMessage("MongoDB Database not found");
                return res;
            }

            // no same data update
            // error if olddata is same as newdata
            HashMap<String, String> errors = new HashMap<>();
            // FIXME: fix this, add validations
            // if (payload.getNewDBName() != null &&
            // payload.getNewDBName().equals(db.getDbName())) {
            // errors.put("newDBName", "newDBName is same as old dbName");
            // }
            // if (payload.getNewDBUser() != null &&
            // payload.getNewDBUser().equals(db.getDbUser())) {
            // errors.put("newDBUser", "newDBUser is same as old dbUser");
            // }
            // if (payload.getNewUserPassword() != null &&
            // payload.getNewUserPassword().equals(db.getDbPassword())) {
            // errors.put("newUserPassword", "newUserPassword is same as old dbPassword");
            // }

            // return error if any of the above errors are present
            // if (!errors.isEmpty()) {
            // res.setSuccess(false);
            // res.setErrors(errors);
            // return res;
            // }

            // update db status to UPDATE_REQUESTED
            db.setStatus(DBRedisStatus.UPDATE_REQUESTED);

            // Check what to update and set message accdordingly.
            String msg = "Requested update for redis database ";
            // if (payload.getNewDBName() != null) {
            // msg += "dbName, ";
            // }
            // if (payload.getNewDBUser() != null) {
            // msg += "dbUser, ";
            // }
            // if (payload.getNewUserPassword() != null) {
            // msg += "dbPassword, ";
            // }
            // remove last comma and space
            msg = msg.substring(0, msg.length() - 2);
            msg += " is being processed";
            db.setUpdateMessage(msg);
            db = update(db);
            if (db == null) {
                res.setSuccess(false);
                res.setMessage("Redis Database update failed");
                return res;
            }

            // set actual database details to DBRedisPayload
            payload.setCommand(KEventCommandsRedWiz.UPDATE_USER_PASSWORD);
            payload.setDbModelId(id);
            // payload.setDbName(db.getDbName());
            // payload.setDbUser(db.getDbUser());
            // // we don't need password to update it
            // // we actually need, as, it killed my 2hrs to find this thing
            // payload.setDbPassword(db.getDbPassword());

            payload.setUsername(db.getUsername());
            // payload.setPassword(db.getPassword()); // this one will be updated from
            // request

            // create event
            KEvent kevent = new KEvent();
            // set preflight stuff
            kevent.setEventSource(KEventProducers.ENGINE);
            kevent.setEventDestination(KEventProducers.REDWIZ);

            kevent.setPayload(payload);

            // save event to db
            kEventService.createOrUpdate(kevent);
            System.err.println("[DEBUG]: DBRedisService.updateDB() kevent " + kevent);

            // sending command with payloads to the REDWIZ
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
