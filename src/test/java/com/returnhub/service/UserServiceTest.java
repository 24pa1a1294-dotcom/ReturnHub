package com.returnhub.service;

import com.returnhub.dto.UserRegistrationDto;
import com.returnhub.model.Role;
import com.returnhub.model.User;
import com.returnhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationDto registrationDto;

    @BeforeEach
    void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setFullName("John Doe");
        registrationDto.setEmail("john@example.com");
        registrationDto.setPassword("secret123");
        registrationDto.setConfirmPassword("secret123");
        registrationDto.setPhone("+1234567890");
    }

    @Test
    @DisplayName("Should successfully register a new user with hashed password")
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User user = userService.registerUser(registrationDto);

        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getPassword()).isEqualTo("encoded_secret123");
        assertThat(user.getRole()).isEqualTo(Role.ROLE_USER);

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("secret123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void testRegisterUser_DuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when passwords do not match")
    void testRegisterUser_PasswordMismatch() {
        registrationDto.setConfirmPassword("differentPassword");
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);

        assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Passwords do not match");

        verify(userRepository, never()).save(any(User.class));
    }
}
