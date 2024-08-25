package fresh.crafts.dep_wizard.utils.enums;

import de.huxhorn.sulky.ulid.ULID;

public class UlidGenerator {
    private static final ULID ulid = new ULID();

    public static String generate() {
        return ulid.nextULID();
    }
}