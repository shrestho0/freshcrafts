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

    // TODO: remove this one from everywhere
    @Deprecated
    private Object data;

    private Object payload;
    private Object errors;

}
