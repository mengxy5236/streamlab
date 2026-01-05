package com.franklintju.streamlab.users;

import com.franklintju.streamlab.videos.VideoDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> registerUser(
            @Valid @RequestBody RegisterUserRequest request,
            UriComponentsBuilder uriBuilder) {

        var userDto = userService.registerUser(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userDto.getId()).toUri();
        return ResponseEntity.created(uri).body(userDto);
    }

    @GetMapping("/{id}")
    public UserDto getUser(
            @PathVariable Long id
    ){
        return userService.getUser(id);
    }

    @GetMapping
    public Iterable<UserDto> getAllUsers(
            @RequestParam(required = false,defaultValue = "",name = "sort")String sort
    ){
         return userService.getAllUsers(sort);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(
            @PathVariable Long id
    ){
        userService.deleteUser(id);
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody changePasswordRequest request
    ){
        userService.changePassword(id,request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/videos")
    public ResponseEntity<List<VideoDto>> getVideos(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(userService.getVideos(id));
    }

}
