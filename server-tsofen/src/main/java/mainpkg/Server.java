package mainpkg;

import serverpkg.Listening;
import serverpkg.Log;

import java.io.File;
import java.util.Scanner;

public class Server {
    public static final String fileRoot="file_root";//file_root server
    /**
     * write msg
     * @param msg string to write at screen
     * */
    public static void print(String msg){
        System.out.println(msg);
    }
    /***
     * read input from user
     * @return input string
     * */
    public static String input(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    /**
     * make file_root directory
     * */
    public static void mkdir() {
        File file = new File(fileRoot);
        if (!file.exists()) {
            file.mkdir();
        }
    }
    public static void main(String[] args) {
        mkdir();
        print("enter port");
        int port = Integer.parseInt(input());

        Listening listening = new Listening(port);
        listening.start();
    }
}
