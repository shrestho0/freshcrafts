package fresh.crafts.engine.v1.dtos;

import fresh.crafts.engine.v1.entities.Tokens;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenerateRefreshTokenResponseDto {

    private boolean success;
    private Tokens tokens;
    private String message;
}
