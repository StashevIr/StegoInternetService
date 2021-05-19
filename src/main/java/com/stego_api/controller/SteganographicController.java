package com.stego_api.controller;

//import com.fasterxml.jackson.core.JsonProcessingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.stego_api.entity.StegoDTO;
import com.stego_api.helper.ByteArrayToBase64Adapter;
import com.stego_api.helper.GeometryHelper;
import com.stego_api.helper.HashUtil;
import com.stego_api.helper.JsonSerializationHelper;
import com.stego_api.repository.SteganographicRepository;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//import com.github.filosganga.geogson.model.Feature;
//import com.github.filosganga.geogson.model.FeatureCollection;
//import com.github.filosganga.geogson.model.Polygon;
//import com.github.filosganga.geogson.model.positions.AreaPositions;

//import com.github.filosganga.geogson.gson.utils.FeatureUtils;


//import org.apache.tomcat.util.json.JSONParser;
//import org.h2.util.json.JSONObject;

@RestController
public class SteganographicController {

    private final SteganographicRepository steganographicRepository;

    public SteganographicController(SteganographicRepository steganographicRepository) {
        this.steganographicRepository = steganographicRepository;
    }

    @PostMapping(value = "/embedMessage", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String embedValues(@RequestBody String body) throws NoSuchAlgorithmException {
        System.out.println("post request from asp, date: " + new Date().toString());
        // Get Json instance contains file content, owner and map ID
        Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64Adapter()).create();
        StegoDTO responseModel = customGson.fromJson(body, StegoDTO.class);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseModel.fileContent);

        int featureCount = featureCollection.features().size();

        List<int[]> coefficientsList = new ArrayList<>();
        // Get hash of attributes: map ID and map owner
        String mainAttrHash = HashUtil.getHash(responseModel.id + responseModel.owner);
        // Get numbers of edges based on main attributes hash
        int[] edgesByHash = GeometryHelper.getEdgesByHash(mainAttrHash);

        // todo delete
        edgesByHash[29] = 267;
        edgesByHash[30] = 269;

        boolean result = false;
        int polygonIndex = 0;

        for (int i = 0; i < featureCount; i++) {
            Feature currentFeature = featureCollection.features().get(i);

            if (currentFeature.geometry().type().equals(GeometryHelper.typePolygon)) {

                // todo to delete
                Polygon polygon = (Polygon) currentFeature.geometry();
                List<Point> coordinates = polygon.coordinates().get(0);

                if (polygonIndex == 0) {
                    coefficientsList.add(GeometryHelper.embedToPolygon(currentFeature, true, edgesByHash, null));
                    result = GeometryHelper.checkEmbedInfoInFirstPolygon(coordinates, coefficientsList.get(polygonIndex));
                } else {
                    coefficientsList.add(GeometryHelper.embedToPolygon(currentFeature, false, edgesByHash, coefficientsList.get(polygonIndex - 1)));
                    result = GeometryHelper.checkEmbedInfo(coordinates, coefficientsList.get(polygonIndex - 1), coefficientsList.get(polygonIndex), edgesByHash);
                }
                ++polygonIndex;
            }
        }
        // Standard Mapbox .toJson loses precision that why new parser was written.
        // Due to some fucking limitations of Matcher that should work with long text but it don't ( :)) )
        // we replace 'long' coordinates to Json from beginning and ending. Fuck time limits!1!!
        // It works.

        String firstPart = JsonSerializationHelper.replaceCoordinatesInJSON(featureCollection.toJson(), featureCollection.toString());
        String secondPart = JsonSerializationHelper.replaceCoordinatesInJSON(new StringBuilder(firstPart).reverse().toString(), new StringBuilder(featureCollection.toString()).reverse().toString());
        return new StringBuilder(secondPart).reverse().toString();
        //return JsonSerializationHelper.replaceCoordinatesInJSON(featureCollection.toJson(), featureCollection.toString());
    }


    @PostMapping(value = "/checkMap", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public boolean checkMap(@RequestBody String body) throws NoSuchAlgorithmException {
        System.out.println("post request from asp, date: " + new Date().toString());
        // Get Json instance contains file content, owner and map ID
        Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64Adapter()).create();
        StegoDTO responseModel = customGson.fromJson(body, StegoDTO.class);
        // Get features
        FeatureCollection featureCollection = FeatureCollection.fromJson(responseModel.fileContent);
        int featureCount = featureCollection.features().size();

        List<int[]> coefficientsList = new ArrayList<>();
        // Get hash of attributes: map ID and map owner
        String mainAttrHash = HashUtil.getHash(responseModel.id + responseModel.owner);
        // Get numbers of edges based on main attributes hash
        int[] edgesByHash = GeometryHelper.getEdgesByHash(mainAttrHash);
        // todo delete
        edgesByHash[29] = 267;
        edgesByHash[30] = 269;

        int polygonIndex = 0;

        for (int i = 0; i < featureCount; i++) {
            Feature currentFeature = featureCollection.features().get(i);
            if (currentFeature.geometry().type().equals(GeometryHelper.typePolygon)) {
                Polygon polygon = (Polygon) currentFeature.geometry();
                List<Point> coordinates = polygon.coordinates().get(0);
                // Get coefficients by property hash that should be embed
                String hashOfProperties = HashUtil.getHash(currentFeature.properties().toString());
                coefficientsList.add(HashUtil.getDecimalHashArray(hashOfProperties));

                if (polygonIndex == 0) {
                    if (!GeometryHelper.checkEmbedInfoInFirstPolygon(coordinates, coefficientsList.get(polygonIndex))) {
                        return false;
                    }
                    ;
                } else {
                    if (!GeometryHelper.checkEmbedInfo(coordinates, coefficientsList.get(polygonIndex - 1), coefficientsList.get(polygonIndex), edgesByHash)) {
                        return false;
                    }
                    ;
                }
                polygonIndex++;
            }
        }
        return true;
    }
}
