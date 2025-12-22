package springframework.springbankinapp.users.repositories;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import springframework.springbankinapp.users.entities.User;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Find user by email - should find user when email exists")
    void findUserByEmail() {
        User user = createUser("user@email.com", "Test", "User");

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findUserByEmail(user.getEmail());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo(user.getEmail());
        assertThat(foundUser.get().getFirstName()).isEqualTo(user.getFirstName());
    }

    @Test
    @DisplayName("Find user by non existent email - should return empty optional")
    void findUserByNonExistentEmail() {
        Optional<User> user = userRepository.findUserByEmail("notFound@email.com");

        assertThat(user).isEmpty();
    }

    @Test
    @DisplayName("findAllByQuery - should find user by email")
    void findAllByQueryByEmail() {
        User user1 = createUser("john@email.com", "John", "Doe");
        createUser("michael@email.com", "Michael", "Ericsson");

        List<User> result = userRepository.findAllByQuery(user1.getEmail(), null, null);

        assertThat(result).hasSize(1);
        assertThat(result).contains(user1);
    }

    @Test
    @DisplayName("findAllByQuery - should find user by lastName")
    void findAllByQueryByLastName() {
        createUser("john@email.com", "John", "Doe");
        User user2 = createUser("michael@email.com", "Michael", "Ericsson");

        List<User> result = userRepository.findAllByQuery(null, user2.getLastName(), null);

        assertThat(result).hasSize(1);
        assertThat(result).contains(user2);
    }

    @Test
    @DisplayName("findAllByQuery - should return all users when all params are null")
    void findAllByQueryAllParamsNull() {
        User user1 = createUser("john@email.com", "John", "Doe");
        User user2 = createUser("michael@email.com", "Michael", "Ericsson");

        List<User> result = userRepository.findAllByQuery(null, null, null);

        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    @DisplayName("findAllByQuery - should return user by partial match")
    void findUserByPartialMatch() {
        User user1 = createUser("john@email.com", "John", "Doe");
        createUser("michael@email.com", "Michael", "Ericsson");

        List<User> result = userRepository.findAllByQuery("joh", null, null);

        assertThat(result).hasSize(1);
        assertThat(result).containsExactlyInAnyOrder(user1);
    }

    private User createUser(String email, String firstName, String lastName) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPhoneNumber("+123456789321");
        user.setKeycloakUserId("keycloak-" + email);
        return userRepository.save(user);
    }
}
