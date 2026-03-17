package com.franklintju.streamlab.follow;

import com.franklintju.streamlab.exceptions.AlreadyFollowedException;
import com.franklintju.streamlab.exceptions.NotFollowedException;
import com.franklintju.streamlab.follow.mapper.FollowMapper;
import com.franklintju.streamlab.common.RedisLockService;
import com.franklintju.streamlab.users.User;
import com.franklintju.streamlab.users.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository userFollowRepository;
    @Mock
    private FollowMapper followMapper;
    @Mock
    private RedisLockService redisLockService;
    @InjectMocks
    private FollowService followService;

    private User follower;
    private User following;

    @BeforeEach
    void setUp() {
        follower = new User();
        follower.setId(1L);
        follower.setEmail("follower@example.com");

        following = new User();
        following.setId(2L);
        following.setEmail("following@example.com");
    }

    @Test
    void shouldFollowUser() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userFollowRepository.existsById(any(UserFollowId.class))).thenReturn(false);
        when(redisLockService.acquireLock(any(String.class), any(Integer.class))).thenReturn("lock-value");

        followService.follow(request);

        verify(userFollowRepository).save(any(UserFollow.class));
        verify(followMapper).incrementFollowing(1L, 2L);
        verify(followMapper).incrementFollowers(2L, 1L);
    }

    @Test
    void shouldThrowExceptionWhenSelfFollow() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(1L);

        assertThatThrownBy(() -> followService.follow(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("非法 follow 调用！");
    }

    @Test
    void shouldThrowExceptionWhenAlreadyFollowed() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(follower));
        when(userRepository.findById(2L)).thenReturn(Optional.of(following));
        when(userFollowRepository.existsById(any(UserFollowId.class))).thenReturn(true);
        when(redisLockService.acquireLock(any(String.class), any(Integer.class))).thenReturn("lock-value");

        assertThatThrownBy(() -> followService.follow(request))
                .isInstanceOf(AlreadyFollowedException.class);
    }

    @Test
    void shouldUnfollowUser() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        UserFollowId followId = new UserFollowId(1L, 2L);
        UserFollow userFollow = new UserFollow();
        userFollow.setId(followId);
        userFollow.setFollower(follower);
        userFollow.setFollowing(following);

        when(userFollowRepository.findById(followId)).thenReturn(Optional.of(userFollow));
        when(redisLockService.acquireLock(any(String.class), any(Integer.class))).thenReturn("lock-value");

        followService.unfollow(request);

        verify(userFollowRepository).delete(userFollow);
        verify(followMapper).decrementFollowing(1L, 2L);
        verify(followMapper).decrementFollowers(2L, 1L);
    }

    @Test
    void shouldThrowExceptionWhenNotFollowed() {
        FollowRequest request = new FollowRequest();
        request.setFollowerId(1L);
        request.setFollowingId(2L);

        UserFollowId followId = new UserFollowId(1L, 2L);
        when(userFollowRepository.findById(followId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> followService.unfollow(request))
                .isInstanceOf(NotFollowedException.class);
    }

    @Test
    void shouldGetFollowingList() {
        UserSummary summary = new UserSummary();
        summary.setId(2L);
        summary.setUsername("user2");

        when(followMapper.findFollowingByUserId(1L)).thenReturn(List.of(summary));

        List<UserSummary> result = followService.getFollowingList(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(2L);
    }

    @Test
    void shouldGetFollowerList() {
        UserSummary summary = new UserSummary();
        summary.setId(3L);
        summary.setUsername("user3");

        when(followMapper.findFollowersByUserId(1L)).thenReturn(List.of(summary));

        List<UserSummary> result = followService.getFollowerList(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
    }
}
