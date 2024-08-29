package fresh.crafts.depwiz.entities;

import lombok.Data;

@Data
public class CommandServiceResult {
    private String command;
    private String output;
    private String error;
    private int exitCode;
}
