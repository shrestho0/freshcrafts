package fresh.crafts.engine.v1.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fresh.crafts.engine.v1.models.Notification;
import fresh.crafts.engine.v1.services.NotificationService;
import fresh.crafts.engine.v1.utils.enums.NotificationSortField;
import fresh.crafts.engine.v1.utils.enums.NotificationType;
import org.springframework.data.domain.Pageable;

@RestController
@RequestMapping(value = "/api/v1/notifications", consumes = "application/json", produces = "application/json")
public class NotificationController {
    // Get paginated notifications

    @Autowired
    private NotificationService notificationService;

    @GetMapping("")
    public Page<Notification> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") NotificationSortField orderBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction sort,
            @RequestParam(required = false) Boolean markedAsRead,
            @RequestParam(required = false) NotificationType type) {

        // no search on notifications, cause, that's the plan

        Pageable pageable = PageRequest.of(page, pageSize, sort, orderBy.getNotificationFieldName());

        // Query query = new Query().with(pageable);

        // if (markedAsRead != null) {
        // query.addCriteria(Criteria.where("markedAsRead").is(markedAsRead));
        // }

        // if (type != null) {
        // query.addCriteria(Criteria.where("type").is(type));
        // }

        Page<Notification> res = notificationService.getAllPaginated(pageable);
        System.out.println("res: " + res);
        return res;
    }
}
