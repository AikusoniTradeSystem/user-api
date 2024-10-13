package io.github.aikusoni.ats.user.api.dao;


import io.github.aikusoni.ats.user.api.model.dto.UserDto;
import io.github.aikusoni.ats.user.api.model.mapper.mapper.UserModelMapper;
import io.github.aikusoni.ats.user.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserDao {
    private final UserRepository userRepository;
    private final UserModelMapper userModelMapper;

    @Transactional(readOnly = true)
    public UserDto getUser(String username) {
        return userModelMapper.toUserDto(userRepository.findByUsername(username));
    }
}
