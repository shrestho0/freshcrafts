
package fresh.crafts.wiz_mysql.utils;

import de.huxhorn.sulky.ulid.ULID;

public class UlidGenerator {
    private static final ULID ulid = new ULID();

    public static String generate() {
        return ulid.nextULID();
    }
}