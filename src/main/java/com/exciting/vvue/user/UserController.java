package com.exciting.vvue.user;


import com.exciting.vvue.auth.service.AuthService;
import com.exciting.vvue.married.model.Married;
import com.exciting.vvue.married.service.MarriedService;
import com.exciting.vvue.user.model.User;
import com.exciting.vvue.user.model.dto.UserAuthenticated;
import com.exciting.vvue.user.model.dto.UserDto;
import com.exciting.vvue.user.model.dto.UserInfoUpdated;
import com.exciting.vvue.user.model.dto.UserModifyDto;
import com.exciting.vvue.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;
    private final MarriedService marriedService;

    @Operation(summary ="모든 정보(부부연결유무/유저정보-성별,생일,닉네임) 입력 여부 확인")
    @GetMapping("/all-info-updated")
    public ResponseEntity<?> isAllAuthenticated(@RequestHeader("Authorization") String token) {
        Long id = authService.getUserIdFromToken(token);
        User user = userService.getUserById(id);
        Married married = marriedService.getMarriedByUserId(user.getId());

        UserInfoUpdated userAuthenticated = new UserInfoUpdated(
            user.isAuthenticated(), married != null,
            married != null ? married.getMarriedDay() != null : false);
        return ResponseEntity.status(HttpStatus.OK).body(userAuthenticated);
    }

    @Operation(summary ="추가 정보(성별,생일,닉네임) 입력 여부 확인")
    @GetMapping("/user-info-updated")
    public ResponseEntity<?> isAuthenticated(@RequestHeader("Authorization") String token) {
        Long id = authService.getUserIdFromToken(token);
        User user = userService.getUserById(id);
        UserAuthenticated userAuthenticated = new UserAuthenticated(user.isAuthenticated());
        return ResponseEntity.status(HttpStatus.OK).body(userAuthenticated);
    }

    @Operation(summary ="유저 정보 조회")
    @GetMapping
    public ResponseEntity<UserDto> getUserInfoByToken(
        @RequestHeader("Authorization") String token) {
        log.debug("유저 정보 조회"+token);
        Long id = authService.getUserIdFromToken(token);
        UserDto userResDto = userService.getUserDto(id);
        return new ResponseEntity<>(userResDto, HttpStatus.OK);
    }
    @Operation(summary ="유저 정보(성별,생일,닉네임,프로필사진ID) 수정")
    @PutMapping
    public ResponseEntity<?> modify(@RequestHeader("Authorization") String token,
        @RequestBody UserModifyDto userModifyDto) {
        log.debug("[PUT] /users : modifyInfo " + userModifyDto);
        Long userId = authService.getUserIdFromToken(token);
        userService.modifyUser(userId, userModifyDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @Operation(summary ="[TODO] 유저 삭제")
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token) {
        Long id = authService.getUserIdFromToken(token);
        userService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
