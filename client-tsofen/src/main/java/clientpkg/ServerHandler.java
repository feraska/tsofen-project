package clientpkg;

import mainpkg.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import requestResponsePkg.Request;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Scanner;
import java.util.UUID;

public class ServerHandler extends Thread {
    private Socket socket;
   // private DataOutputStream dataOutputStream;
    private PrintWriter dataOutputStream;
    private BufferedReader dataInputStream;
    private Request request;
    private String action;


    public ServerHandler(Socket socket) {
        this.socket = socket;
        try {
            dataOutputStream = new PrintWriter(socket.getOutputStream(),true);
            dataInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {

        }
    }

    @Override
    public void run() {
        prepareRequest();
    }

    public void prepareRequest(){

        while (!socket.isClosed()) {
            request = new Request();
            String[] options={"1.upload","2.download","3.delete","4.getListByDir"};
            for (String op:options){
                Client.print(op);
            }
            String reqName = Client.input();
            switch (reqName){
                case "1":
                    action = "upload";
                    upload();
                    break;
                case "2":
                    action = "download";
                    download();
                    break;
                case "3":
                    action = "delete";
                    delete();
                    break;
                case "4":
                    action = "get";
                    getListByDir();
                    break;
                default:
                    action=reqName;
                    prepareData(action,"","");
                    dataOutputStream.println(request);
                    error();
                    break;

            }
        }

    }
    public void error(){
        try {

            System.out.println(dataInputStream.readLine());
        }
        catch (Exception e){

        }
    }
    public void prepareData(String action,String fileName,String contentType,String contentFile){
        String uniqueId = UUID.randomUUID().toString();
        request.addParam("action",action);
        request.addParam("file-name",fileName);
        request.addParam("content-type",contentType);
        request.addHeader("request-id",uniqueId);
       // JSONArray bytes = new JSONArray();
       // bytes.put(contentFile);
        request.addBody("content-file",contentFile);
        request.addContent();

    }
    public void prepareData(String action,String fileName,String contentType){
        String uniqueId = UUID.randomUUID().toString();
        request.addParam("action",action);
        request.addParam("file-name",fileName);
        request.addParam("content-type",contentType);
        request.addHeader("request-id",uniqueId);
        request.addContent();
    }


    public void upload(){

        try {
            String filePath=Client.uploadFileGui();
            if(filePath.equals("")){
                throw new RuntimeException("not file");
            }
            Client.print("enter path to save");
            String path=Client.input();
            Path of = Path.of(filePath);
            String contentType =Files.probeContentType(of);
            byte[]bytes=Files.readAllBytes(of);
            byte[]encode=Base64.getEncoder().encode(bytes);
            String byteString = new String(encode);
            prepareData(action, path,contentType,byteString);
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line.substring(0,line.indexOf("body")));


        } catch (Exception e) {
            System.out.println(e.getMessage());
            //   dataOutputStream.println("exception");
        }



    }
    public void download(){

        try {
            String path = Client.saveFileGui();
            if(path.equals("")){
                throw new RuntimeException("not file");
            }
            Client.print("enter file to download");
            String input=Client.input();
            prepareData(action,input,Files.probeContentType(Path.of(input)));
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line.substring(0,line.indexOf("body")));
            JSONObject main = new JSONObject(line);
            JSONObject body = main.getJSONObject("body");
            String data = body.getString("content-file");
            byte[]bytes=Base64.getDecoder().decode(data.getBytes());
            Files.write(Path.of(path),bytes);



        } catch (Exception e) {
              // Client.print(e.getMessage());
         //   dataOutputStream.println("exception");
        }
    }
    public void delete(){
        try {
            Client.print("enter path from sever to delete");
            String input = Client.input();

            prepareData(action,input,Files.probeContentType(Path.of(input)));
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line);

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    public void getListByDir(){
        Client.print("enter directory path");
        String input = Client.input();
        try {
            prepareData(action,input,"");
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line.substring(0,line.indexOf("body")));
            JSONObject jsonObject = new JSONObject(line);
            JSONObject body = jsonObject.getJSONObject("body");
            String lines=body.getString("content-file");
            String[]l = lines.split(" ");
            for (String s:l){
                Client.print(s);
            }
        } catch (IOException e) {

        }
    }
}
