package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import com.CVRgram.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/follow")
public class FollowController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/{targetUserId}")
    public ResponseEntity<?> toggleFollow(@PathVariable Long targetUserId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).body("Please login first");
        if (currentUser.getId().equals(targetUserId)) return ResponseEntity.badRequest().body("You cannot follow yourself");

        User dbCurrentUser = userRepository.findById(currentUser.getId()).get();
        User targetUser = userRepository.findById(targetUserId).orElseThrow();

        String status;
        if (dbCurrentUser.getFollowingIds().contains(targetUserId)) {
            // Unfollow
            dbCurrentUser.getFollowingIds().remove(targetUserId);
            targetUser.getFollowerIds().remove(dbCurrentUser.getId());
            status = "Follow";
        } else if (dbCurrentUser.getPendingFollowingIds().contains(targetUserId)) {
            // Cancel Request
            dbCurrentUser.getPendingFollowingIds().remove(targetUserId);
            targetUser.getPendingFollowerIds().remove(dbCurrentUser.getId());
            status = "Follow";
        } else {
            // Send Request
            dbCurrentUser.getPendingFollowingIds().add(targetUserId);
            targetUser.getPendingFollowerIds().add(dbCurrentUser.getId());
            status = "Requested";
        }

        userRepository.save(dbCurrentUser);
        userRepository.save(targetUser);
        session.setAttribute("loggedInUser", dbCurrentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("followerCount", targetUser.getFollowerIds().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/accept/{requesterId}")
    public ResponseEntity<?> acceptFollow(@PathVariable Long requesterId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        User dbCurrentUser = userRepository.findById(currentUser.getId()).get();
        User requester = userRepository.findById(requesterId).orElseThrow();

        if (dbCurrentUser.getPendingFollowerIds().contains(requesterId)) {
            dbCurrentUser.getPendingFollowerIds().remove(requesterId);
            dbCurrentUser.getFollowerIds().add(requesterId);
            
            requester.getPendingFollowingIds().remove(dbCurrentUser.getId());
            requester.getFollowingIds().add(dbCurrentUser.getId());

            userRepository.save(dbCurrentUser);
            userRepository.save(requester);
            session.setAttribute("loggedInUser", dbCurrentUser);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/decline/{requesterId}")
    public ResponseEntity<?> declineFollow(@PathVariable Long requesterId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        User dbCurrentUser = userRepository.findById(currentUser.getId()).get();
        User requester = userRepository.findById(requesterId).orElseThrow();

        dbCurrentUser.getPendingFollowerIds().remove(requesterId);
        requester.getPendingFollowingIds().remove(dbCurrentUser.getId());

        userRepository.save(dbCurrentUser);
        userRepository.save(requester);
        session.setAttribute("loggedInUser", dbCurrentUser);
        
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<?> getFollowStatus(@PathVariable Long targetUserId, HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.ok(Map.of("status", "Follow"));

        User dbUser = userRepository.findById(currentUser.getId()).get();
        String status = "Follow";
        
        // Use direct contains check on the IDs
        if (dbUser.getFollowingIds().stream().anyMatch(id -> id.equals(targetUserId))) {
            status = "Following";
        } else if (dbUser.getPendingFollowingIds().stream().anyMatch(id -> id.equals(targetUserId))) {
            status = "Requested";
        }
        
        return ResponseEntity.ok(Map.of("status", status));
    }

    @GetMapping("/pending")
    public ResponseEntity<?> getPendingRequests(HttpSession session) {
        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) return ResponseEntity.status(401).build();

        User dbUser = userRepository.findById(currentUser.getId()).get();
        return ResponseEntity.ok(userRepository.findAllById(dbUser.getPendingFollowerIds()));
    }
}
