package com.CVRgram.Controller;

import com.CVRgram.Model.Post;
import com.CVRgram.Model.User;
import com.CVRgram.Repository.CommentRepository;
import com.CVRgram.Repository.MessageRepository;
import com.CVRgram.Repository.PostRepository;
import com.CVRgram.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
public class SettingsController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private MessageRepository messageRepository;

    @GetMapping("/settings")
    public String showSettings(HttpSession session) {
        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }
        return "forward:/settings.html";
    }

    @PostMapping("/profile/delete-account")
    @ResponseBody
    @Transactional
    public ResponseEntity<String> deleteAccount(@RequestBody Map<String, String> data, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return ResponseEntity.status(401).body("Not logged in.");
        }

        User dbUser = userRepository.findById(sessionUser.getId()).orElse(null);
        if (dbUser == null) {
            return ResponseEntity.status(404).body("User not found.");
        }

        String currentPassword = data.get("currentPassword");
        if (currentPassword == null || !currentPassword.equals(dbUser.getPassword())) {
            return ResponseEntity.status(400).body("Current password is incorrect.");
        }

        Long userId = dbUser.getId();

        List<Post> allPosts = postRepository.findAll();
        for (Post post : allPosts) {
            boolean changed = post.getLikedUserIds().remove(userId);
            changed = post.getSavedUserIds().remove(userId) || changed;
            if (changed) {
                postRepository.save(post);
            }
        }

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            boolean changed = user.getFollowerIds().remove(userId);
            changed = user.getFollowingIds().remove(userId) || changed;
            changed = user.getPendingFollowerIds().remove(userId) || changed;
            changed = user.getPendingFollowingIds().remove(userId) || changed;
            if (changed) {
                userRepository.save(user);
            }
        }

        messageRepository.deleteBySenderIdOrRecipientId(userId, userId);
        commentRepository.deleteByAuthorId(userId);
        postRepository.deleteAll(postRepository.findByAuthorIdOrderByCreatedAtDesc(userId));
        userRepository.delete(dbUser);

        session.invalidate();
        return ResponseEntity.ok("Account deleted successfully.");
    }
}
