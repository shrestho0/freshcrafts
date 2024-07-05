package fresh.crafts.engine.v1.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResponseDto {
    private Boolean success;
    private String message;
    private Object data;
    
}
