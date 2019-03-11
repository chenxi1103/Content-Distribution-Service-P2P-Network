import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class FloodThread implements Runnable{
    private Node node;
    private Node neighbor;
    private LSPacket packet;
    private static final int TIMEOUT = 10000;
    private static final int MAX = 3;
    FloodThread(Node node, Node neighbor, LSPacket packet) {
        this.node = node;
        this.neighbor = neighbor;
        this.packet = packet;
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
    public void run() {
        try{
            InetAddress add = InetAddress.getByName("localhost");
            // sent from random port
            DatagramSocket dsock = new DatagramSocket();
            String forwardInfo = this.packet.toString();
            byte arr[] = forwardInfo.getBytes();
            DatagramPacket dpack = new DatagramPacket(arr, arr.length, add, neighbor.getBackend_port());

            byte[] buf = new byte[1024];
            DatagramPacket dreceive = new DatagramPacket(buf,1024);
            boolean receiveFlag = false;
            dsock.setSoTimeout(TIMEOUT);
            int tries = 0;
            while (!receiveFlag && tries < MAX) {
                dsock.send(dpack);
                try {
                    dsock.receive(dreceive);
//                    if (!dreceive.getAddress().equals(add)) {
//                        throw new IOException("Received Packet from Unknown Place");
//                    }
                    receiveFlag = true;
                } catch (InterruptedIOException e) {
                    tries++;
                }
            }

            if(!receiveFlag) {
                System.out.println("No response!");
                node.removeDead(neighbor);
                node.updateConfigFile();
                dsock.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
