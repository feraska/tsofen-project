package mainpkg;

import clientpkg.Connection;
import org.json.JSONObject;
import requestResponsePkg.Request;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Scanner;
public class Client {
    public static void print(String msg){
        System.out.println(msg);
    }
    public static String input(){
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    public static String uploadFileGui(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());

        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = jfc.getSelectedFile();

            return selectedFile.getAbsolutePath();
        }
        return "";
    }
    public static String saveFileGui() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            return fileToSave.getAbsolutePath();
        }
        return "";
    }
    public static void main(String[] args) {
        try {
            print("enter ip");
            String ip = input();
            print("enter port");
            int port = Integer.parseInt(input());
            Connection connection = new Connection(ip,port);
            connection.start();
        }
        catch (Exception e){
            print(e.getMessage());
        }

    }
}
