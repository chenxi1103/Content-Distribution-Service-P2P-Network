import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class Node {
    private String file;
    private String uuid;
    private String name;
    private int backend_port;
    private int peer_count;
    private int seqNum;
    private String ip;
    private List<Node> neighbors;
    private HashMap<String, Integer> distanceMetric;
    private HashMap<String, Node> aliveNeighbors;
    private HashMap<String, LSPacket> LSDB;
    private List<clientThread> clientThreadList;
    List<LSPThread> lspThreads;

    /**
     * Node Constructor
     */
    Node(String uuid) {
        this.uuid = uuid;
        this.name = "default";
        this.seqNum = 0;
        this.distanceMetric = new HashMap<>();
        this.neighbors = new ArrayList<>();
        this.aliveNeighbors = new HashMap<>();
        this.clientThreadList = new ArrayList<>();
        this.LSDB = new HashMap<>();
        this.lspThreads = new ArrayList<>();
    }

    String mapString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"" + this.name + "\":");
        for (String id : aliveNeighbors.keySet()) {
            Node currNode = aliveNeighbors.get(id);
            sb.append("{\"" + currNode.getName() + "\":" + distanceMetric.get(currNode.getUuid()) + ",");
        }
        String str = sb.toString().substring(0, sb.toString().length()-1) + "},";
        return str;
    }

    HashMap<String, LSPacket> getLSDB() {
        return this.LSDB;
    }
    void addNewLSPThread(LSPThread thread) {
        this.lspThreads.add(thread);
    }

    void setPacket(String id, LSPacket packet) {
        if (!id.equals(this.uuid)) {
            LSDB.put(id, packet);
        }
    }

    String getName() {
        return "node" + this.uuid.substring(0,3);
    }

    int getSeqNum() {
        return this.seqNum;
    }
    void setSeqNum(int seq) {
        this.seqNum = seq;
    }

    Node getNeighbor(String id) {
        return aliveNeighbors.get(id);
    }

    void addNewClientThread(clientThread thread) {
        clientThreadList.add(thread);
    }
    int getNumClients() {
        return clientThreadList.size();
    }

    void killAllThreads() {
        for (clientThread thread : clientThreadList) {
            thread.setFlag(false);
        }
        for (LSPThread lspThread : lspThreads) {
            lspThread.setFlag(false);
        }
    }
    HashMap<String, Node> getAliveNeighbors() {
        return aliveNeighbors;
    }

    void printAliveNodes() {
        StringBuilder sb = new StringBuilder();
        sb.append("[{");
        for (String key : aliveNeighbors.keySet()) {
            Node currNode = aliveNeighbors.get(key);
            sb.append(currNode.toJson());
            sb.append("\"metric\":" + distanceMetric.get(currNode.getUuid()) + "},");
        }
        String str = sb.toString().substring(0, sb.toString().length() - 1) + "]";
        System.out.println(str);
    }

    void updateConfigFile() throws IOException {
        File file = new File(this.file);
        BufferedWriter bufferedWriter1 = new BufferedWriter(new FileWriter(file));
        bufferedWriter1.write(staticInfo());
        int index = 0;
        for (String key : aliveNeighbors.keySet()) {
            String str = "peer_" + index + " = " + aliveNeighbors.get(key).nodeInfo()
                    + distanceMetric.get(aliveNeighbors.get(key).getUuid()) + "\n";
            bufferedWriter1.write(str);
            index++;
        }
        bufferedWriter1.close();
    }

    void setFile(String file) {
        this.file = file;
    }

    String getIp() {
        return this.ip;
    }

    void removeDead(Node dead) {
        this.aliveNeighbors.remove(dead.getUuid());
    }

    boolean hasNeighbor(String id) {
        return this.aliveNeighbors.containsKey(id);
    }

    void addNewAlive(Node alive) {
        this.aliveNeighbors.put(alive.getUuid(), alive);
    }


    void setName(String name) {
        this.name = name;
    }

    void setBackend_port(int backend_port) {
        this.backend_port = backend_port;
    }

    int getBackend_port() {
        return this.backend_port;
    }

    void setIp(String ip) {
        this.ip = ip;
    }

    int getNumNeighbor() {
        return neighbors.size();
    }

    List<Node> getNeighbors() {
        return this.neighbors;
    }

    String getUuid() {
        return this.uuid;
    }

    void addNeighbor(String info) {
        String[] infos = info.split(",");
        if (infos.length == 4) {
            // uuid
            Node neighbor = new Node(infos[0]);
            // ip
            neighbor.setIp(infos[1].trim());
            // backend_port
            neighbor.setBackend_port(Integer.parseInt(infos[2]));
            neighbor.setName("node" + infos[0].substring(0,3));
            this.aliveNeighbors.put(neighbor.getUuid(), neighbor);
            this.neighbors.add(neighbor);
            // metrics
            this.distanceMetric.put(neighbor.getUuid(),
                    Integer.parseInt(infos[3]));
        }
    }

    int getDistance(String id) {
        return this.distanceMetric.get(id);
    }

    void setDistance(String id, int distance) {
        this.distanceMetric.put(id, distance);
    }

    @Override
    public String toString() {
        return "UUID:" + uuid + "\n" + "IP:" + ip + "\n" +
                "Backend Port:" + backend_port + "\n";
    }
    private String toJson() {
        return "uuid:" + "\"" + this.uuid + "\","
                + "name:" + "\"" + this.name + "\","
                + "host:" + "\"" + this.ip + "\","
                + "backend:" + this.backend_port + ",";
    }
    private String staticInfo() {
        return "uuid = " + uuid + "\n" + "name = " + name + "\n" +
                "backend_port = " + backend_port + "\n" + "peer_count = " + aliveNeighbors.size() + "\n";
    }

    String nodeInfo() {
        return uuid + "," + ip + "," + backend_port + ",";
    }
}
