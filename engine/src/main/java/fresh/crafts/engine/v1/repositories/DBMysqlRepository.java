package fresh.crafts.engine.v1.repositories;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.DBMysql;
import fresh.crafts.engine.v1.utils.enums.DBMysqlStatus;

@Repository
public interface DBMysqlRepository extends MongoRepository<DBMysql, String> {

    Example<DBMysql> findAll = null;

    DBMysql findBydbUser(String dbUser);

    DBMysql findBydbName(String dbName);

    @Query("{ $or: [ {dbName: /?0/}, {dbUser: /?0/} ] }")
    List<DBMysql> searchBydbNameOrdbUser(String query);
    // List<DBMysql> findBydbNameLike(String dbName);

    Page<DBMysql> findAll(Pageable pageable);

    // Count By Status
    long countByStatus(DBMysqlStatus status);

}