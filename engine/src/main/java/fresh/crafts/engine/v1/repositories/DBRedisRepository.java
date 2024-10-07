package fresh.crafts.engine.v1.repositories;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.DBRedis;
import fresh.crafts.engine.v1.utils.enums.DBRedisStatus;

@Repository
public interface DBRedisRepository extends MongoRepository<DBRedis, String> {

    Example<DBRedis> findAll = null;

    DBRedis findByUsername(String username);

    DBRedis findBydbPrefix(String dbPrefix);

    @Query("{ $or: [ {dbPrefix: /?0/}, {username: /?0/} ] }")
    List<DBRedis> searchBydbPrefixOrUsername(String query);

    Page<DBRedis> findAll(Pageable pageable);

    long countByStatus(DBRedisStatus status);

}