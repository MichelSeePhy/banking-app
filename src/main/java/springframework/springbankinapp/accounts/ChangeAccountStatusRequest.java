package springframework.springbankinapp.accounts;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeAccountStatusRequest {

    @NotNull(message = "Action is required")
    AccountAction action;
}
