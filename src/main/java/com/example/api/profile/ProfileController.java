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

    @PostMapping("/photo")
    public Map<String, Object> uploadProfilePhoto(
            @RequestParam String email,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestBody(required = false) Map<String, String> jsonBody) {
        
        Map<String, Object> res = new java.util.HashMap<>();
        
        try {
            byte[] fileBytes = null;
            String actualFileName = "photo.png";
            
            if (file != null && !file.isEmpty()) {
                // Multipart file upload
                fileBytes = file.getBytes();
                actualFileName = file.getOriginalFilename();
            } else if (jsonBody != null && jsonBody.containsKey("base64")) {
                // Base64 string from JSON body
                String base64Data = jsonBody.get("base64");
                actualFileName = jsonBody.getOrDefault("fileName", "photo.png");
                fileBytes = java.util.Base64.getDecoder().decode(base64Data);
            } else {
                res.put("success", false);
                res.put("message", "File is required - send JSON with 'base64' and optional 'fileName'");
                return res;
            }

            return profileService.uploadProfilePhoto(email, fileBytes, actualFileName);

        } catch (IOException ex) {
            res.put("success", false);
            res.put("message", "File read error");
            res.put("error", ex.getMessage());
            return res;
        } catch (IllegalArgumentException ex) {
            res.put("success", false);
            res.put("message", "Invalid base64 encoding");
            res.put("error", ex.getMessage());
            return res;
        }
    }
}
