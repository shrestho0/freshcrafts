package fresh.crafts.engine.v1.dtos;

import java.util.List;

import fresh.crafts.engine.v1.utils.enums.AuthProviderType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProvidersResponseDto {

    private boolean success;
    private List<AuthProviderType> providers;
    private String message;
    
    
}
