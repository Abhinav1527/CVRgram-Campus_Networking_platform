package com.CVRgram.Service;

import org.springframework.stereotype.Service;

@Service
public class HomeService {

    public String getHome() {
        return "Welcome to CVRgram Home Feed!";
    }
}