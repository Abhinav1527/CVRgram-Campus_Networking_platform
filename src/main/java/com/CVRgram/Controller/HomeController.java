package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.CVRgram.Service.HomeService;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    // 🔹 PAGE ROUTING
    @GetMapping
    public String homePage() {
        return "forward:/home.html";
    }

    // 🔹 API ENDPOINT
    @ResponseBody
    @GetMapping("/api")
    public String homeApi() {
        return homeService.getHome();
    }
}