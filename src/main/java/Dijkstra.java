import java.util.*;

class Dijkstra {
    private Node node;
    private HashMap<String, Integer> map;
    private HashMap<Integer, String> reverseMap;
    private List<List<router>> list;
    private HashMap<String, LSPacket> LSDB;
    private int[] dist;

    Dijkstra(Node node) {
        this.node = node;
        this.map = new HashMap<>();
        this.reverseMap = new HashMap<>();
        map.put(node.getUuid(),0);
        reverseMap.put(0,node.getUuid());
        int index = 1;
        for (String key : node.getLSDB().keySet()) {
            map.put(key, index);
            reverseMap.put(index, key);
            index++;
        }
        this.list = new ArrayList<>();
        this.LSDB = node.getLSDB();
        this.dist = new int[map.size()];
    }


    private void initial() {
        List<router> subList = new ArrayList<>();
        for (String uuid : node.getAliveNeighbors().keySet()) {
            int distance = node.getDistance(uuid);
            int index = map.get(uuid);
            subList.add(new router(index, distance));
        }
        this.list.add(subList);

        for (int i = 1; i < map.size(); i++) {
            List<router> sub = new ArrayList<>();
            Node currNode = LSDB.get(reverseMap.get(i)).getRouter();
            for (String uuid : currNode.getAliveNeighbors().keySet()) {
                int distance = currNode.getDistance(uuid);
                int index = map.get(uuid);
                sub.add(new router(index, distance));
            }
            list.add(sub);
        }
    }

    private void mainLogic() {
        // Initial the list
        initial();

        HashSet<Integer> processed = new HashSet<>();
        PriorityQueue<router> pq = new PriorityQueue<router>(map.size(), new Comparator<router>() {
            @Override
            public int compare(router o1, router o2) {
                return o1.distance - o2.distance;
            }
        });

        // First initial the distance tobe Integer.MAX_VALUE (infinite)
        for (int i = 1; i < dist.length; i++) {
            dist[i] = Integer.MAX_VALUE;
        }

        // Add the current node into priority queue
        pq.add(new router(0,0));

        while (processed.size() != map.size()) {
            int currNode = pq.remove().node;
            processed.add(currNode);

            int edgeDist, potentialDist = -1;

            // Explore all the neighbors of this node
            for (int i = 0; i < this.list.get(currNode).size(); i++) {
                router currRouter = this.list.get(currNode).get(i);

                if (!processed.contains(currRouter.node)) {

                    edgeDist = currRouter.distance;
                    potentialDist = dist[currNode] + edgeDist;

                    if (dist[currRouter.node] > potentialDist) {
                        dist[currRouter.node] = potentialDist;
                    }
                    pq.add(new router(currRouter.node, dist[currRouter.node]));
                }
            }
        }
    }

    void printRank() {
        mainLogic();
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        mainLogic();

        for (int i = 1; i < this.dist.length; i++) {
            String uuid = reverseMap.get(i);
            Node currNode = this.LSDB.get(uuid).getRouter();
            String name = currNode.getName();
            int distance = this.dist[i];

            sb.append("{\"" + name + ":" + distance + "},");
        }

        String str = sb.toString().substring(0, sb.toString().length() - 1) + "]";
        System.out.println(str);
    }

}
