package com.stego_api.controller;

//import com.fasterxml.jackson.core.JsonProcessingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.geojson.*;
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

@RestController
public class SteganographicController {

    private final SteganographicRepository steganographicRepository;

    public SteganographicController(SteganographicRepository steganographicRepository) {
        this.steganographicRepository = steganographicRepository;
    }

    @PostMapping(value = "/embedMessage", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String embedValues(@RequestBody String body) throws NoSuchAlgorithmException {
        System.out.println("post request from asp, date: " + new Date().toString());
        // Get Json instance contains file content and owner
        Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64Adapter()).create();
        StegoDTO responseModel = customGson.fromJson(body, StegoDTO.class);

        FeatureCollection featureCollection = FeatureCollection.fromJson(responseModel.fileContent);
        int featureCount = featureCollection.features().size();
        // Get hash of attributes: map owner
        String mainAttrHash = HashUtil.getHash(responseModel.owner);
        // Get numbers of edges based on main attributes hash
        int[] edgesByHash = GeometryHelper.getEdgesByHash(mainAttrHash);

        int polygonIndex = 0;
        List<int[]> coefficientsList = new ArrayList<>();

        for (int i = 0; i < featureCount; i++) {
            Feature currentFeature = featureCollection.features().get(i);
            // Algorithm processes only polygons
            // MultiPolygon
            if (currentFeature.geometry().type().equals(GeometryHelper.multiPolygonType)) {
                MultiPolygon multiPolygon = (MultiPolygon) currentFeature.geometry();
                for (Polygon polygon : multiPolygon.polygons()) {
                    coefficientsList.add(GeometryHelper.embedToPolygon(polygon, polygonIndex, currentFeature.properties(), edgesByHash, coefficientsList));
                    ++polygonIndex;
                }
            }
            // Polygon
            if (currentFeature.geometry().type().equals(GeometryHelper.polygonType)) {
                Polygon polygon = (Polygon) currentFeature.geometry();
                coefficientsList.add(GeometryHelper.embedToPolygon(polygon, polygonIndex, currentFeature.properties(), edgesByHash, coefficientsList));
                ++polygonIndex;
            }
        }
        return JsonSerializationHelper.toGeoJSON(featureCollection);
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
        //String mainAttrHash = HashUtil.getHash(responseModel.id + responseModel.owner);
        String mainAttrHash = HashUtil.getHash(responseModel.owner);
        // Get numbers of edges based on main attributes hash
        int[] edgesByHash = GeometryHelper.getEdgesByHash(mainAttrHash);
        // todo delete
//        edgesByHash[29] = 267;
//        edgesByHash[30] = 269;

        int polygonIndex = 0;

        for (int i = 0; i < featureCount; i++) {
            Feature currentFeature = featureCollection.features().get(i);
            if (currentFeature.geometry().type().equals(GeometryHelper.polygonType)) {
                Polygon polygon = (Polygon) currentFeature.geometry();
                List<Point> coordinates = polygon.coordinates().get(0);
                // Get coefficients by property hash that should be embed
                String hashOfProperties = HashUtil.getHash(currentFeature.properties().toString());
                coefficientsList.add(HashUtil.getDecimalHashArray(hashOfProperties));

                if (polygonIndex == 0) {
                    if (!GeometryHelper.checkEmbedInfoInFirstPolygon(coordinates, coefficientsList.get(polygonIndex))) {
                        return false;
                    }

                } else {
                    if (!GeometryHelper.checkEmbedInfo(coordinates, coefficientsList.get(polygonIndex - 1), coefficientsList.get(polygonIndex), edgesByHash)) {
                        return false;
                    }

                }
                polygonIndex++;
            }
        }
        return true;
    }
}
