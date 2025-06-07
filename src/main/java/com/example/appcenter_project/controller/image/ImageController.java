package com.example.appcenter_project.controller.image;

import com.example.appcenter_project.service.image.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images")
public class ImageController implements ImageApiSpecification {

    private final ImageService imageService;

    @PostMapping(value = "/default/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> setDefaultUserImage(@RequestPart MultipartFile image) {
        imageService.setDefaultUserImage(image);
        return ResponseEntity.status(CREATED).build();
    }
}
