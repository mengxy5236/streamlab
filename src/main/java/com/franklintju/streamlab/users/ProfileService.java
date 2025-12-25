package com.franklintju.streamlab.users;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ProfileMapper profileMapper;

    @Transactional
    public void update(Long id, updateProfileRequest request) {

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Profile profile = (Profile) profileRepository.findByUser(user);

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

    @Transactional
    public ProfileDto getProfile(Long id) {

        var user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        Profile profile = (Profile) profileRepository.findByUser(user);

        return profileMapper.toDto(profile);
    }
}
