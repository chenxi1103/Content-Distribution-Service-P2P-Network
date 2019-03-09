import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class clientThread implements Runnable{
    private Node node;
    private Node neighbor;
    private static final int TIMEOUT = 10000;
    private static final int MAX = 3;
    private boolean flag = true;
    clientThread(Node node, Node neighbor) {
        this.node = node;
        this.neighbor = neighbor;
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
            while (flag) {
                call(this.neighbor);
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void setFlag(boolean flag) {
        this.flag = flag;
    }

    private void call(Node neighbor) {
        {
            try {
                InetAddress add = InetAddress.getByName("localhost");
                // sent from random port
                DatagramSocket dsock = new DatagramSocket();

                String message1 = node.toString() + node.getDistance(neighbor.getUuid()) + "\n";

                byte arr[] = message1.getBytes();
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
                        if (!dreceive.getAddress().equals(add)) {
                            throw new IOException("Received Packet from Unknown Place");
                        }
                        receiveFlag = true;
                    } catch (InterruptedIOException e) {
                        tries++;
                        System.out.println("Time out for the " + tries + " try");
                    }
                }

                if(receiveFlag) {
                    System.out.println("Received data from server: ");
                    String data = new String(dreceive.getData(),0,dreceive.getLength())
                            + "from " + dreceive.getAddress().getHostAddress() + ":" + dreceive.getPort();
                    System.out.println(data);
                } else {
                    System.out.println("No response!");
                    node.removeDead(neighbor);
                    node.updateConfigFile();
                    dsock.close();
                    flag = false;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
