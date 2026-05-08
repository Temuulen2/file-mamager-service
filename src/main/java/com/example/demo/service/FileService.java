package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }
        String filename = UUID.randomUUID().toString() + ext;

        Path dirPath = Paths.get(uploadDir);
        if (!Files.exists(dirPath)) {
            Files.createDirectories(dirPath);
        }

        Path filePath = dirPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filename;
    }

    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename).normalize();
    }

    public List<String> listFiles() {
        List<String> names = new ArrayList<>();
        try {
            Path dirPath = Paths.get(uploadDir);
            if (!Files.exists(dirPath)) return names;
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
                for (Path entry : stream) {
                    names.add(entry.getFileName().toString());
                }
            }
        } catch (IOException e) {
            // return empty list
        }
        return names;
    }

    public boolean deleteFile(String filename) throws IOException {
        Path filePath = getFilePath(filename);
        // Prevent path traversal
        if (!filePath.startsWith(Paths.get(uploadDir).toAbsolutePath())) {
            return false;
        }
        return Files.deleteIfExists(filePath);
    }
}
