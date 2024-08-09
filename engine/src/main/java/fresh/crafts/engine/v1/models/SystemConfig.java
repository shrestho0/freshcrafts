package fresh.crafts.engine.v1.models;

import java.util.Date;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection = "SystemConfig")
public class SystemConfig {

    @Id
    @Indexed(unique = true)
    private String id;

    // We don't need that
    private String setupKey;

    private Boolean systemUserSetupComplete;

    // These will be used as token payload
    private String SystemUserName;
    private String systemUserEmail;
    private String systemUserPasswordHash;

    // OAuth
    private Boolean systemUserOauthGoogleEnabled;
    private Boolean systemUserOauthGithubEnabled;

    // we'll use this to check, email for Google and id for GitHub
    // these will have their own classes later like ProviderGoogle, ProviderGithub
    // with their required information
    private String systemUserOAuthGithubId;
    private String systemUserOAuthGoogleEmail;

    // Parsing json will give us the result
    // TODO: this will be OauthProvider consiting oauth_email, oauth_id (if
    // possible), oauth_refresh_token
    private Map<String, Object> systemUserOauthGoogleData;
    private Map<String, Object> systemUserOauthGithubData;

    @CreatedDate
    private Date created;
    @LastModifiedDate
    private Date updated;

}
