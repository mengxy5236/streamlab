package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.common.RedisLockService;
import com.franklintju.streamlab.exceptions.AlreadyFollowedException;
import com.franklintju.streamlab.exceptions.NotFollowedException;
import com.franklintju.streamlab.follow.mapper.FollowMapper;
import com.franklintju.streamlab.exceptions.UserNotFoundException;
import com.franklintju.streamlab.users.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class FollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final FollowMapper followMapper;
    private final RedisLockService redisLockService;

    private static final String FOLLOW_LOCK_KEY = "user:follow";
    private static final int LOCK_EXPIRE_SECONDS = 10;

    @Transactional
    public void follow(FollowRequest request){

        if(request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("非法 follow 调用！");
        }

        String lockKey = FOLLOW_LOCK_KEY + ":" + request.getFollowingId();
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);

        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            var follower = userRepository
                    .findById(request.getFollowerId())
                    .orElseThrow(UserNotFoundException::new);


            var following = userRepository
                    .findById(request.getFollowingId())
                    .orElseThrow(UserNotFoundException::new);


            UserFollowId followId = new UserFollowId(follower.getId(), following.getId());

            boolean exists = userFollowRepository.existsById(followId);
            if (exists) {
                throw new AlreadyFollowedException();
            }

            UserFollow userFollow = new UserFollow();
            userFollow.setFollower(follower);
            userFollow.setFollowing(following);
            userFollow.setId(followId);

            try {
                userFollowRepository.save(userFollow);
                userFollowRepository.flush();

                followMapper.incrementFollowing(follower.getId(), following.getId());
                followMapper.incrementFollowers(following.getId(), follower.getId());
                log.info("User {} followed user {}", follower.getId(), following.getId());

            } catch (DataIntegrityViolationException e) {

                throw new AlreadyFollowedException();
            }
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    @Transactional
    public void unfollow(FollowRequest request) {

        if(request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("非法 follow 调用！");
        }

        String lockKey = FOLLOW_LOCK_KEY + ":" + request.getFollowingId();
        String lockValue = redisLockService.acquireLock(lockKey, LOCK_EXPIRE_SECONDS);

        if (lockValue == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        try {
            var userFollow = userFollowRepository
                    .findById(new UserFollowId(request.getFollowerId(), request.getFollowingId()))
                    .orElseThrow(NotFollowedException::new);

            userFollowRepository.delete(userFollow);
            userFollowRepository.flush();

            followMapper.decrementFollowing(request.getFollowerId(), request.getFollowingId());
            followMapper.decrementFollowers(request.getFollowingId(), request.getFollowerId());
            log.info("User {} unfollowed user {}", request.getFollowerId(), request.getFollowingId());
        } finally {
            redisLockService.releaseLock(lockKey, lockValue);
        }
    }

    public List<UserSummary> getFollowingList(Long id) {

        return followMapper.findFollowingByUserId(id);
    }

    public List<UserSummary> getFollowerList(Long id) {

        return followMapper.findFollowersByUserId(id);
    }


}
