package com.eod.eod.domain.image.presentation.dto.res;

import lombok.Data;

@Data
public class ImageUploadResponse {
    
    private String url;
    
    public static ImageUploadResponse create(String url) {
        ImageUploadResponse response = new ImageUploadResponse();
        response.url = url;
        return response;
    }
}
