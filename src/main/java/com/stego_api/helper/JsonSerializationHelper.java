package com.stego_api.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSerializationHelper {
    public static String replaceCoordinatesInJSON(String to, String from) {
        //builders to efficiently change jsons
        StringBuilder toBuilder = new StringBuilder(to);
        StringBuilder fromBuilder = new StringBuilder(from);

        //regex pattern which we want to find in json
        String COORDINATES_REGEX_PATTERN = "([-+]?\\d+\\.\\d+)\\s*,\\s*([-+]?\\d+\\.\\d+)";
        Pattern pattern = Pattern.compile(COORDINATES_REGEX_PATTERN);

        //matchers to find created patters
        Matcher toMatcher = pattern.matcher(toBuilder);
        Matcher fromMatcher = pattern.matcher(fromBuilder);

        //go through json with short values and find coordinates
        while (toMatcher.find()) {
            //for every coordinate in short json find exactly the same match by index in full json
            fromMatcher.find();

            //and replace found full value in short json
            toBuilder.replace(toMatcher.start(), toMatcher.end(), fromMatcher.group());
        }

        return toBuilder.toString();
    }
}
