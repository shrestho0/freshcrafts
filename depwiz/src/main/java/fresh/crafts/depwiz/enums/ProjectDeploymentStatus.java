package fresh.crafts.depwiz.enums;

public enum ProjectDeploymentStatus {
    // Requested creation is set from engine

    PRE_CREATION,
    READY_FOR_DEPLOYMENT,
    READY_FOR_UPDATE,

    REQUESTED_DEPLOYMENT,
    REQUESTED_REDEPLOYMENT,

    DEPLOYMENT_BEING_PROCESSED,
    DEPLOYMENT_COMPLETED,
    DEPLOYMENT_FAILED,

    REQUESTED_DELETION,
    DELETING_DEPLOYMENT,
    // on this event, engine will delete the project and send notification
    // accordingly
    COMPLETED_DELETION,

    // FIXME: only one type of fail will be ok
    // FAILED_BUILD,
    // FAILED_PM2_SETUP,
    // FAILED_NGINX_SETUP,
    // FAILED_SSL_SETUP,

}
