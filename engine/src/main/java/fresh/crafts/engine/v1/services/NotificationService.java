package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import fresh.crafts.engine.v1.models.Notification;
import fresh.crafts.engine.v1.repositories.NotificationRepository;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createOrUpdate(Notification noti) {
        return noti != null ? notificationRepository.save(noti) : noti;
    }

    public Page<Notification> getAllPaginated(Pageable pageable) {
        // Page<Notification> notifications =
        // notificationRepository.findByExample(query, pageable);
        Page<Notification> notifications;

        notifications = notificationRepository.findAll(pageable);

        return notifications;
    }

}
