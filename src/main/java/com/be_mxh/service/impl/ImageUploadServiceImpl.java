package com.be_mxh.service.impl;

import com.be_mxh.dto.image.ImageUploadResult;
import com.be_mxh.service.ImageUploadService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageUploadServiceImpl implements ImageUploadService {
    @Autowired
    private Cloudinary cloudinary;

    @Override
    public ImageUploadResult upload(MultipartFile file, String folderFile) {
        validate(file);

        try {
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder",folderFile,
                            "resource_type", "image"
                    )
            );

            return new ImageUploadResult(
                    result.get("secure_url").toString(),
                    result.get("public_id").toString()
            );

        } catch (IOException e) {
            throw new RuntimeException("Upload to Cloudinary failed", e);
        }
    }

    @Override
    public void delete(String imageUrl) {
        String publicId = extractPublicId(imageUrl);
        try {
            cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.emptyMap()
            );
        } catch (Exception e) {
            throw new RuntimeException("Delete image failed", e);
        }
    }

    private void validate(MultipartFile file) {
        if (file.isEmpty())
            throw new RuntimeException("File is empty");

        if (!file.getContentType().startsWith("image/"))
            throw new RuntimeException("File must be image");

        if (file.getSize() > 5 * 1024 * 1024)
            throw new RuntimeException("Max image size is 5MB");
    }

    private static String extractPublicId(String imageUrl) {
        if (imageUrl == null) return null;

        String noQuery = imageUrl.split("\\?")[0];

        int uploadIndex = noQuery.indexOf("/upload/");
        if (uploadIndex == -1) return null;

        String publicPath = noQuery.substring(uploadIndex + 8); // sau /upload/

        // bỏ version
        if (publicPath.startsWith("v")) {
            publicPath = publicPath.substring(publicPath.indexOf("/") + 1);
        }

        // bỏ extension
        return publicPath.substring(0, publicPath.lastIndexOf("."));
    }
}
