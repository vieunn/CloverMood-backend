package com.example.api.profile;

public class ProfileResponse {
    private String email;
    private String fullName;
    private String gender;
    private boolean hasPhoto;

    public ProfileResponse(String email, String fullName, String gender, boolean hasPhoto) {
        this.email = email;
        this.fullName = fullName;
        this.gender = gender;
        this.hasPhoto = hasPhoto;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public boolean isHasPhoto() { return hasPhoto; }
    public void setHasPhoto(boolean hasPhoto) { this.hasPhoto = hasPhoto; }
}
