import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LSPacket {
    private Node router;
    private int seqNum;
    private List<Node> nodeList;

    LSPacket(Node router, int seqNum, HashMap<String, Node> aliveNodes) {
        this.router = router;
        this.seqNum = seqNum;
        this.nodeList = new ArrayList<>();
        for (String key : aliveNodes.keySet()) {
            nodeList.add(aliveNodes.get(key));
        }
    }

    int getSeqNum() {
        return this.seqNum;
    }

    Node getRouter() {
        return this.router;
    }

    List<Node> getNodeList() {
        return this.nodeList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(seqNum + "\n");
        sb.append(router.nodeInfo() + "\n");
        for (String key : router.getAliveNeighbors().keySet()) {
                Node currNode = router.getAliveNeighbors().get(key);
            String info =
                    currNode.nodeInfo()
                            + router.getDistance(currNode.getUuid())
                            + "\n";
            sb.append(info);
        }
        return sb.toString();
    }


    String mapString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"" + router.getName() + "\":{");

        for (String key : router.getAliveNeighbors().keySet()) {
            String id = router.getAliveNeighbors().get(key).getUuid();
            String name = router.getAliveNeighbors().get(key).getName();
            sb.append("\"" + name
                    + "\":" + router.getDistance(id) + ",");
        }
        String str = sb.toString().substring(0, sb.toString().length()-1) + "}," + "\n";
        return str;
    }
}
