package com.exciting.vvue.married;

import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exciting.vvue.auth.service.AuthService;
import com.exciting.vvue.married.exception.AlreadyMarriedException;
import com.exciting.vvue.married.exception.MarriedCodeNotGeneratedException;
import com.exciting.vvue.married.exception.MarriedWithSameIdException;
import com.exciting.vvue.married.model.dto.MarriedCode;
import com.exciting.vvue.married.model.dto.req.MarriedCreateDto;
import com.exciting.vvue.married.service.MarriedCodeService;
import com.exciting.vvue.married.service.MarriedService;
import com.exciting.vvue.user.service.UserService;

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/married-code")
@RequiredArgsConstructor
public class MarriedCodeController {
	private final MarriedCodeService marriedCodeService;
	private final MarriedService marriedService;
	private final AuthService authService;
	private final UserService userService;
	private final int REGENERATE_COUNT = 10;
	private final int CODE_LENGTH = 8;
	// 부부 인증 코드 발급
	@GetMapping("/generate")
	@ApiOperation(value="부부 인증 코드 발급 (자신 id로)",notes = "부부 인증 코드를 발급한다")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = MarriedCode.class),
		@ApiResponse(code = 404, message = "인증코드 생성실패"),
	})
	public ResponseEntity<?> getMarriedAuthCode(@RequestHeader("Authorization") String token){
		/**
		 * todo
		 * 인증 코드 생성
		 * token으로 id 가져오기
		 * 
		 */
		Long id = authService.getUserIdFromToken(token);
		log.debug("[GET] /married-code/generate : id " + id);

		String code = marriedCodeService.generateCode(CODE_LENGTH, REGENERATE_COUNT);
		log.debug("[GET] /married-code/generate : marriedCode " + code);
		if(code == null)
			throw new MarriedCodeNotGeneratedException("인증 코드 생성에 실패했어요.");

		marriedCodeService.addMarriedCodeInRedis(id, code);

		MarriedCode marriedCodeDto = new MarriedCode(code);

		return new ResponseEntity<>(marriedCodeDto, HttpStatus.OK);
	}

	@GetMapping("/regenerate")
	@ApiOperation(value="부부 인증 코드 재발급", notes="인증 코드 시간 만료 시 자동 호출 + 버튼 눌렀을 때 호출")
	@ApiImplicitParam(name="marriedCode",value = "전에 발급된 부부인증코드", required = true)
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = MarriedCode.class),
		@ApiResponse(code = 404, message = "인증코드 생성실패"),
	})
	public ResponseEntity<?> regenerateAuthCode(@RequestHeader("Authorization") String token, @RequestParam String marriedCode){
		/**
		 * todo
		 * 인증코드 재발급
		 * 인증코드 중복처리 - redis
		 * 인증방식 논의 필요
		 */
		Long id = authService.getUserIdFromToken(token);
		log.debug("[GET] /married-code/regenerate : id " + id);
		String code = marriedCodeService.generateCode(CODE_LENGTH, REGENERATE_COUNT);
		log.debug("[GET] /married-code/regenerate : regenerated marreidCode " + code);
		if(code == null)
			throw new MarriedCodeNotGeneratedException("인증 코드 생성에 실패했어요.");

		if(marriedCodeService.isCodeInRedis(marriedCode))
			marriedCodeService.deleteMarriedCodeInRedis(marriedCode);

		marriedCodeService.addMarriedCodeInRedis(id, code);
		return new ResponseEntity<>(new MarriedCode(code), HttpStatus.OK);
	}

	@PostMapping("/connect")
	@ApiOperation(value = "인증 코드 일치 확인", notes = "인증 코드 발급한 유저와 부부 정보 생성")
	// @ApiImplicitParam(name = "marriedCode", value = "입력한 인증코드", required = true)
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공", response = Long.class ),
		@ApiResponse(code = 400, message = "자신 인증코드 입력, 이미 부부 정보가 있는 사람과 연동 시도"),
		@ApiResponse(code = 404, message = "redis에 인증 코드 없음"),
	})
	public ResponseEntity<?> connectMarriedCode(@RequestHeader("Authorization") String token, @RequestBody MarriedCode marriedCode){

		Long id = authService.getUserIdFromToken(token);
		log.debug("[POST] /married-code/connect : id " + id);
		log.debug("[POST] /married-code/connect : marriedCode " + marriedCode);
		if(!marriedCodeService.isCodeInRedis(marriedCode.getMarriedCode()))
			throw new MarriedCodeNotGeneratedException("인증 코드가 존재하지 않아요.");

		Long targetId = marriedCodeService.getIdFromMarriedCode(marriedCode.getMarriedCode());
		log.debug("[POST] /married-code/connect : target id " + targetId);

		if(targetId == id)
			throw new MarriedWithSameIdException("혼자선 부부가 될 수 없어요!");

		if(marriedService.countByUserId(targetId) > 0 && marriedService.countByUserId(id) > 0)
			throw new AlreadyMarriedException("상대방은 이미 가입중이에요.");

		// 코드 있다면 redis에서 지우기
		if(marriedCodeService.isCodeInRedis(marriedCode.getMarriedCode()))
			marriedCodeService.deleteMarriedCodeInRedis(marriedCode.getMarriedCode());

		// 부부 정보 연결
		marriedService.createMarried(id, MarriedCreateDto.builder()
				.partnerId(targetId)
				.build());

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
