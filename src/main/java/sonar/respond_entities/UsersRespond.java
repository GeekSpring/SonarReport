package sonar.respond_entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sonar.entities.User;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRespond {
    private List<User> users;
}
