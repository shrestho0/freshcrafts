
package fresh.crafts.engine.v1.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum NotificationSortField {
    // id("id"),
    // dbName("dbName"),
    // dbUser("dbUser");
    id("id");

    private final String notificationFieldName;
}