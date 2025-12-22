package springframework.springbankinapp.users.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;
import springframework.springbankinapp.auth.*;
import springframework.springbankinapp.users.UserMapper;
import springframework.springbankinapp.users.dtos.*;
import springframework.springbankinapp.users.entities.User;
import springframework.springbankinapp.users.events.*;
import springframework.springbankinapp.users.exceptions.UserAlreadyExistsException;
import springframework.springbankinapp.users.repositories.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private AuthService authService;

    @Mock
    private PermissionService permissionService;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("setActive: Throws AccessDeniedException when user is not an admin")
    void setActive_nonAdmin_throws() {
        Long targetUserId = 2L;
        String action = "activate";

        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(targetUserId);
        targetUser.setActive(false);
        targetUser.setKeycloakUserId("keycloak-123");

        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.getUserById(targetUserId)).thenReturn(Optional.of(targetUser));
        when(permissionService.canChangeUserActiveState(any(User.class), any(User.class)))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> {
            userService.setActive(targetUserId, action);
        });

        verify(keycloakService, never()).setUserEnabled(anyString(), anyBoolean());
    }

    @Test
    @DisplayName("setActive: Should sync with Keycloak and save user when admin activates a user")
    void setActive_adminActivatesUser() {

        User targetUser = new User();
        targetUser.setActive(false);
        targetUser.setId(2L);
        targetUser.setKeycloakUserId("keycloak-123");

        String action = "activate";

        User currentUser = new User();
        currentUser.setId(1L);

        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.getUserById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(permissionService.canChangeUserActiveState(any(User.class), any(User.class))).thenReturn(true);

        userService.setActive(targetUser.getId(), action);

        verify(keycloakService).setUserEnabled(targetUser.getKeycloakUserId(), true);
        verify(userRepository).save(targetUser);
    }

    @Test
    @DisplayName("setActive: Should skip deactivation logic when user is already deactivated")
    void setActive_deactivate_alreadyDeactivatedUser() {

        User targetUser = new User();
        targetUser.setActive(false);
        targetUser.setId(2L);
        targetUser.setKeycloakUserId("keycloak-123");

        when(authService.getCurrentUser()).thenReturn(new User());
        when(userRepository.getUserById(targetUser.getId())).thenReturn(Optional.of(targetUser));

        userService.setActive(targetUser.getId(), "deactivate");

        verify(keycloakService, never()).setUserEnabled(anyString(), anyBoolean());
        verify(userRepository, never()).save(targetUser);
    }

    @Test
    @DisplayName("User creation: user should be created")
    void userCreation() {

        CreateUserDto newUser = new CreateUserDto();
        newUser.setEmail("testmail@gmail.com");
        newUser.setFirstName("Test");
        newUser.setLastName("User");
        newUser.setPhoneNumber("+49123456789");

        when(userRepository.findUserByEmail(newUser.getEmail())).thenReturn(Optional.empty());
        when(keycloakService.createUser(any())).thenReturn("keycloak-123");
        when(userMapper.toUserResponseDto(any(User.class))).thenReturn(new UserResponseDto());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));


        userService.createUser(newUser);

        verify(keycloakService).createUser(any());
        verify(userRepository).save(any());
        verify(publisher).publishEvent(any(KeycloakUserCreatedEvent.class));
        verify(userMapper).toUserResponseDto(any());

    }

    @Test
    @DisplayName("User creation: user should not be created if email already exists")
    void userCreationWithSameEmail() {

        CreateUserDto newUser = new CreateUserDto();
        newUser.setEmail("testmail@gmail.com");

        when(userRepository.findUserByEmail(newUser.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class,
                () -> userService.createUser(newUser));

        verify(keycloakService, never()).createUser(any());
        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    @DisplayName("User update: user should be able to update his details")
    void userUpdateHisOwnDetails() {

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("update@email.com");
        currentUser.setFirstName("Test");

        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.getUserById(currentUser.getId())).thenReturn(Optional.of(currentUser));
        when(permissionService.canUpdateUser(any(User.class), any(User.class)))
                .thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserDto requestDto = UpdateUserDto.builder()
                .email("newEmail@email.com")
                .firstName("newFirstName")
                .build();
        userService.updateUser(currentUser.getId(), requestDto);

        verify(publisher).publishEvent(any(KeycloakUserUpdatedEvent.class));
        verify(keycloakService).updateUser(currentUser.getEmail(), requestDto);
        verify(userMapper).update(requestDto, currentUser);
        verify(userRepository).save(currentUser);


    }

    @Test
    @DisplayName("User update: regular user can't update someone else's details")
    void userCantUpdateDifferentUser() {
        User currentUser = new User();
        currentUser.setId(1L);

        User targetUser = new User();
        targetUser.setId(2L);

        when(authService.getCurrentUser()).thenReturn(currentUser);
        when(userRepository.getUserById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(permissionService.canUpdateUser(currentUser, targetUser))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class, () ->
                userService.updateUser(targetUser.getId(), UpdateUserDto.builder().build()));

        verify(keycloakService, never())
                .updateUser(anyString(), any(UpdateUserDto.class));
        verify(userMapper, never())
                .update(any(UpdateUserDto.class), any(User.class));
        verify(publisher, never()).publishEvent(any(KeycloakUserUpdatedEvent.class));
        verify(userRepository, never()).save(any(User.class));
    }



}