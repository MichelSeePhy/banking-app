package springframework.springbankinapp.users.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import springframework.springbankinapp.users.dtos.*;
import springframework.springbankinapp.users.exceptions.UserNotFoundException;
import springframework.springbankinapp.users.services.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("POST /users - should create user and return 201 CREATED")
    void createUserShouldReturnCreated() throws Exception {
        // Given
        CreateUserDto request = new CreateUserDto();
        request.setEmail("test@email.com");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setPassword("23233we");
        request.setPhoneNumber("+123456789123");

        UserResponseDto response = new UserResponseDto();
        response.setId(1L);
        response.setEmail("test@email.com");

        when(userService.createUser(any(CreateUserDto.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(httpBasic("admin", "admin")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"));

        verify(userService).createUser(any(CreateUserDto.class));
    }

    @Test
    @DisplayName("GET /users/{userId} - should return user")
    void getUserById() throws Exception {

        UserResponseDto response = new UserResponseDto();
        response.setId(1L);
        response.setEmail("test@email.com");
        response.setFirstName("Test");

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@email.com"))
                .andExpect(jsonPath("$.firstName").value("Test"));

        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("GET /users/{userId} - should return 404 when user not found")
    void getUserByIdNotFoundException() throws Exception {

        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(1L);
    }

}
