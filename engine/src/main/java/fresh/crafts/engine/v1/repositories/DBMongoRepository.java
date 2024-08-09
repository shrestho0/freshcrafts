package fresh.crafts.engine.v1.repositories;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.DBMongo;

@Repository
public interface DBMongoRepository extends MongoRepository<DBMongo, String> {

    Example<DBMongo> findAll = null;

    DBMongo findBydbUser(String dbUser);

    DBMongo findBydbName(String dbName);

    @Query("{ $or: [ {dbName: /?0/}, {dbUser: /?0/} ] }")
    List<DBMongo> searchBydbNameOrdbUser(String query);

    Page<DBMongo> findAll(Pageable pageable);

}