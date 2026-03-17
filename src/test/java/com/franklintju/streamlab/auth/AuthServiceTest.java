package com.franklintju.streamlab.auth;

import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void shouldReturnTokensWhenLoginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Jwt mockJwt = Mockito.mock(Jwt.class);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn(mockJwt);
        when(jwtService.generateRefreshToken(testUser)).thenReturn(mockJwt);

        LoginResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isNotNull();
        assertThat(response.getRefreshToken()).isNotNull();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void shouldReturnNewAccessTokenWhenRefreshSuccess() {
        String refreshToken = "validRefreshToken";

        Jwt mockJwt = Mockito.mock(Jwt.class);
        when(jwtService.parseToken(refreshToken)).thenReturn(mockJwt);
        when(mockJwt.isExpired()).thenReturn(false);
        when(mockJwt.getUserId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(testUser)).thenReturn(mockJwt);

        Jwt result = authService.refreshAccessToken(refreshToken);

        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnUserFromSecurityContext() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(1L, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = authService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldReturnNullWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        User result = authService.getCurrentUser();

        assertThat(result).isNull();
    }
}
