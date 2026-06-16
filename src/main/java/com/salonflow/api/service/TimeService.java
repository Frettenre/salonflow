package com.salonflow.api.service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TimeService {
    public LocalDateTime getBucharestTimeNow() {
        return LocalDateTime.now(ZoneId.of("Europe/Bucharest"));
    }

    public String formatFriendlyDateInBackend(String rawDateStr) {
        try {
            if (rawDateStr == null || !rawDateStr.contains("T")) return rawDateStr;
            String[] parts = rawDateStr.split("T");
            String[] ymd = parts[0].split("-");
            String[] hm = parts[1].split(":");
            return String.format("%s/%s/%s %s:%s", ymd[2], ymd[1], ymd[0], hm[0], hm[1]);
        } catch (Exception e) {
            return rawDateStr;
        }
    }
}