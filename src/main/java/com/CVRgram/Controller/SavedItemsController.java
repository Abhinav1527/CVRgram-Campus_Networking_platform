package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/saved")
public class SavedItemsController {

    @GetMapping
    public String savedPage() {
        return "forward:/saved.html";
    }
}
