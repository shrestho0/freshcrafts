package fresh.crafts.engine.v1.services;

import org.springframework.beans.factory.annotation.Autowired;
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

}
