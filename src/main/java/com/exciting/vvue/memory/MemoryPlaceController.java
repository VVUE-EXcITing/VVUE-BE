package com.exciting.vvue.memory;

import com.exciting.vvue.auth.service.AuthService;
import com.exciting.vvue.married.model.Married;
import com.exciting.vvue.married.service.MarriedService;
import com.exciting.vvue.memory.model.PlaceMemory;
import com.exciting.vvue.memory.model.dto.MemoryPlaceFindDto;
import com.exciting.vvue.memory.service.MemoryPlaceService;
import com.exciting.vvue.user.model.User;
import com.exciting.vvue.user.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/memory-place")
@RequiredArgsConstructor
public class MemoryPlaceController {
    private final MemoryPlaceService memoryPlaceService;

    private final AuthService authService;
    @Deprecated
    @ApiOperation(value="[TODO] 추억 지도 조회")
    @GetMapping
    public ResponseEntity<?> searchMemoryPlace(@RequestHeader("Authorization") String token, @RequestParam(required = false) MemoryPlaceFindDto findCondition) {
        log.debug("[GET] /memory-place");
        Long userId = authService.getUserIdFromToken(token);
        List<PlaceMemory> placeMemories = memoryPlaceService.getRecentMemoryPlaceByMarriedId(userId, findCondition);
        return ResponseEntity.ok().body(placeMemories);
    }

    @Deprecated
    @ApiOperation(value="[TODO] (특정 장소의) 모든 추억 조회")
    @GetMapping("/{placeId}")
    public ResponseEntity<?> searchMemoryPlace(@RequestHeader("Authorization")String token, @PathVariable Long placeId) {
        log.debug("[GET] /memory-place/" + placeId);
        //TODO: 모든 추억 조회
        return ResponseEntity.ok().build();
    }
}
