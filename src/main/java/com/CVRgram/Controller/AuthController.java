package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Controller
public class AuthController {

    private Map<String, User> pendingUsers = new HashMap<>();
    private Map<String, User> registeredUsers = new HashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/auth/login";
    }

    @GetMapping("/auth/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/auth/verify")
    public String verifyPage() {
        return "forward:/verify.html";
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public String registerUser(@RequestBody Map<String, String> userData) {

        String username = userData.get("username");
        String email = userData.get("email");
        String department = userData.get("department");
        String password = userData.get("password");

        if (email == null || !email.toLowerCase().endsWith("@cvr.ac.in")) {
            return "Only @cvr.ac.in emails are allowed.";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDepartment(department);
        user.setPassword(password);
        user.setVerified(false);

        // generate OTP
        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setOtp(otp);

        pendingUsers.put(email, user);

        sendOtpEmail(email, otp);

        return "OTP Sent to your email.";
    }

    private void sendOtpEmail(String toEmail, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("CVRgram Email Verification");
        message.setText("Your OTP is: " + otp);

        mailSender.send(message);
    }

    @PostMapping("/auth/verify")
    @ResponseBody
    public String verifyOtp(@RequestBody Map<String, String> data,
                            HttpSession session) {

        String email = data.get("email");
        String enteredOtp = data.get("otp");

        User user = pendingUsers.get(email);

        if (user == null) {
            return "User not found.";
        }

        if (!user.getOtp().equals(enteredOtp)) {
            return "Invalid OTP.";
        }

        user.setVerified(true);

        // Move to registered users
        registeredUsers.put(email, user);
        pendingUsers.remove(email);

        // Set session
        session.setAttribute("loggedInUser", user);

        return "Verification Successful";
    }
    @PostMapping("/auth/login")
    @ResponseBody
    public String loginUser(@RequestBody Map<String, String> data,
                            HttpSession session) {

        String email = data.get("email");
        String password = data.get("password");

        User user = registeredUsers.get(email);

        if (user == null) {
            return "User not registered.";
        }

        if (!user.getPassword().equals(password)) {
            return "Invalid password.";
        }

        session.setAttribute("loggedInUser", user);

        return "Login Successful";
    }
}