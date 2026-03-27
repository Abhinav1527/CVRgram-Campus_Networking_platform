package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/search")
public class SearchController {

    @GetMapping
    public String searchPage() {
        return "forward:/search.html";
    }

    @ResponseBody
    @GetMapping("/api")
    public String searchApi(@RequestParam String query) {
        return "Search results for: " + query;
    }
}