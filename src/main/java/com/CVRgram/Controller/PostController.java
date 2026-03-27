package com.CVRgram.Controller;

import org.springframework.web.bind.annotation.*;
import com.CVRgram.Service.PostService;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public String getPosts() {
        return postService.getPosts();
    }

    @PostMapping
    public String createPost() {
        return postService.createPost();
    }
}