package fresh.crafts.engine.v1.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties("statusCode")
public class CommonResponseDto {

    private Boolean success = false;
    private String message;
    private Integer statusCode = 400;

    // TODO: remove this one from everywhere
    @Deprecated
    private Object data;

    private Object payload;
    private Object payload2;
    private Object payload3;

    private Object errors;

    public CommonResponseDto(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

}
