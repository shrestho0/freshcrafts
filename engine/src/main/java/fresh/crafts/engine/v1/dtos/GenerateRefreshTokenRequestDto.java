package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.utils.enums.LoginTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateRefreshTokenRequestDto {
    private String refreshToken;
    private LoginTypeEnum provider;
}
