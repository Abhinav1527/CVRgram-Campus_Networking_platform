package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    // 🔹 PAGE ROUTING
    @GetMapping
    public String profilePage() {
        return "forward:/profile.html";
    }

    // 🔹 GET LOGGED-IN USER
    @GetMapping("/data")
    @ResponseBody
    public User getProfile(HttpSession session) {

        return (User) session.getAttribute("loggedInUser");
    }

    // 🔹 UPDATE PROFILE
    @PostMapping("/update")
    @ResponseBody
    public String updateProfile(@RequestBody Map<String, String> data,
                                HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");

        if (user == null) {
            return "Not logged in.";
        }

        user.setUsername(data.get("username"));

        return "Profile Updated";
    }
}