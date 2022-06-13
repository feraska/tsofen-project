package mainpkg;

import clientpkg.Connection;
import org.json.JSONObject;
import requestResponsePkg.Request;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.util.Scanner;
public class Client {
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
     * upload file from your computer
     * @return files selection store in array file
     * */
    public static File[] uploadFileGui(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setMultiSelectionEnabled(true);
        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {

            return jfc.getSelectedFiles();
        }
        return null;
    }
    /**
     * upload file from folder from your computer
     * @return path folder selection
     * */
    public static String uploadFileGuiFolder(){
        JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
        jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = jfc.showOpenDialog(null);
        // int returnValue = jfc.showSaveDialog(null);

        if (returnValue == JFileChooser.APPROVE_OPTION) {

            return jfc.getSelectedFile().getAbsolutePath();
        }
        return "";
    }
    /**
     * save file in your computer
     * @return save location from your computer
     * */
    public static String saveFileGui(String name) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Specify a file to save");
        fileChooser.setSelectedFile(new File(name));
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
            System.exit(0);
        }

    }
}
