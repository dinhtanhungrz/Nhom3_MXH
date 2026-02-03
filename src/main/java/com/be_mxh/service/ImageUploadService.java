package com.be_mxh.service;

import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.entity.StatusImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageUploadService {
    ImageUploadResult upload(MultipartFile file, String folderFile);

    void delete(String publicId);

    List<StatusImage> getImagesByStatusId(Long statusId);
}
