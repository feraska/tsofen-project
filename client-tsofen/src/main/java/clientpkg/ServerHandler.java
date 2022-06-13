package clientpkg;

import mainpkg.Client;
import org.json.JSONArray;
import org.json.JSONObject;
import requestResponsePkg.Request;


import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ServerHandler extends Thread {

    private Socket socket;
    private PrintWriter dataOutputStream;
    private BufferedReader dataInputStream;
    private Request request;
    private String action;
    private final Object object =new Object();

    /**
     * constructor send request from client to server
     * the request is json object(param,header,body)
     * @param socket give the socket from Connection class
     * */
    public ServerHandler(Socket socket) {
        this.socket = socket;
        try {
            dataOutputStream = new PrintWriter(socket.getOutputStream(),true);
            dataInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {

        }
    }

    /**
     * run thread
     * */
    @Override
    public void run() {
        prepareRequest();
    }
    /**
     * the client choose the request and send to the server
     * */
    public void prepareRequest(){

        while (!socket.isClosed()) {
           

            String[] options={"1.upload file","2.download file","3.delete file","4.get List By Directory","5.get Files","6.get Directories","7.upload folder","8.download folder","9.delete folder"};
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
                case "5":
                    action="list";
                    getAllList();
                    break;
                case "6":
                    action="dir";
                    getAllList();
                    break;
                case "7":
                    action="upload";
                    uploadFolder();
                    break;
                case "8":
                    action="downloadDir";
                    downloadFolder();
                    break;
                case "9":
                    action="deleteDir";
                    deleteDir();
                    break;
                default:
                    action=reqName;
                    prepareData(action,"","");
                    dataOutputStream.println(request);
                    error();
                    break;

            }
        }
        try {
            socket.close();
            System.exit(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
    /**
     * if the client received error message
     * */
    public void error(){
        try {

            System.out.println(dataInputStream.readLine());
        }
        catch (Exception e){

        }
    }
    /**
     * @param action action request
     * @param fileName file name
     * @param contentType type of file
     * @param contentFile bytes of file
     * */
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
    /**
     * @param action action request
     * @param fileName file name
     * @param contentType type of file
     * */
    public void prepareData(String action,String fileName,String contentType){
        String uniqueId = UUID.randomUUID().toString();
        request.addParam("action",action);
        request.addParam("file-name",fileName);
        request.addParam("content-type",contentType);
        request.addHeader("request-id",uniqueId);
        request.addContent();
    }
    /**
     * prepare file to upload from folder
     * @param path the folder to store in server
     * @param file the file to upload
     * */
    public void getFile(String path,File file){
        try {

            File[] list = file.listFiles();

            assert list != null;
            for (File f : list) {
               // System.out.println(f);
                if(f.isDirectory()){
                    getFile(path,f);
                }
                else{
                    uploadByFile(f.getAbsolutePath(),path);
                }
            }
        }
        catch (Exception e){

        }
    }
    /**
     * upload file to the server
     * @param fileAbsolutePath file path from your computer to upload to the server
     * @param dirP the folder to store in the server
     * */
    public void uploadByFile(String fileAbsolutePath,String dirP) {
        try {
            request = new Request();
            File file = new File(fileAbsolutePath);
            String filePath=file.getParent();
            filePath=filePath.substring(filePath.lastIndexOf("\\")+1);
            filePath=filePath+"/"+file.getName();
            if(dirP.equals("")){
                dirP=filePath;
            }
            else {
                dirP=dirP+"/"+filePath;
            }

            Path of = Path.of(fileAbsolutePath);
            String contentType = Files.probeContentType(of);
            if(contentType==null){
                contentType="not file";
            }
            byte[] bytes = Files.readAllBytes(of);
            byte[] encode = Base64.getEncoder().encode(bytes);
            String byteString = new String(encode);
            prepareData(action, dirP, contentType, byteString);
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line.substring(0, line.indexOf("body")));
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
     * upload folder to the server
     * */

    public void uploadFolder(){
        try {

            String pathDir = Client.uploadFileGuiFolder();
            if(pathDir.equals("")){
                throw new RuntimeException("not folder");
            }
            Client.print("enter path to save");
            String path = Client.input();
            if(path.equals("root")){
                path="";
            }
            File file = new File(pathDir);
            getFile(path,file);
        }
        catch (Exception e){
            Client.print(e.getMessage());
        }
    }

    /**
     * upload file form your computer or multiple files to the server
     * */
    public void upload(){

        try {
            File[]files=Client.uploadFileGui();
            Client.print("enter path to save");
            String path = Client.input();
            String fileP = path;
            if (files==null) {
                throw new RuntimeException("not file");
            }
            for (File f:files) {
                String filePath = f.getAbsolutePath();
                File file = new File(filePath);

                if (fileP.equals("root")) {
                    fileP = file.getName();
                } else {
                    fileP = path + "/" + file.getName();
                }
                request = new Request();
                Path of = Path.of(filePath);
                String contentType = Files.probeContentType(of);
                if(contentType==null){
                    contentType="not file";
                }
                byte[] bytes = Files.readAllBytes(of);
                byte[] encode = Base64.getEncoder().encode(bytes);
                String byteString = new String(encode);
                prepareData(action, fileP, contentType, byteString);
                dataOutputStream.println(request.toString());
                String line = dataInputStream.readLine();
                Client.print(line.substring(0, line.indexOf("body")));
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
            //   dataOutputStream.println("exception");
        }



    }
    /**
     * download file from sever to your computer
     * */
    public void download(){

        try {
            Client.print("enter file to download");
            String input=Client.input();
            String i = input;
            if(i.contains("/")){
                i=i.substring(i.lastIndexOf("/")+1);
            }
            request = new Request();
            Path of = Path.of(input);
            String contentType = Files.probeContentType(of);
            if(contentType==null){
                contentType="not file";
            }

            prepareData(action,input,contentType);
            String path = Client.saveFileGui(i);
            if(path.equals("")){
                throw new RuntimeException("not file");
            }
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
    /**
     * download folder from server to your computer
     * */
    public void downloadFolder(){
        try {

            Client.print("enter folder to download");
            String input = Client.input();
            String path = Client.saveFileGui(input);
            if(path.equals("")){
                throw new RuntimeException("not file");
            }
            path=path.replace("\\","/");
            request = new Request();
            Path off = Path.of(input);
            String contentType = Files.probeContentType(off);
            if(contentType==null){
                contentType="not file";
            }
            prepareData(action, input,contentType);
            dataOutputStream.println(request.toString());

            while (true) {
               // synchronized (object) {
                    String line = dataInputStream.readLine();
                    JSONObject jsonObject = new JSONObject(line);
                    JSONObject header = jsonObject.getJSONObject("header");
                    JSONObject body = jsonObject.getJSONObject("body");
                    String filePath = header.getString("file-name");
                    String msg = header.getString("msg");
                    System.out.println(header);
                    if (msg.equals("finish")) {
                    break;
                     }
                    System.out.println(filePath);
                    String data = body.getString("content-file");
                    byte[] bytes = Base64.getDecoder().decode(data.getBytes());
                    String dirs = filePath.substring( 0,filePath.lastIndexOf("/"));
                    File file = new File( path+"/"+dirs);
                    file.mkdirs();
                    Path of = Path.of( path+"/"+filePath);
                    System.out.println(path);
                    System.out.println(filePath);
                    Files.write(of, bytes);

              //  }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }


    }
    /**
     * delete file or multiple files from server
     * */
    public void delete(){
        try {
            Client.print("enter path from sever to delete");
            List<String> list = new LinkedList<>();
            String input="";
            do {
                 input = Client.input();
                 if(!input.equals("")) {
                     list.add(input);
                 }
            }
            while (!input.equals(""));
            for (String l:list) {
                request = new Request();
                Path off = Path.of(l);
                String contentType = Files.probeContentType(off);
                if(contentType==null){
                    contentType="not file";
                }
                prepareData(action, l, contentType);
                dataOutputStream.println(request.toString());
                String line = dataInputStream.readLine();
                Client.print(line);
            }

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }
    /**
     * prepare folder to delete from server
     * */
    public void deleteDir(){
        try {
            Client.print("enter directory from sever to delete");
            String dir = Client.input();
            request = new Request();
            Path off = Path.of(dir);
            String contentType = Files.probeContentType(off);
            if(contentType==null){
                contentType="not file";
            }
            prepareData(action, dir, contentType);
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line);
        }
        catch (Exception e){

        }
    }
    /***
     * receive all file from folder is on server
     * */
    public void getListByDir(){
        Client.print("enter directory path");
        String input = Client.input();
        try {
            request = new Request();
            prepareData(action,input,"");
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            Client.print(line.substring(0,line.indexOf("body")));
            JSONObject jsonObject = new JSONObject(line);
            JSONObject body = jsonObject.getJSONObject("body");
            String lines=body.getString("content-file");
            String[]l = lines.split(":");
            for (String s:l){
                Client.print(s);
            }
        } catch (IOException e) {

        }
    }
    /***
     * receive all file and folder from server
     * */
    public void getAllList(){
        try {
            request = new Request();
            prepareData(action, "", "");
            dataOutputStream.println(request.toString());
            String line = dataInputStream.readLine();
            JSONObject jsonObject = new JSONObject(line);
            JSONObject body = jsonObject.getJSONObject("body");
            String lines = body.getString("content-file");
            String[] l = lines.split(":");
            for (String s : l) {
                Client.print(s);
            }
        }
        catch (Exception e){

        }
    }
}
