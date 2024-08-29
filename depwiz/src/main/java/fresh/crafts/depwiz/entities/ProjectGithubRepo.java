package fresh.crafts.depwiz.entities;

import lombok.Data;

@Data
public class ProjectGithubRepo {
    Integer id;
    String name;
    String fullName;
    Boolean isPrivate;

    String downloadsUrl;
    String tarDownloadUrl;
    String defaultBranch;

    String deploymentsUrl;

}