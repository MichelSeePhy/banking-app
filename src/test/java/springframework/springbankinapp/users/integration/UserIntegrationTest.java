package springframework.springbankinapp.users.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import springframework.springbankinapp.auth.*;
import springframework.springbankinapp.users.dtos.CreateUserDto;
import springframework.springbankinapp.users.entities.User;
import springframework.springbankinapp.users.repositories.UserRepository;
import springframework.springbankinapp.users.services.PermissionService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private KeycloakService keycloakService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        when(keycloakService.createUser(any())).thenReturn("test-keycloak-id");
    }

    @Test
    @DisplayName("POST /users - should create user in database")
    void createUserShouldPersistToDatabase() throws Exception {
        CreateUserDto request = createUserDto("integration@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        var savedUser = userRepository.findUserByEmail("integration@test.com");

        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("Integration");
        assertThat(savedUser.get().getLastName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("POST /users - duplicated user throws exception")
    void duplicatedUserThrowsException() throws Exception {
        CreateUserDto request = createUserDto("duplicated@test.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("duplicated@test.com"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        var savedUser = userRepository.findUserByEmail("duplicated@test.com");
        var allUsers = userRepository.findAllByQuery("duplicated@test.com", null, null);
        assertThat(allUsers).hasSize(1);
        assertThat(savedUser).isPresent();
        assertThat(savedUser.get().getFirstName()).isEqualTo("Integration");
        assertThat(savedUser.get().getLastName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("GET /users/{id} - should return user by id")
    @WithMockUser
    void getUserById() throws Exception {
        User user = new User();
        user.setEmail("test@email.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPhoneNumber("+123456789");
        user.setKeycloakUserId("keycloak-123");

        User saved = userRepository.save(user);

        when(authService.getCurrentUser()).thenReturn(saved);
        when(permissionService.canViewUser(any(), any())).thenReturn(true);

        mockMvc.perform(get("/users/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));

    }

    private CreateUserDto createUserDto(String email) {
        CreateUserDto user = new CreateUserDto();
        user.setEmail(email);
        user.setFirstName("Integration");
        user.setLastName("Test");
        user.setPassword("password123");
        user.setPhoneNumber("+123456789123");
        return user;
    }
}
