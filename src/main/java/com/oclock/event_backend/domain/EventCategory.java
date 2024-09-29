package com.oclock.event_backend.domain;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum EventCategory {

    SOCIAL_ACTIVITIES("Social Activities"),
    ART_CULTURE("Art & Culture"),
    COMMUNITY_ENVIRONMENT("Community & Environment"),
    BUSINESS_CAREER("Business & Career"),
    LANGUAGE("Language"),
    GAMES("Games"),
    POLITICAL_ORGANIZATIONS("Political Organizations"),
    MUSIC("Music"),
    RELIGION_SPIRITUALITY("Religion & Spirituality"),
    HEALTH_WELLNESS("Health & Wellness"),
    SCIENCE_EDUCATION("Science & Education"),
    SUPPORT_COACHING("Support & Coaching"),
    SPORTS_FITNESS("Sports & Fitness"),
    TECHNOLOGY("Technology"),
    TRAVEL("Travel");

    private final String displayName;

    EventCategory(String displayName) {
        this.displayName = displayName;
    }

    public static EventCategory fromDisplayName(String displayName) {
        return Arrays.stream(EventCategory.values())
                .filter(category -> category.getDisplayName().equalsIgnoreCase(displayName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown event category: " + displayName));

    }
}
