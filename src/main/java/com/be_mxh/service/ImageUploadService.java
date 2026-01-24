package com.be_mxh.service;

import com.be_mxh.dto.image.ImageUploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    ImageUploadResult upload(MultipartFile file, String folderFile);

    void delete(String publicId);
}
