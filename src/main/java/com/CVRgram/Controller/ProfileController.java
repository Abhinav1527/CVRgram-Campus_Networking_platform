package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import com.CVRgram.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    // 🔹 PAGE ROUTING
    @GetMapping
    public String profilePage() {
        return "forward:/profile.html";
    }

    // 🔹 GET LOGGED-IN USER
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> getProfile(HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return ResponseEntity.status(401).build();
        
        // Fetch fresh from DB to avoid session/serialization issues
        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) return ResponseEntity.status(404).build();
        
        dbUser.setPassword(null); // Safety check

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", dbUser.getId());
        response.put("username", dbUser.getUsername());
        response.put("email", dbUser.getEmail());
        response.put("department", dbUser.getDepartment());
        response.put("headline", dbUser.getHeadline());
        response.put("bio", dbUser.getBio());
        response.put("profilePhotoUrl", dbUser.getProfilePhotoUrl());
        response.put("backgroundPhotoUrl", dbUser.getBackgroundPhotoUrl());
        response.put("followerCount", dbUser.getFollowerIds().size());
        response.put("followingCount", dbUser.getFollowingIds().size());

        return ResponseEntity.ok(response);
    }

    // 🔹 UPDATE PROFILE
    @PostMapping("/update")
    @ResponseBody
    public String updateProfile(@RequestBody Map<String, String> data,
                                HttpSession session) {

        User sessionUser = (User) session.getAttribute("loggedInUser");

        if (sessionUser == null) {
            return "Not logged in.";
        }

        // Fetch fresh entity from DB to ensure it's managed and we don't lose data
        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) {
            return "User not found in database.";
        }

        // Update fields
        dbUser.setUsername(data.get("username"));
        dbUser.setHeadline(data.get("headline"));
        dbUser.setDepartment(data.get("department"));
        dbUser.setBio(data.get("bio"));

        // Save to DB
        userRepository.save(dbUser);

        // Update Session so UI keeps reflecting the new state
        session.setAttribute("loggedInUser", dbUser);

        return "Profile Updated Successfully!";
    }

    // 🔹 UPLOAD PROFILE PHOTO
    @PostMapping("/upload-photo")
    @ResponseBody
    public ResponseEntity<String> uploadProfilePhoto(@RequestParam("file") MultipartFile file,
                                                      HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return ResponseEntity.status(401).body("Not logged in");

        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) return ResponseEntity.status(404).body("User not found");

        String url = saveFile(file);
        if (url == null) return ResponseEntity.status(500).body("Upload failed");

        dbUser.setProfilePhotoUrl(url);
        userRepository.save(dbUser);
        session.setAttribute("loggedInUser", dbUser);

        return ResponseEntity.ok(url);
    }

    // 🔹 UPLOAD BACKGROUND PHOTO
    @PostMapping("/upload-background")
    @ResponseBody
    public ResponseEntity<String> uploadBackgroundPhoto(@RequestParam("file") MultipartFile file,
                                                         HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) return ResponseEntity.status(401).body("Not logged in");

        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) return ResponseEntity.status(404).body("User not found");

        String url = saveFile(file);
        if (url == null) return ResponseEntity.status(500).body("Upload failed");

        dbUser.setBackgroundPhotoUrl(url);
        userRepository.save(dbUser);
        session.setAttribute("loggedInUser", dbUser);

        return ResponseEntity.ok(url);
    }

    private String saveFile(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) dir.mkdirs();

            String ext = "";
            String origName = file.getOriginalFilename();
            if (origName != null && origName.contains(".")) {
                ext = origName.substring(origName.lastIndexOf("."));
            }
            String fileName = UUID.randomUUID() + ext;
            
            // Get absolute path to avoid transferTo issues with relative paths
            File dest = new File(dir.getAbsoluteFile(), fileName);
            file.transferTo(dest);
            
            System.out.println("File saved to: " + dest.getAbsolutePath());
            return "/uploads/" + fileName;
        } catch (IOException e) {
            System.err.println("Upload failed: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}