package fresh.crafts.engine.v1.entities;

import fresh.crafts.engine.v1.types.enums.LoginTypeEnum;
import lombok.Data;


@Data
public class JwtPayload {
    private String systemUserName;
    private String systemUserEmail;
    private LoginTypeEnum provider;

}
