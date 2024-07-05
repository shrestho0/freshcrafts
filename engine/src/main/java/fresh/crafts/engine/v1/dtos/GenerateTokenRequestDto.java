package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.types.enums.LoginTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenRequestDto {
    private LoginTypeEnum login_type;
    private String email;
    private String password;
    private Object oauthData;
    
}
