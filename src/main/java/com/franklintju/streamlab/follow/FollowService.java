package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.follow.mapper.FollowMapper;
import com.franklintju.streamlab.users.Profile;
import com.franklintju.streamlab.users.ProfileRepository;
import com.franklintju.streamlab.users.UserNotFoundException;
import com.franklintju.streamlab.users.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@AllArgsConstructor
@Service
public class FollowService {

    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;
    private final FollowMapper followMapper;
    private final ProfileRepository profileRepository;

    @Transactional
    public void follow(FollowRequest request){

        if(request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("非法 follow 调用！");
        }

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

        } catch (DataIntegrityViolationException e) {
            
            throw new AlreadyFollowedException();
        }
    }

    @Transactional
    public void unfollow(FollowRequest request) {

        if(request.getFollowerId().equals(request.getFollowingId())) {
            throw new IllegalStateException("非法 follow 调用！");
        }

        var userFollow = userFollowRepository
                .findById(new UserFollowId(request.getFollowerId(), request.getFollowingId()))
                .orElseThrow(NotFollowedException::new);

        userFollowRepository.delete(userFollow);
        userFollowRepository.flush();

        followMapper.decrementFollowing(request.getFollowerId(), request.getFollowingId());
        followMapper.decrementFollowers(request.getFollowingId(), request.getFollowerId());

    }

    public List<UserSummary> getFollowingList(Long id) {

        return followMapper.findFollowingByUserId(id);
    }

    public List<UserSummary> getFollowerList(Long id) {

        return followMapper.findFollowersByUserId(id);
    }


}
