package serverpkg;

import mainpkg.Server;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Listening extends Thread {
    private int port;
    private String ip;

    public Listening(int port) {
        this.port = port;
        this.ip ="0.0.0.0";
    }



    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port,0, InetAddress.getByName(ip));
           
            while (!serverSocket.isClosed()) {
                Server.print("wait to connection");
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
                Server.print("connection successfully");

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
