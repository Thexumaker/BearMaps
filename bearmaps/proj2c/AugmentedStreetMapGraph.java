package bearmaps.proj2c;
import bearmaps.proj2c.MyTrieSet;
import bearmaps.hw4.streetmap.Node;
import bearmaps.hw4.streetmap.StreetMapGraph;
import bearmaps.proj2ab.Point;
import bearmaps.proj2ab.PointSet;
import bearmaps.proj2ab.WeirdPointSet;
import org.eclipse.jetty.util.Trie;

import java.util.*;

/**
 * An augmented graph that is more powerful that a standard StreetMapGraph.
 * Specifically, it supports the following additional operations:
 *
 *
 * @author Alan Yao, Josh Hug, David Xu
 */
public class AugmentedStreetMapGraph extends StreetMapGraph {
    private Map<Point, Node> storage;
    private PointSet points; //kdTree
    private MyTrieSet trieSet;
    private HashMap<String, String> namesToCleanNames;
    private HashMap<String, String> cleanNamesToNames;
    private HashMap<String, HashSet<Node>> seenPlaces; // Mapping name of location to node
    private HashMap<String, HashSet<String>> totalLocations; //Mapping name to other similar cleaned names

    public AugmentedStreetMapGraph(String dbPath) {
        super(dbPath);
        // You might find it helpful to uncomment the line below:
        storage = new HashMap<>();
        trieSet = new MyTrieSet();
        namesToCleanNames = new HashMap<>();
        cleanNamesToNames = new HashMap<>();

        seenPlaces = new HashMap<>();
        totalLocations = new HashMap<>();

        //Part 3

        ArrayList<Point> pointSetStorage = new ArrayList<>();
        for (Node n: this.getNodes()) {
            if (!this.neighbors(n.id()).isEmpty()) {
                Point point = new Point(n.lon(), n.lat());
                storage.put(point, n);
                pointSetStorage.add(point);

            }
            String name;
            String cleanName;
            if (n.name() != null) {
                name = n.name(); // Take both of it's names - cleaned and uncleaned
                cleanName = cleanString(name);
                namesToCleanNames.put(name, cleanName);
                cleanNamesToNames.put(cleanName, name);
                trieSet.add(cleanName);
                if (!totalLocations.containsKey(cleanName)) { // If we haven't encountered this location yet
                    HashSet<String> temp = new HashSet(); // Create a new HashSet and add this place's name into there
                    temp.add(n.name());
                    totalLocations.put(cleanName, temp);
                } else if (totalLocations.containsKey(cleanName)) {
                    totalLocations.get(cleanName).add(n.name()); // If we've already seen it, just add the name to the hashset.
                }

                if (!seenPlaces.containsKey(cleanName)) {

                    seenPlaces.put(cleanName, new HashSet<Node>());
                    seenPlaces.get(cleanName).add(n);
                }
                // If we have encountered this place already...
                else if (seenPlaces.containsKey(cleanName)) {

                    seenPlaces.get(cleanName).add(n);
                }
            }
        }
        points = new WeirdPointSet(pointSetStorage);
    }


    /**
     * For Project Part II
     * Returns the vertex closest to the given longitude and latitude.
     * @param lon The target longitude.
     * @param lat The target latitude.
     * @return The id of the node in the graph closest to the target.
     */
    public long closest(double lon, double lat) {
        Point close = points.nearest(lon,lat);
        return storage.get(close).id();
    }


    /**
     * For Project Part III (gold points)
     * In linear time, collect all the names of OSM locations that prefix-match the query string.
     * @param prefix Prefix string to be searched for. Could be any case, with our without
     *               punctuation.
     * @return A <code>List</code> of the full names of locations whose cleaned name matches the
     * cleaned <code>prefix</code>.
     */
    public List<String> getLocationsByPrefix(String prefix) {
        String cleanedPrefix = cleanString(prefix);
        List<String> prefixList = trieSet.keysWithPrefix(prefix);
        List<String> results = new ArrayList<>();
        for(String s: prefixList) {
            for (String name : totalLocations.get(s)) {
                results.add(name);

            }


        }


        return results;
    }

    /**
     * For Project Part III (gold points)
     * Collect all locations that match a cleaned <code>locationName</code>, and return
     * information about each node that matches.
     * @param locationName A full name of a location searched for.
     * @return A list of locations whose cleaned name matches the
     * cleaned <code>locationName</code>, and each location is a map of parameters for the Json
     * response as specified: <br>
     * "lat" -> Number, The latitude of the node. <br>
     * "lon" -> Number, The longitude of the node. <br>
     * "name" -> String, The actual name of the node. <br>
     * "id" -> Number, The id of the node. <br>
     */
    public List<Map<String, Object>> getLocations(String locationName) {
        String cleanedName = cleanString(locationName);
        List<Map<String, Object>> returnList = new ArrayList<>();

        if (seenPlaces.containsKey(cleanedName)) {

            for (Node n : seenPlaces.get(cleanedName)) {

                HashMap location = new HashMap();
                location.put("lat", n.lat());
                location.put("lon", n.lon());
                location.put("name", n.name());
                location.put("id", n.id());
                returnList.add(location);
            }
        }
        return returnList;
    }


    /**
     * Useful for Part III. Do not modify.
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    private static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

}