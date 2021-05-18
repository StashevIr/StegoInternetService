package com.stego_api.helper;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class GeometryHelper {

    public static String typePolygon = "Polygon";

    public static final int COUNT_OF_EDGE = 31;
    public static final int COUNT_OF_PIECE = 16;
    public static final int HEX_MAX = 15;

    public static int[] getRandomEdges(int size, int[] indexOfEdgesWithEmbedPoints) {
        var ref = new Object() {
            int currentValue = 0;
        };
        // Random used to get indexes to embed info
        Random random = new Random(5);

        int[] indexOfEdge = new int[COUNT_OF_EDGE];
        // Get random indexes of edges
        for (int i = 0; i < COUNT_OF_EDGE; i++) {
            while (IntStream.of(indexOfEdge).anyMatch(x -> x == ref.currentValue) ||
                    IntStream.of(indexOfEdgesWithEmbedPoints).anyMatch(x -> x == ref.currentValue)) {
                ref.currentValue = random.nextInt(size - 1); // from 0?
            }
            indexOfEdge[i] = ref.currentValue;
        }
        Arrays.sort(indexOfEdge);
        return indexOfEdge;
    }

    public static int[] getEdgesByHash(String hash) {
        //int[] edges = new int[hash.length()];
        int[] edges = new int[COUNT_OF_EDGE];
        int[] decHash = HashUtil.getDecimalHashArray(hash);
        edges[0] = decHash[0];

        for (int i = 1; i < edges.length; i++) {
            edges[i] = edges[i - 1] + decHash[i] + 1; // Add 1 to avoid double index in case of 0 value of hash
        }
        return edges;
    }

    public static Point[] getPointsByIndex(List<Point> points, int index) {
        return new Point[]{
                points.get(index),
                points.get(index + 1),
                points.get(index + 2)};
    }

    public static boolean isPointsOnLine(Point[] points) {
        // Formula is: x3 * (y2 - y1) - y3 * (x2 - x1) == x1 * y2 - x2 * y1;

        if (points[0].equals(points[1])) {
            return true;
        }
        if (points[1].equals(points[2])) {
            return false;
        }

        BigDecimal firstLongitude = BigDecimal.valueOf(points[0].longitude());
        BigDecimal firstLatitude = BigDecimal.valueOf(points[0].latitude());
        BigDecimal secondLongitude = BigDecimal.valueOf(points[1].longitude());
        BigDecimal secondLatitude = BigDecimal.valueOf(points[1].latitude());
        BigDecimal thirdLongitude = BigDecimal.valueOf(points[2].longitude());
        BigDecimal thirdLatitude = BigDecimal.valueOf(points[2].latitude());

        BigDecimal leftPart = thirdLongitude.multiply(secondLatitude.subtract(firstLatitude)).subtract(thirdLatitude.multiply(secondLongitude.subtract(firstLongitude))).stripTrailingZeros();
        BigDecimal rightPart = firstLongitude.multiply(secondLatitude).subtract(secondLongitude.multiply(firstLatitude)).stripTrailingZeros();

        return leftPart.equals(rightPart);
    }

    public static Point getPointToEmbed(Point beginPoint, Point endPoint, BigDecimal hashValueToEmbed) {
        //double k = hashValueToEmbed / NUM_OF_PIECE;

//        if (hashValueToEmbed.intValue() == 0){
//            return endPoint;
//        }

        BigDecimal k = hashValueToEmbed.divide(BigDecimal.valueOf(COUNT_OF_PIECE));

        BigDecimal beginLongitude = BigDecimal.valueOf(beginPoint.longitude());
        BigDecimal beginLatitude = BigDecimal.valueOf(beginPoint.latitude());
        BigDecimal endLongitude = BigDecimal.valueOf(endPoint.longitude());
        BigDecimal endLatitude = BigDecimal.valueOf(endPoint.latitude());

        //Xc = Xa + (Xb - Xa) * k
        BigDecimal newLongitude = beginLongitude.add(k.multiply(endLongitude.subtract(beginLongitude)));
        //Yc = Ya + (Yb - Ya) * k
        BigDecimal newLatitude = beginLatitude.add(k.multiply(endLatitude.subtract(beginLatitude)));
        return Point.fromLngLat(newLongitude.doubleValue(), newLatitude.doubleValue());
    }

    public static int getCoefficient(Point[] points) {
        BigDecimal k;

        BigDecimal firstLongitude = BigDecimal.valueOf(points[0].longitude());
        BigDecimal secondLongitude = BigDecimal.valueOf(points[1].longitude());
        BigDecimal thirdLongitude = BigDecimal.valueOf(points[2].longitude());

        //BigDecimal k = secondLongitude.subtract(firstLongitude).divide(thirdLongitude.subtract(firstLongitude));
        BigDecimal subtraction = thirdLongitude.subtract(firstLongitude);
        if (subtraction.doubleValue() == 0) {
            BigDecimal firstLatitude = BigDecimal.valueOf(points[0].latitude());
            BigDecimal secondLatitude = BigDecimal.valueOf(points[1].latitude());
            BigDecimal thirdLatitude = BigDecimal.valueOf(points[2].latitude());
            subtraction = thirdLatitude.subtract(firstLatitude);
            k = secondLatitude.subtract(firstLatitude).divide(subtraction);
        } else {
            k = secondLongitude.subtract(firstLongitude).divide(subtraction);
        }

        int result = k.multiply(BigDecimal.valueOf(COUNT_OF_PIECE)).intValue();

//        if (result == 16) return 0;
//        else
        return result;
    }

    public static void addNewRandomEdge(int[] edgeIndexes, int embedIndex) {
        // Get new number of edge
        var ref = new Object() {
            int newEdge = edgeIndexes[embedIndex];
        };

        while (IntStream.of(edgeIndexes).anyMatch(x -> x == ref.newEdge)) {
            ref.newEdge += 1;
        }
        if (ref.newEdge > edgeIndexes[embedIndex + 1]) {
            edgeIndexes[embedIndex] = edgeIndexes[embedIndex + 1];
            edgeIndexes[embedIndex + 1] = ref.newEdge;
        } else {
            edgeIndexes[embedIndex] = ref.newEdge;
        }
    }

    public static void embedPropertyHash(List<Point> listOfPoints, int[] decHash, int[] edgeIndexes) {
        int currentPointIndex = 0;
        int currentEdgeIndex = 0;
        int embedIndex = 0;

        while (currentPointIndex < listOfPoints.size() - 2) {
            if (embedIndex >= edgeIndexes.length) {
                return;
            }

            Point[] points = getPointsByIndex(listOfPoints, currentPointIndex);

            boolean isPointsOnLine = isPointsOnLine(points);

            if (edgeIndexes[embedIndex] == currentEdgeIndex) {
                // Check if edge is empty
                if (isPointsOnLine) {
                    currentPointIndex += 2;

                    addNewRandomEdge(edgeIndexes, embedIndex);
                } else {
                    Point newPoint = getPointToEmbed(points[0], points[1], BigDecimal.valueOf(decHash[embedIndex]));
                    listOfPoints.add(currentPointIndex + 1, newPoint);

                    currentPointIndex += 2;
                    embedIndex++;
                }
            } else {
                if (isPointsOnLine) {
                    currentPointIndex += 2;
                } else {
                    currentPointIndex += 1;
                }
            }
            currentEdgeIndex++;
        }
    }

    public static void embedCommonHash(List<Point> listOfPoints, int[] decHash, int[] indexOfEdge) {
        // Embed points from end to avoid change of index
        for (int i = COUNT_OF_EDGE - 1; i >= 0; i--) {
            Point newPoint = getPointToEmbed(listOfPoints.get(indexOfEdge[i]), listOfPoints.get(indexOfEdge[i] + 1), BigDecimal.valueOf(decHash[i]));
            listOfPoints.add(indexOfEdge[i] + 1, newPoint);
        }
    }

    public static int[] embedToPolygon(Feature currentFeature, boolean isFirst, int[] edgesByHash, int[] previousCoefficients) throws NoSuchAlgorithmException {
        String hashOfProperties = HashUtil.getHash(currentFeature.properties().toString());
        int[] coefficients = HashUtil.getDecimalHashArray(hashOfProperties);

        Polygon polygon = (Polygon) currentFeature.geometry();
        List<Point> coordinates = polygon.coordinates().get(0);

        int size = coordinates.size(); // need to know count of coordinates before changes
        if (!isFirst) {
            GeometryHelper.embedCommonHash(coordinates, previousCoefficients, edgesByHash);
        }
        int[] randomEdges = GeometryHelper.getRandomEdges(size, edgesByHash);
        GeometryHelper.embedPropertyHash(coordinates, coefficients, randomEdges);

        return coefficients;
    }

    public static boolean checkEmbedInfo(List<Point> listOfPoints, int[] commonHash, int[] propHash, int[] edgeIndexes) {
        int currentPointIndex = 0;
        int currentEdgeIndex = 0;

        int commonHashIndex = 0;
        int propHashIndex = 0;

        while (commonHashIndex < COUNT_OF_EDGE && propHashIndex < COUNT_OF_EDGE
                && currentPointIndex < listOfPoints.size() - 3) {
            Point[] points = getPointsByIndex(listOfPoints, currentPointIndex);

            boolean isPointsOnLine = isPointsOnLine(points);
            int coefficient = 0;
            if (isPointsOnLine) {
                coefficient = getCoefficient(points);
            }

            if (edgeIndexes[commonHashIndex] == currentEdgeIndex) {
                // Check if edge is empty
                if (isPointsOnLine) {
                    if (commonHash[commonHashIndex] == coefficient) {
                        currentEdgeIndex++;

                        currentPointIndex += 2;
                        if (commonHashIndex < commonHash.length - 1) {
                            commonHashIndex++;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (isPointsOnLine) {
                    if (propHash[propHashIndex] == coefficient) {
                        currentPointIndex += 2;
                        currentEdgeIndex++;
                        if (propHashIndex < propHash.length - 1) {
                            propHashIndex++;
                        }
                    } else {
                        return false;
                    }

                } else {
                    currentPointIndex += 1;
                    currentEdgeIndex++;
                }
            }

            if (commonHashIndex == COUNT_OF_EDGE - 1 && propHashIndex == COUNT_OF_EDGE - 1) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkEmbedInfoInFirstPolygon(List<Point> listOfPoints, int[] propHash) {
        int currentPointIndex = 0;
        int propHashIndex = 0;

        while (propHashIndex < COUNT_OF_EDGE
                && currentPointIndex < listOfPoints.size() - 3) {

            Point[] points = getPointsByIndex(listOfPoints, currentPointIndex);

            if (isPointsOnLine(points)) {
                if (propHash[propHashIndex] == getCoefficient(points)) {
                    currentPointIndex += 2;
                    if (propHashIndex < propHash.length - 1) {
                        propHashIndex++;
                    }
                } else {
                    return false;
                }

            } else {
                currentPointIndex += 1;
            }

            if (propHashIndex == COUNT_OF_EDGE - 1) {
                return true;
            }
        }
        return false;
    }


//    public static int[] getEdge(Point[] points) {
//        //Point pointToCheck, Point next, Point nextnext, Point previous) {
//        if (isPointsOnLine(points[3], points[0], points[1])) {
//            return new int[]{-1, 1};
//        } else {
//            if (isPointsOnLine(points[0], points[1], points[2])) {
//                return new int[]{0, 2};
//            } else {
//                return new int[]{0, 1};
//            }
//        }
//    }

//    public static boolean checkEdge(Point[] points) {
//        //Point pointToCheck, Point next, Point nextnext, Point previous) {
//        if (isPointsOnLine(new Point[]{points[3], points[0], points[1]})) {
//            return false;
//        } else {
//            if (isPointsOnLine(new Point[]{points[0], points[1], points[2]})) {
//                return false;
//            } else {
//                return true;
//            }
//        }
//    }

}
