package springframework.springbankinapp.users.dtos;

import lombok.Data;
import springframework.springbankinapp.users.Role;

@Data
public class ChangeRoleRequest {

    Role newRole;

}
