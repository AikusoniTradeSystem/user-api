package io.github.aikusoni.ats.user.api.controller;

import io.github.aikusoni.ats.spring.mvcstandard.model.view.ATSResponseBody;
import io.github.aikusoni.ats.user.api.model.dto.UserDto;
import io.github.aikusoni.ats.user.api.model.mapper.mapper.UserModelMapper;
import io.github.aikusoni.ats.user.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {
     private final UserService userService;
     private final UserModelMapper userModelMapper;

     @GetMapping("/username/{username}")
     public ResponseEntity<ATSResponseBody<UserDto>> getUser(
             @PathVariable String username
     ) {
         UserDto userDto = userService.getUser(username);
         return ATSResponseBody.<UserDto>ok(userDto)
                 .toResponseEntity();
     }
}
