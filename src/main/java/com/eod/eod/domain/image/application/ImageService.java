package com.eod.eod.domain.image.application;

import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    // 이미지 파일 업로드
    // @param file 업로드할 파일
    // @return 업로드된 파일의 URL
    String uploadImage(MultipartFile file, Long userId);
}
