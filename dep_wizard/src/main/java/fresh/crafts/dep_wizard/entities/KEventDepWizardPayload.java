package fresh.crafts.dep_wizard.entities;

import lombok.Data;

@Data
public class KEventDepWizardPayload {
    private String projectId;
    private String projectDeploymentId;
    private KEventCommandsDepWizard command;
}
