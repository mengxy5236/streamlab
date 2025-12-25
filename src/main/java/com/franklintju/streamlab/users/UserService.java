package com.franklintju.streamlab.users;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;

    @Transactional
    public UserDto registerUser(RegisterUserRequest request) {

        if(userRepository.existsByEmail(request.getEmail())||userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateUserException();
        }

        var user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        var profile = new Profile();
        profile.setUser(user);
        profile.setUsername("用户" + user.getEmail());
        profileRepository.save(profile);

        return userMapper.toDto(user);
    }

    public UserDto getUser(Long id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userMapper.toDto(user);
    }

    public Iterable<UserDto> getAllUsers(String sort) {

        if(!Map.of("phone","email").containsKey(sort)) {
            sort = "email";
        }

        return userRepository.findAll(Sort.by(sort))
                .stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        userRepository.delete(user);
    }

    @Transactional
    public void changePassword(Long id, changePasswordRequest request) {

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);

        if(!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AccessDeniedException("Password is incorrect!!");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

}
