package fresh.crafts.engine.v1.entities;

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

    String default_branch_commit_sha;
    String default_branch_commit_date;
    String owner_login;
    String owner_id;

}
