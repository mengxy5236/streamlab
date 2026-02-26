package com.franklintju.streamlab.users;

import com.franklintju.streamlab.auth.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;
    private final AuthService authService;

    @Transactional
    public void update(Long id, updateProfileRequest request) {

        Profile profile = profileRepository.findByUserId(id).orElseThrow(UserNotFoundException::new);

        if (request.getUsername() != null) {
            profile.setUsername(request.getUsername());
        }

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getGender() != null) {
            profile.setGender(request.getGender());
        }

        if (request.getBirthday() != null) {
            profile.setBirthday(request.getBirthday());
        }

        profileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public ProfileDto getProfile(Long id) {

        User currentUser = authService.getCurrentUser();

        boolean isSelf = currentUser != null && currentUser.getId().equals(id);
        boolean isAuthenticated = currentUser != null;


        if (isSelf) {
            Profile profile = profileRepository.findByUserId(id)
                    .orElseThrow(UserNotFoundException::new);
            return profileMapper.toFullDto(profile);
        }

        else if (isAuthenticated) {
            Profile profile = profileRepository.findByUserId(id)
                    .orElseThrow(UserNotFoundException::new);
            return profileMapper.toPublicDto(profile);
        }

        else {
            Profile profile = profileRepository.findByUserId(id)
                    .orElseThrow(UserNotFoundException::new);
            return profileMapper.toGuestDto(profile);
        }
    }
}
