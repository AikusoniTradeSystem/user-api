package io.github.aikusoni.ats.user.api.service;

import io.github.aikusoni.ats.user.api.model.dto.UserDto;
import io.github.aikusoni.ats.user.api.model.mapper.mapper.UserModelMapper;
import io.github.aikusoni.ats.user.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
     private final UserRepository userRepository;
     private final UserModelMapper userMapper;

     public UserDto getUser(String username) {
         return userMapper.toUserDto(userRepository.findByUsername(username));
     }
}
