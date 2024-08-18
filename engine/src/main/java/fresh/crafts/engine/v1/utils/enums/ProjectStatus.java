package fresh.crafts.engine.v1.utils.enums;

public enum ProjectStatus {
    AWAIT_DEPLOY,
    AWAIT_DELETE,

    PROSESSING_SETUP,
    PROCESSING_DEPLOYMENT,
    PROCESSING_ROLLBACK,
    PROCESSING_DELETION,

    ACTIVE,
    INACTIVE,
    DELETED,

}
