package com.stego_api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stego_api.entity.StegoDTO;
import com.stego_api.entity.ThemeParkRide;
import com.stego_api.helper.ByteArrayToBase64Adapter;
import com.stego_api.repository.ThemeParkRideRepository;
import mil.nga.sf.GeometryType;
import mil.nga.sf.geojson.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;


//import org.apache.tomcat.util.json.JSONParser;
//import org.h2.util.json.JSONObject;

@RestController
public class ThemeParkRideController {

    private final ThemeParkRideRepository themeParkRideRepository;

    public ThemeParkRideController(ThemeParkRideRepository themeParkRideRepository) {
        this.themeParkRideRepository = themeParkRideRepository;
    }

    @GetMapping(value = "/ride", produces = MediaType.APPLICATION_JSON_VALUE)
    public Iterable<ThemeParkRide> getRides() {
        return themeParkRideRepository.findAll();
    }

    @GetMapping(value = "/ride/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ThemeParkRide getRide(@PathVariable long id){
        return themeParkRideRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Invalid ride id %s", id)));
    }

    @PostMapping(value = "/ride", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ThemeParkRide createRide(@Valid @RequestBody ThemeParkRide themeParkRide) {
        return themeParkRideRepository.save(themeParkRide);
    }

    @PostMapping(value = "/embedMessage", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE) // Main Logic
    public String getTest(@RequestBody String body) throws JsonProcessingException, NoSuchAlgorithmException {
        System.out.println ( "post request from asp, date: " + new Date().toString() );
        // Get Json instance contains message and file content
        Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new ByteArrayToBase64Adapter()).create();
        StegoDTO responseModel = customGson.fromJson(body, StegoDTO.class);

        // Get GeoJson from file content
        GeoJsonObject geoJsonObject = new ObjectMapper().readValue(responseModel.fileContent, GeoJsonObject.class);

        // Get list of features
        FeatureCollection featureCollection = FeatureConverter.toFeatureCollection(geoJsonObject);
        List<Feature> features = featureCollection.getFeatures();

        // Random used to get indexes to embed info
        Random random = new Random(5);

        final int numOfEdge = 16;
        int[] indexOfEdge = new int[numOfEdge];

        for (Feature feature : features) {
            var type = feature.getGeometryType().getName();

            List<List<List<Position>>> allCoordinates;
            List<List<Position>> currentCoordinates;

            // Get hash of properties
            // TODO: check why no props
            Map<String, Object> prop = feature.getProperties();
            var o = prop.get("prop1");
            var obj = prop.values();
            String[] arr = (String[]) obj.toArray();
            var str = arr.toString();
            byte[] bytesOfMessage = str.getBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);

            // Add points
            if (type == GeometryType.POLYGON.getName() ) {
                Polygon pol = (Polygon) feature.getGeometry();
                currentCoordinates = pol.getCoordinates();
                // Get random indexes of edges
                for (int i = 1; i < numOfEdge; i++) {
                    indexOfEdge[i] = random.nextInt(currentCoordinates.size());
                }
            }

            MultiPolygon mpol = (MultiPolygon) feature.getGeometry();
            List<List<List<Position>>> coordinates1 = mpol.getCoordinates();

        }

        return  body.toString();
    }
}
