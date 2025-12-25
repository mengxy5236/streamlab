package com.franklintju.streamlab.follow;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/users")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<?> follow(
            @RequestBody FollowRequest request
    ){
        followService.follow(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/unfollow")
    public ResponseEntity<?> unfollow(
            @RequestBody FollowRequest request
    ){
        followService.unfollow(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/following")
    public ResponseEntity<?> getFollowingList(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(followService.getFollowingList(id));
    }

    @GetMapping("/{id}/follower")
    public ResponseEntity<?> getFollowerList(
            @PathVariable Long id
    ){
        return ResponseEntity.ok(followService.getFollowerList(id));
    }
}
