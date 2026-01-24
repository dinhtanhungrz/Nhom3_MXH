package com.be_mxh.dto.image;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ImageUploadResult {
    private String url;
    private String publicId;
}
