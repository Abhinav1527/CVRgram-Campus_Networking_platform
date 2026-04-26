package com.CVRgram.Controller;

import com.CVRgram.Model.User;
import com.CVRgram.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "forward:/login.html";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "forward:/register.html";
    }

    @GetMapping("/verify")
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

        if (password == null || !password.matches("^(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>])(?=.*[A-Z]).{6,15}$")) {
            return "Password must be 6-15 characters, with at least 1 number, 1 uppercase, and 1 special char.";
        }

        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if (existingUserByUsername.isPresent() && existingUserByUsername.get().isVerified()) {
            return "Username is already taken.";
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        if (existingUser.isPresent() && existingUser.get().isVerified()) {
            return "Email is already registered.";
        }

        User user = existingUser.orElse(new User());
        user.setUsername(username);
        user.setEmail(email);
        user.setDepartment(department);
        user.setPassword(password);
        user.setVerified(false);

        // generate OTP
        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);
        user.setOtp(otp);

        userRepository.save(user);

        sendOtpEmail(email, otp);

        return "OTP Sent to your email.";
    }

    private void sendOtpEmail(String toEmail, String otp) {

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setTo(toEmail);
                message.setSubject("CVRgram Email Verification");
                message.setText("Your OTP is: " + otp);

                mailSender.send(message);
            } catch (Exception e) {
                System.err.println("Failed to send OTP email asynchronously: " + e.getMessage());
            }
        });
    }

    @PostMapping("/auth/verify")
    @ResponseBody
    public String verifyOtp(@RequestBody Map<String, String> data,
            HttpSession session) {

        String email = data.get("email");
        String enteredOtp = data.get("otp");

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            return "User not found.";
        }

        User user = userOptional.get();

        if (!user.getOtp().equals(enteredOtp)) {
            return "Invalid OTP.";
        }

        user.setVerified(true);
        user.setOtp(null); // Clear OTP after verification
        userRepository.save(user);

        // Set session
        session.setAttribute("loggedInUser", user);

        return "Verification Successful";
    }

    @PostMapping("/auth/login")
    @ResponseBody
    public String loginUser(@RequestBody Map<String, String> data,
            HttpSession session) {

        String email = data.get("email"); // this can now be email or username
        String password = data.get("password");

        Optional<User> userOptional = userRepository.findByEmailOrUsername(email, email);

        if (userOptional.isEmpty()) {
            return "User not registered.";
        }

        User user = userOptional.get();
        
        if (!user.isVerified()) {
            return "Please verify your email first.";
        }

        if (!user.getPassword().equals(password)) {
            return "Invalid password.";
        }

        session.setAttribute("loggedInUser", user);

        return "Login Successful";
    }
}