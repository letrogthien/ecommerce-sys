package com.chuadatten.user.controller;


import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.chuadatten.user.file.FileStorageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-service/files")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    /**
     * Upload file
     */
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("folder") String folder,
            @RequestParam("id") String id
    ) {
        String fileUrl = fileStorageService.storeFile(file, folder, id);
        return ResponseEntity.ok(new UploadResponse(fileUrl));
    }

    /**
     * API GET file
     */
    @GetMapping("/{folder}/{id}/{fileName:.+}")
    
    public ResponseEntity<Resource> getFile(
            @PathVariable String folder,
            @PathVariable String id,
            @PathVariable String fileName
    ) {
        Resource resource = fileStorageService.loadFileAsResource(folder, id, fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    private record UploadResponse(String url) {}
}
