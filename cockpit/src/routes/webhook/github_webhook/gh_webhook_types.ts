// To parse this data:
//
//   import { Convert, ReqReq } from "./file";
//
//   const reqReq = Convert.toReqReq(json);

export type GithubWebhookResponseDto<T> = {
    headers: Headers;
    payload: T;
}

export type Headers = {
    "x-hub-signature-256": string;
    "content-type": string;
    "user-agent": string;
    "X-GitHub-Hook-ID": string;
    "X-GitHub-Delivery": string;
    "X-GitHub-Event": string;
    "X-GitHub-Hook-Installation-Target-Type": string;
    "X-GitHub-Hook-Installation-Target-ID": string;
}

export type Payload_of_installation = {
    action: "created" | "deleted";
    installation: Installation;
    repositories: Repository[];
    requester: null;
    sender: Sender;
}

export type Payload_of_installation_repositories = {
    action: 'added' | 'removed';
    installation: Installation;
    repository_selection: string;
    repositories_added?: Repository[]; // only present if action is 'added'
    repositories_removed?: Repository[]; // only present if action is 'removed'
    requester: null;
    sender: Sender;
}

export type Installation = {
    id: number;
    account: Sender;
    repository_selection: string;
    access_tokens_url: string;
    repositories_url: string;
    html_url: string;
    app_id: number;
    app_slug: string;
    target_id: number;
    target_type: string;
    permissions: Permissions;
    events: string[];
    created_at: Date;
    updated_at: Date;
    single_file_name: null;
    has_multiple_single_files: boolean;
    single_file_paths: any[];
    suspended_by: null;
    suspended_at: null;
}

export type Sender = {
    login: string;
    id: number;
    node_id: string;
    avatar_url: string;
    gravatar_id: string;
    url: string;
    html_url: string;
    followers_url: string;
    following_url: string;
    gists_url: string;
    starred_url: string;
    subscriptions_url: string;
    organizations_url: string;
    repos_url: string;
    events_url: string;
    received_events_url: string;
    type: string;
    site_admin: boolean;
}

export type Permissions = {
    actions: string;
    administration: string;
    checks: string;
    contents: string;
    deployments: string;
    metadata: string;
    pull_requests: string;
    repository_advisories: string;
    repository_hooks: string;
    secret_scanning_alerts: string;
    security_events: string;
    vulnerability_alerts: string;
    workflows: string;
}

export type Repository = {
    id: number;
    node_id: string;
    name: string;
    full_name: string;
    private: boolean;
}

