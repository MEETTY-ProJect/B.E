package com.example.meetty.auth.controller;

import com.example.meetty.auth.dto.SignUpDto;
import com.example.meetty.auth.service.UserService;
import com.example.meetty.global.dto.ApiResponse;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.validation.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AuthController {

    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ValidationService validationService;

    @Operation(summary = "회원가입", description = "JSON 데이터(signUpDto)와 이미지 파일(image)을 함께 업로드하는 회원가입 API")
    @PostMapping(value = "/signUp", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> signUp(
            @Parameter(
                    description = "회원가입 정보(JSON)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpDto.class)
                    )
            )
            @RequestPart(value = "signUpDto") String dtoJson,
            @Parameter(
                    description = "회원 프로필 이미지 파일",
                    content = @Content(
                            mediaType = "profileImage/png",
                            schema = @Schema(type = "String", format = "binary")
                    )
            )
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) throws Exception {

        log.info("[POST]: 회원가입 요청");

        SignUpDto signUpDto = objectMapper.readValue(dtoJson, SignUpDto.class);
        validationService.validate(signUpDto);

        userService.signUp(signUpDto, profileImage);

        return ResponseEntity.ok(ApiResponse.success("회원가입이 완료되었습니다."));
    }
}
