package com.CVRgram.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/groups")
public class GroupsController {

    @GetMapping
    public String groupsPage() {
        return "forward:/groups.html";
    }
}
