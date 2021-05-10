package com.stego_api.helper;

//import org.geotools.data.DataStore;
//import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.feature.FeatureIterator;
//import org.opengis.feature.Property;
//import org.opengis.feature.simple.SimpleFeature;
//import org.springframework.http.codec.cbor.Jackson2CborEncoder;
//import org.springframework.http.codec.json.AbstractJackson2Encoder;

/** Parses ESRI shapefiles and extracts all spatial/geometric data with attributes */
public class ShapefileParser {

//    private static Base64 map;
//
//    public static void main( String[] args) {
//        String filePath;
//        if (args.length == 1) {
//            filePath = args[0];
//        }
//        else {
//            throw new RuntimeException("Call with path to SHP file as single argument");
//        }
//
//        System.out.println(parse(filePath));
//    }
//
//    public static String parse(final String filePath) {
//        final LinkedList<LinkedList<String>> entries = extractRecords(filePath);
//
//        StringBuilder out = new StringBuilder();
//
//        for (LinkedList<String> entry : entries) {
//            out.append(map.encode(entry));
//            out.append("\n");
//        }
//
//        return out.toString();
//    }
//
//    protected static LinkedList<LinkedList<String>> extractRecords(final String filePath) {
//        final LinkedList<LinkedList<String>> out = new LinkedList<LinkedList<String>>();
//
//        try {
//            final DataStore dataStore = openDataStore(filePath);
//            final String[] typeNames = dataStore.getTypeNames();
//
//            final FeatureIterator<SimpleFeature> iterator = dataStore.getFeatureSource(typeNames[0]).getFeatures().features();
//
//            try {
//                SimpleFeature feature = null;
//                LinkedList<String> headers = null;
//                LinkedList<String> keys;
//                LinkedList<String> values;
//
//                while (iterator.hasNext()) {
//                    feature = iterator.next();
//
//                    keys = new LinkedList<String>();
//                    values = new LinkedList<String>();
//
//                    // feature.getDefaultGeometry().toString()
//
//                    for (Property p : feature.getProperties()) {
//                        keys.add(p.getName().toString());
//                        values.add(p.getValue().toString());
//                    }
//
//                    out.add(values);
//
//                    if (headers == null) {
//                        headers = keys;
//                    }
//                    else {
//                        if (!keys.equals(headers)) {
//                            throw new RuntimeException("Keys vary in number, order or content");
//                        }
//                    }
//                }
//
//                if (headers != null) {
//                    out.addFirst(headers);
//                }
//
//                return out;
//            }
//            finally {
//                iterator.close();
//                dataStore.dispose();
//            }
//
//        }
//        catch (Throwable e) {
//            e.printStackTrace();
//        }
//
//        return out;
//    }
//
//    protected static DataStore openDataStore(final String filePath) throws IOException {
//        final File file = new File(filePath);
//        return new ShapefileDataStore(file.toURI().toURL());
//
//        //final Map<String, Object> params = new HashMap<String, Object>();
//        //params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
//        //params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, Boolean.TRUE);
//        //return DataStoreFinder.getDataStore(params);
//    }

}