package fresh.crafts.engine.v1.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.DBMysql;

@Repository
public interface DBMysqlRepository extends MongoRepository<DBMysql, String> {

    DBMysql findBydbUser(String dbUser);

    DBMysql findBydbName(String dbName);

}