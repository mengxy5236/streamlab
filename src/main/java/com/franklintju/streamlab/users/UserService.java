package com.franklintju.streamlab.users;

import com.franklintju.streamlab.auth.AuthService;
import com.franklintju.streamlab.users.mapper.UserMapper;
import com.franklintju.streamlab.videos.VideoDto;
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
    private final UserConverter userConverter;
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final UserMapper userMapper;
    private final AuthService authService;

    @Transactional
    public UserDto registerUser(RegisterUserRequest request) {

        if(userRepository.existsByEmail(request.getEmail())||userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateUserException();
        }

        var user = userConverter.toEntity(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        var profile = new Profile();
        profile.setUser(user);
        profile.setUsername("用户" + user.getEmail());
        profileRepository.save(profile);

        return userConverter.toDto(user);
    }

    public UserDto getUser(Long id) {
        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return userConverter.toDto(user);
    }

    public Iterable<UserDto> getAllUsers(String sort) {

        if(!Map.of("phone","email").containsKey(sort)) {
            sort = "email";
        }

        return userRepository.findAll(Sort.by(sort))
                .stream()
                .map(userConverter::toDto)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        var currentUser = authService.getCurrentUser();
        if (!id.equals(currentUser.getId())) {
            throw new AccessDeniedException("无权删除此用户");
        }
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

    public List<VideoDto> getVideos(Long id) {

        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException();
        }

        return userMapper.getVideosByUserId(id);
    }
}
