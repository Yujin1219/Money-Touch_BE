package com.server.money_touch.global.s3;

import com.server.money_touch.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test/s3")
public class S3testController {

    // 이미지를 업로드하는 각 서비스에 추가해주세요.
    private final S3Manager s3Manager;

    @Operation(summary = "이미지 업로드 테스트 API", description = "Multipart 파일 업로드")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    /*consumes ~, @RequestParam ~ , String url = ~ 작성해야 업로드 가능. dir 은 저장 폴더명입니다.
    프로필 이미지라면 dirName: "profile", 소비 루틴 기록 이미지라면 "record" 라고 작성하는 것을 추천드립니다.*/

    public ApiResponse<String> uploadTest(@RequestParam("file") MultipartFile file) {
        try {
            String url = s3Manager.upload(file, "test");
            return ApiResponse.onSuccess(url);
        } catch (Exception e) {
            return ApiResponse.onFailure("S3_UPLOAD_FAIL", e.getMessage(), null);
        }
    }

}
