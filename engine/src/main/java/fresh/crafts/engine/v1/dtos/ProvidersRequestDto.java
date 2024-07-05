package fresh.crafts.engine.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProvidersRequestDto {
 
    private OAuthProviderEnum provider;
    
    private String token;
    private String userInfo;
    private String data;
    
}
