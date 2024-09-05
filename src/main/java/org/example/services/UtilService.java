package org.example.services;

import io.javalin.http.UploadedFile;

import java.io.IOException;
import java.util.Base64;

public class UtilService {
    private String encodeToBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }
    public byte[] readUploadedFile(UploadedFile uploadedFile) {
        try {
            return uploadedFile.content().readAllBytes();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read file", e);
        }
    }
}
