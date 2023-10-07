package com.exciting.vvue.place;

import com.exciting.vvue.auth.service.AuthService;
import com.exciting.vvue.place.model.dto.PlaceReqDto;
import com.exciting.vvue.place.service.FavoritePlaceService;
import com.exciting.vvue.place.service.PlaceService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/places-favorite")
@RequiredArgsConstructor
public class FavoritePlaceController {

    private final FavoritePlaceService favoritePlaceService;
    private final PlaceService placeService;
    private final AuthService authService;

    @PostMapping
    @ApiOperation(value = "즐겨찾기 상태 변경", notes = "즐겨찾기의 상태를 변경한다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "요청 성공")
            , @ApiResponse(code = 202, message = "요청 성공, place 등록 필요")
            , @ApiResponse(code = 400, message = "잘못된 필드, 값 요청")
            , @ApiResponse(code = 401, message = "로그인되지 않은 사용자")
            , @ApiResponse(code = 500, message = "DB 서버 에러")
    })

    public ResponseEntity<?> changeScrap(@RequestHeader("Authorization") String token, @RequestParam Long mapPlaceId){
        // 유저 인증 처리
        Long userId = authService.getUserIdFromToken(token);
        // mapPlaceId로 Place가 DB에 있는지 체크
        Long placeId = placeService.checkPlace(mapPlaceId);

        // scrap 되어 있는지 확인
        boolean state;
        if(favoritePlaceService.checkScrap(userId, placeId)) {
            favoritePlaceService.deleteScrap(userId, placeId);
            state = false;
        }
        else{
            favoritePlaceService.addScrap(userId, placeId);
            state = true;
        }
        return ResponseEntity.ok(state);
    }

    @PostMapping("/regist")
    @ApiOperation(value = "장소 등록 및 즐겨찾기 하기", notes = "장소를 등록하고 즐겨찾기한 상태로 변경.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "요청 성공")
            , @ApiResponse(code = 400, message = "잘못된 필드, 값 요청")
            , @ApiResponse(code = 401, message = "로그인되지 않은 사용자")
            , @ApiResponse(code = 500, message = "DB 서버 에러")
    })
    public ResponseEntity<?> scrapAndAdd(@RequestHeader("Authorization") String token, @RequestBody PlaceReqDto placeReqDto){
        Long userId = authService.getUserIdFromToken(token);
        long placeId = placeService.addPlace(placeReqDto);
        favoritePlaceService.addScrap(userId, placeId);
        return ResponseEntity.ok("장소 등록 및 즐겨찾기 성공");
    }

}
