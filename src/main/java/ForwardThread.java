public class ForwardThread implements Runnable {
    private Node node;
    private boolean flag;

    ForwardThread(Node node) {
        this.node = node;
        this.flag = true;
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
                for (String key : node.getAliveNeighbors().keySet()) {
                    Node currNeighbor = node.getAliveNeighbors().get(key);
                    for (String id : node.getLSDB().keySet()) {
                        FloodThread floodThread = new FloodThread(node, currNeighbor, node.getLSDB().get(id));
                        new Thread(floodThread).start();
                    }

                }
                Thread.sleep(10000);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    void kill() {
        this.flag = false;
    }
}
