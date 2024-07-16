package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.config.MessageProducer;
import fresh.crafts.engine.v1.dtos.CommonResponseDto;
import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.repositories.DBMysqlRepository;

@Service
public class DBMysqlService {

    @Autowired
    private DBMysqlRepository dbMysqlRepository;

    @Autowired
    private MessageProducer messageProducer;

    public CommonResponseDto createDatabaseAndUser(CommonResponseDto res, DBMysql dbMysqlRequested) {
        // check if db name already exists

        System.out.println("DBMysqlService.createDatabaseAndUser()" + dbMysqlRequested);

        if (dbMysqlRepository.findBydbName(dbMysqlRequested.getDbName()) != null) {
            res.setSuccess(false);

            res.setMessage("Database name already exists");
            return res;
        }

        if (dbMysqlRepository.findBydbUser(dbMysqlRequested.getDbUser()) != null) {
            res.setSuccess(false);
            res.setMessage("Database user already exists");
            return res;
        }

        // TODO: send create_db_and_user command with payloads to the mysql_wizard
        String newDBId = dbMysqlRequested.getId();

        try {
            DBMysql createdX = dbMysqlRepository.save(dbMysqlRequested);
            messageProducer.sendMessage("wizard_mysql", "create_db_and_user with id: " + newDBId);

            res.setData(createdX);
            res.setSuccess(true);
            res.setMessage("Requested database and user creation");

        } catch (Exception e) {
            System.out.println("DBMysqlService.createDatabaseAndUser() " + e);
            res.setSuccess(false);
            res.setMessage(e.getMessage());
        }

        return res;
    }

}
