package com.example.helloworld.service;

import com.example.helloworld.exception.InvalidInputException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

// Name must not be empty and must start with a letter from A to M
@Service
public class GreetingService {

    public String greet(String name) {
        String trimmed = name == null ? "" : stripSpaces(name);
        if (trimmed.isEmpty()) {
            throw new InvalidInputException("Name is missing or empty");
        }

        char first = trimmed.charAt(0);

        if (!isInFirstHalfOfAlphabet(first)) {
            throw new InvalidInputException("Name must start with a letter between A and M");
        }

        // Capitalize the first letter
        return "Hello " + StringUtils.capitalize(trimmed);
    }

    // Trim spaces, including non-breaking spaces
    private static String stripSpaces(String value) {
        int start = 0;
        int end = value.length();
        while (start < end && isSpace(value.charAt(start))) {
            start++;
        }
        while (end > start && isSpace(value.charAt(end - 1))) {
            end--;
        }
        return value.substring(start, end);
    }

    private static boolean isSpace(char c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c);
    }

    // Only English letters A-M
    private static boolean isInFirstHalfOfAlphabet(char c) {
        return (c >= 'A' && c <= 'M') || (c >= 'a' && c <= 'm');
    }
}
