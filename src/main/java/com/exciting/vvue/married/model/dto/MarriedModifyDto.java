package com.exciting.vvue.married.model.dto;

import java.time.LocalDate;

import com.exciting.vvue.picture.model.Picture;
import com.exciting.vvue.picture.model.dto.PictureDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "부부 정보 수정", description = "부부 정보 수정을 위함")
public class MarriedModifyDto {

	@ApiModelProperty(value="결혼기념일", dataType = "LocalDate", required = true)
	private LocalDate marriedDay;
	@ApiModelProperty(value="사진 id, url", dataType = "PictureDto", required = false)
	private Long pictureId;

}
