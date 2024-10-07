package fresh.crafts.engine.v1.repositories;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.DBPostgres;

import fresh.crafts.engine.v1.utils.enums.DBPostgresStatus;

@Repository
public interface DBPostgresRepository extends MongoRepository<DBPostgres, String> {

    Example<DBPostgres> findAll = null;

    DBPostgres findBydbUser(String dbUser);

    DBPostgres findBydbName(String dbName);

    @Query("{ $or: [ {dbName: /?0/}, {dbUser: /?0/} ] }")
    List<DBPostgres> searchBydbNameOrdbUser(String query);

    Page<DBPostgres> findAll(Pageable pageable);

    long countByStatus(DBPostgresStatus status);

}