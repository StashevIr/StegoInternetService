package com.stego_api.helper;

import com.mapbox.geojson.FeatureCollection;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonSerializationHelper {

    // Standard Mapbox .toJson loses precision that why new parser is written.
    // Coordinates with precision loss (.toJson) are replaced by coordinates from object FeatureCollection (.toString).
    // Due to some limitations of Matcher it is needed to do this operation twice: from the begin and from the end.
    public static String toGeoJSON(FeatureCollection featureCollection) {
        String firstPart = JsonSerializationHelper.replaceCoordinatesInJSON(featureCollection.toJson(), featureCollection.toString());
        String secondPart = JsonSerializationHelper.replaceCoordinatesInJSON(new StringBuilder(firstPart).reverse().toString(), new StringBuilder(featureCollection.toString()).reverse().toString());
        String map = new StringBuilder(secondPart).reverse().toString();
        return map;
    }

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
