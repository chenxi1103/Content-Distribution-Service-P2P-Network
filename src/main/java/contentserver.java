import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.UUID;

public class contentserver {
    public static void main(String args[]) throws IOException{
        String toBeInserted;
        if(args.length < 2 || !args[0].equals("-c")) {
            System.out.println("Sorry! please give a valid input. (e.g. -c + file path)");
            return;
        }
        String filepath = args[1];
        File file = new File(filepath);
        BufferedReader reader = null;
        HashMap<String, String> map = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String str = null;
            int line = 1;
            while ((str = reader.readLine()) != null) {
                String[] currLine = str.split("=");
                if (currLine.length == 2) {
                    map.put(currLine[0].trim(), currLine[1].trim());
                }
            }

            // Generate a node, and set its neighbors
            String id;
            if (map.containsKey("uuid")) {
                id = map.get("uuid");
            } else {
                id = UUID.randomUUID().toString();
                map.put("uuid",id);
            }

            // Update the conf file
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            toBeInserted = "uuid = " + id + "\n";

            if (map.containsKey("name")) {
                toBeInserted += "name = " + map.get("name") + "\n";
            }
            if (map.containsKey("backend_port")) {
                toBeInserted += "backend_port = " + map.get("backend_port") + "\n";
            }
            if (map.containsKey("peer_count")) {
                int size = Integer.parseInt(map.get("peer_count"));
                toBeInserted += "peer_count = " + size + "\n";
                for (int i = 0; i < size; i++) {
                    String key = "peer_" + i;
                    if (map.containsKey(key)) {
                        toBeInserted += key + " = " + map.get(key) + "\n";
                    }
                }

            }
            bufferedWriter.write(toBeInserted);
            bufferedWriter.close();


            // Generate a node, and set its neighbors
            Node node = new Node(id);
            node.setFile(filepath);

            // get local host ip
            InetAddress addr=InetAddress.getLocalHost();
            node.setIp(addr.getHostAddress());

            if (map.containsKey("name")) {
                node.setName(map.get("name"));
            }
            if (map.containsKey("backend_port")) {
                node.setBackend_port(Integer.parseInt(map.get("backend_port")));
            }

            if (map.containsKey("peer_count")) {
                int size = Integer.parseInt(map.get("peer_count"));
                for (int i = 0; i < size; i++) {
                    String key = "peer_" + i;
                    if (map.containsKey(key)) {
                        node.addNeighbor(map.get(key));
                    }
                }

            }

            // Server Thread
            serverThread listener = new serverThread(node);
            new Thread(listener).start();
            // Keep Alive Client Threads + LSPThreads
            for (String key : node.getAliveNeighbors().keySet()) {
                clientThread client = new clientThread(node,node.getAliveNeighbors().get(key));
                LSPThread lspThread = new LSPThread(node, node.getAliveNeighbors().get(key));

                new Thread(client).start();
                new Thread(lspThread).start();
                node.addNewClientThread(client);
                node.addNewLSPThread(lspThread);
            }



            // ForwardFloodingThread
            ForwardThread forwardThread = new ForwardThread(node);
            new Thread(forwardThread).start();

            Scanner scanner = new Scanner(System.in);
            boolean flag = true;
            while (flag) {
                String input = scanner.nextLine();
                // Check the UUID
                if (input.toLowerCase().equals("uuid")) {
                    String output = "{\"uuid\":\"" + map.get("uuid") + "\"}";
                    System.out.println(output);
                }

                // Add neighbors
                else if(input.startsWith("addneighbor")) {
                    String neighborInfo = input.split(" ")[1];
                    String[] info = neighborInfo.split("=");
                    String uuid = info[1].substring(0, info[1].length() - 4);
                    String host = info[2].substring(0,info[2].length() - 7);
                    String backend = info[3].substring(0, info[3].length() - 6);
                    String metric = info[4];
                    String realInfo = uuid+","+host+","+backend+","+metric;

                    // update the config file
                    BufferedWriter bufferedWriter1 = new BufferedWriter(new FileWriter(file));
                    toBeInserted += "peer_" + node.getNumNeighbor() + " = " + realInfo + "\n";
                    bufferedWriter1.write(toBeInserted);
                    bufferedWriter1.close();

                    node.addNeighbor(realInfo);

                    clientThread client = new clientThread(node, node.getNeighbor(uuid));

                    new Thread(client).start();
                    node.addNewClientThread(client);
                }


                // Keep alive
                else if (input.toLowerCase().equals("neighbors")) {
                    node.printAliveNodes();
                }

                else if (input.toLowerCase().equals("map")) {
                    System.out.println("{" + node.mapString());
                    for (String key : node.getLSDB().keySet()) {
                        System.out.println(node.getLSDB().get(key).mapString());
                    }

                }

                else if (input.toLowerCase().equals("rank")) {

                }

                else if (input.toLowerCase().equals("kill")) {
                    listener.setFlag(false);
                    forwardThread.kill();
                    node.killAllThreads();
                    flag = false;
                    System.out.println("Killed!");
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    System.out.println(e1.getMessage());
                }
            }
        }
    }

}
