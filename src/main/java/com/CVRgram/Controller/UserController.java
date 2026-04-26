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
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setPassword(null); // Don't expose password
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}
