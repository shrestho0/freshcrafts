package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.entities.Tokens;
import fresh.crafts.engine.v1.utils.enums.LoginTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateTokenResponseDto {

    private boolean success;
    private Tokens tokens;
    private LoginTypeEnum login_type;
    private String message;

    
}
