package clientpkg;

import java.io.IOException;

import java.net.Socket;

public class Connection{
    private String ip;
    private int port;
    private Socket socket;


    public Connection(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }


    public void start() {
        try {
            socket = new Socket(ip,port);
            ServerHandler serverHandler = new ServerHandler(socket);
            serverHandler.start();

        } catch (IOException e) {

        }
    }

}
