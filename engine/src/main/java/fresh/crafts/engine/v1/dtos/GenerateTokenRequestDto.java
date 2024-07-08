package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.types.enums.LoginTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenRequestDto {
    public LoginTypeEnum provider;

    // for provider: EMAIL_PASSWORD
    public String email;
    public String password; // for EMAIL_PASSWORD auth only

    // for provider: OAUTH_GITHUB
    public String githubId;
    // for provider: OAUTH_GOOGLE
    public String googleEmail;

//    public ArrayList<String> oAuthEmails; // not sure, it may have refresh token and email if required
    
}
