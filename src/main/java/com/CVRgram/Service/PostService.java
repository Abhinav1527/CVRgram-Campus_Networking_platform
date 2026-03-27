package com.CVRgram.Service;

import org.springframework.stereotype.Service;

@Service
public class PostService {

    public String getPosts() {
        return "All Posts Displayed!";
    }

    public String createPost() {
        return "Post Created Successfully!";
    }
}