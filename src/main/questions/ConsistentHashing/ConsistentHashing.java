

public class ConsistentHashing {

    // Map to store the hash ring with node positions
    private final TreeMap<Integer, String> hashRing = new TreeMap<>();

    // Map to store the mapping of data points to nodes
    private final HashMap<String, String> dataPointToNodeMap = new HashMap<>();

    // Virtual nodes per physical node
    private final int numVirtualNodes;

    // Constructor
    public ConsistentHashing(int numVirtualNodes) {
        this.numVirtualNodes = numVirtualNodes;
    }

    // Add a node to the hash ring
    public void addNode(String node) {
        for (int i = 0; i < numVirtualNodes; i++) {
            String virtualNode = node + "-" + i; // Generate virtual node name
            int hash = getHash(virtualNode); // Get hash value for virtual node
            hashRing.put(hash, node); // Map virtual node to hash value
            // Update data point to node mapping
            for (Map.Entry<String, String> entry : dataPointToNodeMap.entrySet()) {
                String dataPoint = entry.getKey();
                if (getNode(dataPoint).equals(node)) {
                    dataPointToNodeMap.put(dataPoint, node);
                }
            }
        }
    }

    // Remove a node from the hash ring
    public void removeNode(String node) {
        for (int i = 0; i < numVirtualNodes; i++) {
            String virtualNode = node + "-" + i; // Generate virtual node name
            int hash = getHash(virtualNode); // Get hash value for virtual node
            hashRing.remove(hash); // Remove virtual node from hash ring
            for (Map.Entry<String, String> entry : dataPointToNodeMap.entrySet()) {
                String dataPoint = entry.getKey();
                if (entry.getValue().equals(node)) {
                    String newNode = getNode(dataPoint);
                    dataPointToNodeMap.put(dataPoint, newNode);
                }
            }
        }
    }

    // Get the node responsible for a data point
    public String getNode(String dataPoint) {
        int hash = getHash(dataPoint); // Get hash value for data point
        // Get the entry in the hash ring whose key is closest to or equal to the hash value
        Map.Entry<Integer, String> entry = hashRing.ceilingEntry(hash);
        if (entry == null) {
            // If the hash value is greater than the largest key in the ring, wrap around to the smallest key
            return hashRing.firstEntry().getValue();
        }
        return entry.getValue(); // Return the node responsible for the data point
    }

    // Helper method to generate hash value using string's hashCode method
    private int getHash(String key) {
        return key.hashCode();
    }

    public static void main(String[] args) {
        // Initialize consistent hashing with 3 virtual nodes per physical node
        ConsistentHashing consistentHashing = new ConsistentHashing(3);

        // Add nodes to the hash ring
        consistentHashing.addNode("Node1");
        consistentHashing.addNode("Node2");
        consistentHashing.addNode("Node3");

        // Store data points and their corresponding nodes
        consistentHashing.dataPointToNodeMap.put("Data1", consistentHashing.getNode("Data1"));
        consistentHashing.dataPointToNodeMap.put("Data2", consistentHashing.getNode("Data2"));
        consistentHashing.dataPointToNodeMap.put("Data3", consistentHashing.getNode("Data3"));

        // Get node responsible for a data point
        String dataPoint = "Data1";
        String node = consistentHashing.dataPointToNodeMap.get(dataPoint);
        System.out.println("Data point " + dataPoint + " is assigned to node: " + node);

        // Remove a node from the hash ring
        consistentHashing.removeNode("Node2");

        // Rebalance data points after removing a node
        for (Map.Entry<String, String> entry : consistentHashing.dataPointToNodeMap.entrySet()) {
            String dp = entry.getKey();
            String n = consistentHashing.dataPointToNodeMap.get(dp);
            System.out.println("After removing a node, data point " + dp + " is assigned to node: " + n);
        }
    }
}