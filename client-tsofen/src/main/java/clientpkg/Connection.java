package clientpkg;

import java.io.IOException;

import java.net.Socket;

public class Connection{
    private String ip;
    private int port;
    private Socket socket;
    /**
     * connection constructor
     * @param ip from server class
     * @param port from server class
     * */
    public Connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    /**
     * create socket and run server handler
     * */
    public void start() {
        try {
            socket = new Socket(ip,port);
            ServerHandler serverHandler = new ServerHandler(socket);
            serverHandler.start();

        } catch (IOException e) {
            System.exit(0);
        }
    }

}
