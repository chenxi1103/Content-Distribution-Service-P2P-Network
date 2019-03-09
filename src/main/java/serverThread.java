import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;
public class serverThread implements Runnable {
    private Node node;
    private boolean flag = true;
    serverThread(Node node) {
        this.node = node;
    }
    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        try{
            /**
             * Node as a server
             */
            // server listen at listenPort
            int listenPort = node.getBackend_port();
            DatagramSocket dsock = new DatagramSocket(listenPort);
            // receive data sent from the client
            byte arr1[] = new byte[1024];
            DatagramPacket receive = new DatagramPacket(arr1, arr1.length);

            while (flag) {
                // Receive message from client
                dsock.receive(receive);
                byte arr2[] = receive.getData();
                int packSize = receive.getLength();
                String s2 = new String(arr2, 0, packSize);
                String[] infoArray = s2.split("\n");

                String uuid = infoArray[0].split(":")[1];

                if (!node.hasNeighbor(uuid)) {
                    String ip = infoArray[1].split(":")[1];
                    String port = infoArray[2].split(":")[1];
                    String distance = infoArray[3];
                    Node newNeighbor = new Node(uuid);
                    newNeighbor.setIp(ip);
                    newNeighbor.setBackend_port(Integer.parseInt(port));
//                    newNeighbor.setDistance(uuid, Integer.parseInt(distance));

                    node.addNewAlive(newNeighbor);
                    node.setDistance(newNeighbor.getUuid(), Integer.parseInt(distance));

                    node.updateConfigFile();
                    System.out.println("One new alive neighbor is added!");
                    clientThread thread = new clientThread(node, newNeighbor);
                    new Thread(thread).start();
                    node.addNewClientThread(thread);
                }

                System.out.println(new Date() + "  " + receive.getAddress() + " : " + receive.getPort());

                // Send message back to client
                String str = node.toString();
                DatagramPacket send = new DatagramPacket(str.getBytes(), str.length(), receive.getAddress(), receive.getPort());
                dsock.send(send);
                receive.setLength(1024);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    void setFlag(boolean flag) {
        this.flag = flag;
    }
}
