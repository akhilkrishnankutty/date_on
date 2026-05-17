package com.example.dateon.Service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new IOException("Failed to upload file to Cloudinary", e);
        }
    }

    public void deleteFileFromUrl(String url) {
        if (url == null || url.isEmpty() || !url.contains("cloudinary")) return;
        try {
            int lastSlash = url.lastIndexOf('/');
            int lastDot = url.lastIndexOf('.');
            if (lastSlash != -1 && lastDot != -1 && lastDot > lastSlash) {
                String publicId = url.substring(lastSlash + 1, lastDot);
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (Exception e) {
            System.err.println("Failed to delete file from Cloudinary: " + e.getMessage());
        }
    }
}
