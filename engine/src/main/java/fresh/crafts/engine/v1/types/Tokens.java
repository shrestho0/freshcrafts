package fresh.crafts.engine.v1.types;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Tokens {
    private String refresh; 
    private String access;
}
