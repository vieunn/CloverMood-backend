package com.example.api.profile;

import java.io.IOException;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public Map<String, Object> getProfile(@RequestParam String email) {
        return profileService.getProfile(email);
    }

    @PutMapping
    public Map<String, Object> updateProfile(@RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(request);
    }

    @PutMapping("/password")
    public Map<String, Object> changePassword(@RequestBody ChangePasswordRequest request) {
        return profileService.changePassword(request);
    }

    @GetMapping("/photo")
    public Map<String, Object> getProfilePhoto(@RequestParam String email) {
        return profileService.getProfilePhoto(email);
    }

    @PostMapping("/photo")
    public Map<String, Object> uploadProfilePhoto(
            @RequestParam String email,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        
        Map<String, Object> res = new java.util.HashMap<>();
        
        try {
            if (file == null || file.isEmpty()) {
                res.put("success", false);
                res.put("message", "File is required - send as multipart FormData with 'file' parameter");
                return res;
            }

            byte[] fileBytes = file.getBytes();
            String actualFileName = file.getOriginalFilename();

            return profileService.uploadProfilePhoto(email, fileBytes, actualFileName);

        } catch (IOException ex) {
            res.put("success", false);
            res.put("message", "File read error");
            res.put("error", ex.getMessage());
            return res;
        }
    }
}
