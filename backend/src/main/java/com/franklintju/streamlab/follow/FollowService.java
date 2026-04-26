package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.common.DistributedLock;
import com.franklintju.streamlab.exceptions.AlreadyFollowedException;
import com.franklintju.streamlab.exceptions.NotFollowedException;
import com.franklintju.streamlab.exceptions.UserNotFoundException;
import com.franklintju.streamlab.follow.mapper.FollowMapper;
import com.franklintju.streamlab.users.ProfileRepository;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final ProfileRepository profileRepository;
    private final FollowMapper followMapper;

    @DistributedLock(key = "user:follow:#request.followingId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void follow(FollowRequest request) {
        if (request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("不能关注自己");
        }

        User follower = userRepository.findById(request.getFollowerId())
                .orElseThrow(UserNotFoundException::new);
        User following = userRepository.findById(request.getFollowingId())
                .orElseThrow(UserNotFoundException::new);

        UserFollowId followId = new UserFollowId(follower.getId(), following.getId());

        if (userFollowRepository.existsById(followId)) {
            throw new AlreadyFollowedException();
        }

        try {
            UserFollow userFollow = new UserFollow();
            userFollow.setFollower(follower);
            userFollow.setFollowing(following);
            userFollow.setId(followId);
            userFollowRepository.save(userFollow);

            profileRepository.incrementFollowingCount(follower.getId(), 1);
            profileRepository.incrementFollowersCount(following.getId(), 1);

            log.info("User {} followed user {}", follower.getId(), following.getId());
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyFollowedException();
        }
    }

    @DistributedLock(key = "user:follow:#request.followingId", expireSeconds = 10, message = "系统繁忙，请稍后重试")
    @Transactional
    public void unfollow(FollowRequest request) {
        if (request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("不能取消关注自己");
        }

        UserFollowId followId = new UserFollowId(request.getFollowerId(), request.getFollowingId());
        UserFollow userFollow = userFollowRepository.findById(followId)
                .orElseThrow(NotFollowedException::new);

        userFollowRepository.delete(userFollow);

        profileRepository.incrementFollowingCount(request.getFollowerId(), -1);
        profileRepository.incrementFollowersCount(request.getFollowingId(), -1);

        log.info("User {} unfollowed user {}", request.getFollowerId(), request.getFollowingId());
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getFollowingList(Long id) {
        return followMapper.findFollowingByUserId(id);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getFollowerList(Long id) {
        return followMapper.findFollowersByUserId(id);
    }
}
