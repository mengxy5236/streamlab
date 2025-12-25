package com.franklintju.streamlab.users;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> updateProfile(
            @PathVariable Long id,
            @RequestBody updateProfileRequest request
    ){
        profileService.update(id,request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfileDto> getProfile(
            @PathVariable Long id
    ){
        var dto = profileService.getProfile(id);
        return ResponseEntity.ok().body(dto);
    }
}
