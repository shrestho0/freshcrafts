package fresh.crafts.engine.v1.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DBRedisSortField {
    id("id"),
    dbPrefix("dbPrefix"),
    username("username");

    private final String databaseFieldName;
}