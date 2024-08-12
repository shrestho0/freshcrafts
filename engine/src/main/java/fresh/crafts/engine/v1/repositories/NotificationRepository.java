package fresh.crafts.engine.v1.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import fresh.crafts.engine.v1.models.Notification;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    // Page<Notification> findByExample(Query query, Pageable pageable);

}