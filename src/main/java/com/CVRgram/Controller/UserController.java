package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import com.CVRgram.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(null); // Don't expose password

            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            response.put("department", user.getDepartment());
            response.put("headline", user.getHeadline());
            response.put("bio", user.getBio());
            response.put("profilePhotoUrl", user.getProfilePhotoUrl());
            response.put("backgroundPhotoUrl", user.getBackgroundPhotoUrl());
            response.put("followerCount", user.getFollowerIds().size());
            response.put("followingCount", user.getFollowingIds().size());

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}
