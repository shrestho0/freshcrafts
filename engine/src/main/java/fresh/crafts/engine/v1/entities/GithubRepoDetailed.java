package fresh.crafts.engine.v1.entities;

import lombok.Data;

@Data
public class GithubRepoDetailed {
    Integer id;
    String node_id;
    String name;
    String full_name;
    Boolean is_private;
    GithubRepoOwner owner;
    String html_url;
    String description;
    Boolean fork;
    String url;
    String forks_url;
    String keys_url;
    String collaborators_url;
    String teams_url;
    String hooks_url;
    String issue_events_url;
    String events_url;
    String assignees_url;
    String branches_url;
    String tags_url;
    String blobs_url;
    String git_tags_url;
    String git_refs_url;
    String trees_url;
    String statuses_url;
    String languages_url;
    String stargazers_url;
    String contributors_url;
    String subscribers_url;
    String subscription_url;
    String commits_url;
    String git_commits_url;
    String comments_url;
    String issue_comment_url;
    String contents_url;
    String compare_url;
    String merges_url;
    String archive_url;
    String downloads_url;
    String issues_url;
    String pulls_url;
    String milestones_url;
    String notifications_url;
    String labels_url;
    String releases_url;
    String deployments_url;
    String created_at;
    String updated_at;
    String pushed_at;
    String git_url;
    String ssh_url;
    String clone_url;
    String svn_url;
    String homepage;
    Integer size;
    Integer stargazers_count;
    Integer watchers_count;
    // String language;
    Boolean has_issues;
    Boolean has_projects;
    Boolean has_downloads;
    Boolean has_wiki;
    Boolean has_pages;
    Boolean has_discussions;
    Integer forks_count;
    // String mirror_url;
    Boolean archived;
    Boolean disabled;
    Integer open_issues_count;
    GithubRepoLicense license;
    Boolean allow_forking;
    Boolean is_template;
    Boolean web_commit_signoff_required;
    Object[] topics;
    String visibility;
    Integer forks;
    Integer open_issues;
    Integer watchers;
    String default_branch;
    GithubRepoPermissions permissions;
}
