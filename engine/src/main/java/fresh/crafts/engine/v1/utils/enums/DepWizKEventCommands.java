package fresh.crafts.engine.v1.utils.enums;

public enum DepWizKEventCommands {
    DEPLOY, // first deploy
    RE_DEPLOY, // re-deploy for inactive deploy
    DELETE_DEPLOYMENTS,
    UPDATE_DEPLOYMENT, // undeploy active, deploy current
    ROLLFORWARD, // undeploy active, deploy current
    ROLLBACK, // undeploy active, deploy current
    MODIFY_DOMAINS, // modify domain, add, remove etc

    FEEDBACK_DEPLOYMENT,
    FEEDBACK_RE_DEPLOYMENT,
    FEEDBACK_DELETE_DEPLOYMENTS,
    FEEDBACK_UPDATE_DEPLOYMENT,
    FEEDBACK_ROLLFORWARD,
    FEEDBACK_ROLLBACK,
    FEEDBACK_MODIFY_DOMAINS,
}
