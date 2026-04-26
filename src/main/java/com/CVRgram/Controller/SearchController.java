package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.CVRgram.Model.User;
import com.CVRgram.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import java.util.List;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String searchPage() {
        return "forward:/search.html";
    }

    @ResponseBody
    @GetMapping("/api")
    public ResponseEntity<List<User>> searchApi(@RequestParam String query) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query);
        for (User user : users) {
            user.setPassword(null);
        }
        return ResponseEntity.ok(users);
    }
}