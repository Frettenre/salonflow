package com.salonflow.api.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {
    public boolean sendOtpEmail(String toEmail, String otp) {
        // Logs simulated email to console so you don't need real mail credentials to log in!
        System.out.println("=================================================");
        System.out.println("[SMTP SIMULATOR] Sent login code to: " + toEmail);
        System.out.println("[SMTP SIMULATOR] Verification code is: " + otp);
        System.out.println("=================================================");
        return false; // Returns false so controller knows to send code directly in response
    }
}