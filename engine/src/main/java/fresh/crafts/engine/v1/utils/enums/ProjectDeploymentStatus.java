package fresh.crafts.engine.v1.utils.enums;

// Prottek state ee engine ke event send korbe
// Then, engine will send the notification to cockpit
// As, step by step kaj hobe,
// if any step fails, then, further processing will be stopped
// and, Engine ee event jabe.
public enum ProjectDeploymentStatus {
    // Requested creation is set from engine

    PRE_CREATION,
    READY_FOR_DEPLOYMENT,
    REQUESTED_DEPLOYMENT,

    DEPLOYMENT_BEING_PROCESSED,
    // BUILDING_PROJECT,
    // SETTING_UP_PM2,
    // SETTING_UP_NGINX,
    // SETTING_UP_SSL,
    // COMPLETED_CREATION,
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
