package fresh.crafts.engine.v1.dtos;

import java.util.List;

import fresh.crafts.engine.v1.types.enums.OAuthProviderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProvidersResponseDto {

    private boolean success;
    private List<OAuthProviderEnum> providers;
    
    
}
