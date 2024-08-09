package fresh.crafts.engine.v1.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBMongoSortField {
    id("id"),
    dbName("dbName"),
    dbUser("dbUser");

    private final String databaseFieldName;
}