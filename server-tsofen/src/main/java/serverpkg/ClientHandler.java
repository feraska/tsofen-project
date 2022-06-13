package serverpkg;

import mainpkg.Server;
import org.json.JSONObject;
import requestResponsePkg.Response;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    //private DataInputStream dataInputStream;
  //  private DataOutputStream dataOutputStream;
    private PrintWriter dataOutputStream;
    private BufferedReader dataInputStream;
    private JSONObject request;

    private JSONObject param;
    private JSONObject header;
    private JSONObject body;
    private static final String OK = "200";
    private static final String FILENOTFOUND = "404";
    private static final String NOTFOUNDREQUEST = "405";
    private static final String EXCEPTION = "500";
    private static final String FUNCTIONNOUTIMMPLEMMENT = "501";
    private StringBuilder files;//list of files in server
    private StringBuilder dirs;//list of directory in server

    private Response response;
    private String action;
    private String requestId;

    /***
     * constructor the role is client handler request and send response
     *  the response is json object(header,body)
     * @param socket from Listening class
     * */
    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {

            this.dataOutputStream = new PrintWriter(socket.getOutputStream(),true);
           this.dataInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        }


    /**
     * run thread
     * */
    @Override
    public void run() {
        while (!socket.isClosed()) {
            processCommand();
        }
    }
    /**
     * write message if request not found
     * */
    public void requestNotFount(){
        response.addHeader("response-code",NOTFOUNDREQUEST);
        response.addContent();
        dataOutputStream.println(response);
    }
    /***
     * server prepare response to send to client
     * @param uniqueId unique id request received from the client
     * @param responseCode appropriate response code
     * @param action the action received from the client
     * @param fileName file name
     * @param contentType type file
     * @param contentFile content bytes of file
     * */
    public void prepareData(String uniqueId,String responseCode,String action,String fileName,String contentType,String contentFile){
        response.addHeader("request-id",uniqueId);
        response.addHeader("action",action);
        response.addHeader("file-name",fileName);
        response.addHeader("response-code",responseCode);
        response.addHeader("content-type",contentType);
        response.addBody("content-file",contentFile);
        response.addContent();
    }
    /***
     * the server is processing the request received form client
     * */
    public void processCommand(){
        try {

            response = new Response();
            String line =dataInputStream.readLine();
            //System.out.println(line);
            request = new JSONObject(line);

            param = request.getJSONObject("param");
            action = param.getString("action");
            String fileName = param.getString("file-name");
            body = request.getJSONObject("body");
            header = request.getJSONObject("header");
            requestId = header.getString("request-id");

            switch (action){
                case "upload":
                    upload(fileName,body.getString("content-file"));
                    break;
                case "download":
                    download(fileName);
                    break;
                case "delete":
                    delete(fileName);
                    break;
                case "get":
                    getListByDir(fileName);
                    break;
                case "list":
                    getAllList();
                    break;
                case "dir":
                    getAllDir();
                    break;
                case "downloadDir":
                    downloadDir(fileName);
                    break;
                case "deleteDir":
                    deleteDir(fileName);
                    break;
                default:
                    requestNotFount();

            }


        } catch (Exception e) {
          //  requestNotFount();
           // e.printStackTrace();
            System.out.println(e.getMessage());
            try {
                socket.close();
                dataOutputStream.close();
                dataInputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            //   dataOutputStream.println(response);
            //System.out.println(e.getMessage());
           // throw new RuntimeException("request not found");

        }
    }
    /**
     * give folder to download
     * @param file folder to download
     * */
    public  void getFileAndDownload(File file){
        try {

            File[] list = file.listFiles();

            if(list==null){
                throw new RuntimeException("not exists");
            }
            // System.out.println(list[2]);
            for (File value : list) {

                if (value.isDirectory()) {
                    getFileAndDownload(value);
                } else {

                    try {
                      //  synchronized (this) {
                            response = new Response();
                            String fileName = value.getPath().substring(value.getPath().indexOf("\\") + 1);
                            fileName = fileName.replace("\\", "/");
                            System.out.println(fileName);
                            byte[] bytes = Files.readAllBytes(Path.of(value.getPath()));
                            byte[] encode = Base64.getEncoder().encode(bytes);
                            String data = new String(encode);


                            prepareData(
                                    requestId,
                                    OK,
                                    action,
                                    fileName,
                                    Files.probeContentType(value.toPath()),
                                    data
                            );
                            response.addHeader("msg","no");
                            dataOutputStream.println(response.toString());

                            //   System.out.println(response.print());


                            //Log.log("WWWWW");
                            Log.log(response.print());
                            System.out.println(response.print());
                   //     }


                    } catch (Exception e) {


                    }

                }
            }
            response = new Response();
            prepareData(
                    requestId,
                    OK,
                    action,
                    "",
                    "",
                    ""
            );
            response.addHeader("msg","finish");
            dataOutputStream.println(response.toString());

        }
        catch (Exception e){
       // e.printStackTrace();
            response = new Response();
            prepareData(
                    requestId,
                    FILENOTFOUND,
                    action,
                    "",
                    "",
                    ""
            );
            response.addHeader("msg","finish");
            dataOutputStream.println(response.toString());
        }
    }

    /**
     * upload file to the server
     * @param fileName the file name
     * @param b bytes of file
     *
     * */
    public void upload(String fileName, String b){
    String fileDes=Server.fileRoot+"/"+fileName;
    File file = new File(fileDes.substring(0,fileDes.lastIndexOf("/")));
    file.mkdirs();
    byte[]decode=b.getBytes();

    byte[]bytes= Base64.getDecoder().decode(decode);

        try {
            Files.write(Path.of(fileDes),bytes);
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    body.getString("content-file")
                    );
            Log.log(response.print());
            dataOutputStream.println(response.toString());


           // System.out.println(response.getContent_body().toString());

        } catch (Exception e) {
            prepareData(
            header.getString("request-id"),
                    EXCEPTION,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    body.getString("content-file")
            );


        }

    }
    /**
     * prepare folder to download
     * @param fileName folder name
     * */
    public  void downloadDir(String fileName){
        try {
            String path = Server.fileRoot + "/" + fileName;
            File file = new File(path);

            getFileAndDownload(file);


        }
        catch (Exception e){

        }

    }
    /**
     * download file to the server
     * @param fileName the file name to download
     * */
    public void download(String fileName) {
        String path = Server.fileRoot + "/" + fileName;
        try {


            byte[] bytes = Files.readAllBytes(Path.of(path));
            byte[] encode = Base64.getEncoder().encode(bytes);
            String data = new String(encode);

            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    data
            );
            Log.log(response.print());
            dataOutputStream.println(response.toString());

        } catch (IOException e) {
            System.out.println(e.getMessage());
            prepareData(
                    header.getString("request-id"),
                    FILENOTFOUND,
                    param.getString("action"),
                    param.getString("file-name"),
                    "",
                    ""
            );

            dataOutputStream.println(response.toString());

        }
    }
    /***
     * delete file from server
     * @param fileName the file name to delete
     * */
    public void delete(String fileName){
        try {

            String path = Server.fileRoot + "/" + fileName;
            Files.delete(Path.of(path));
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    ""
            );
            Log.log(response.print());
            dataOutputStream.println(response.toString());
        }
         catch (Exception e) {
            prepareData(
                    header.getString("request-id"),
                    FILENOTFOUND,
                    param.getString("action"),
                    param.getString("file-name"),
                    "",
                    ""
            );
            dataOutputStream.println(response.toString());
        }
    }
    /**
     * delete folder from the server
     * @param dirName the folder name
     * */
    public void deleteDir(String dirName){
        try {
            response = new Response();
            String path = Server.fileRoot + "/" + dirName;
            File file = new File(path);
            getFileAndDelete(file);
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    param.getString("file-name"),
                    "",
                    ""
            );
            dataOutputStream.println(response.toString());


        }
        catch (Exception e){
            e.printStackTrace();
            response = new Response();
            prepareData(
                    header.getString("request-id"),
                    FILENOTFOUND,
                    param.getString("action"),
                    param.getString("file-name"),
                    "",
                    ""
            );
            dataOutputStream.println(response.toString());
        }
    }
    /**
     * delete all file form the folder
     * @param file the file to delete
     * */
    public void getFileAndDelete(File file) {

        try {
            File[] list = file.listFiles();
            if(list==null) {
                throw new RuntimeException("not file");
            }
            for (File value : list) {

                if (value.isDirectory()) {
                    getFileAndDelete(value);
                } else {
                    Files.delete(value.toPath());
                }

            }
            Files.delete(file.toPath());
        }
        catch (Exception e){
            response = new Response();
            prepareData(
                    header.getString("request-id"),
                    FILENOTFOUND,
                    param.getString("action"),
                    param.getString("file-name"),
                    "",
                    ""
            );
            dataOutputStream.println(response.toString());
        }
    }
    /***
     * get all file from folder
     * @param dirName the folder name
     * */
    public void getListByDir(String dirName) {
        String path = Server.fileRoot + "/" + dirName;
        try {

            File file = new File(path);
            String[] fileList = file.list();
            if (fileList == null) {
                throw new RuntimeException("directory not found");
            }
            StringBuilder stringBuilder = new StringBuilder();
            for (String f : fileList) {
                stringBuilder.append(f).append(":");
            }
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    stringBuilder.toString().trim()
            );
            Log.log(response.print());
            dataOutputStream.println(response);
        }
        catch (Exception e){
            prepareData(
                    header.getString("request-id"),
                    FILENOTFOUND,
                    param.getString("action"),
                    param.getString("file-name"),
                    param.getString("content-type"),
                    ""
            );
            Log.log(response.print());
            dataOutputStream.println(response.toString());
        }
    }
    /**
     * get all file and folder from folder root server
     * */
    public void getAllList(){
        try {
            files=new StringBuilder();
            File file = new File(Server.fileRoot);
            getFile(file);
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    "",
                    "",
                    files.toString().trim()
            );
            Log.log(response.print());
            dataOutputStream.println(response.toString());
        }
        catch (Exception e){

        }
    }
    /***
     * get all folder from the server
     * */
    public void getAllDir(){
        try {
            dirs=new StringBuilder();
            File file = new File(Server.fileRoot);
            getDir(file);
            prepareData(
                    header.getString("request-id"),
                    OK,
                    param.getString("action"),
                    "",
                    "",
                    dirs.toString().trim()
            );
            Log.log(response.print());
            dataOutputStream.println(response.toString());
        }
        catch (Exception e){

        }
    }
    /**
     * get folder by folder
     * @param dir folder to give all folder inside
     * */
    public void getDir(File dir){
        try {

            File[] list = dir.listFiles();

            assert list != null;
            for (File f : list) {
                if(f.isDirectory()){
                    dirs.append(f.getPath()).append(":");
                    getDir(f);
                }

            }
        }
        catch (Exception e){

        }
    }
    /**
     * give all files in folder
     * @param file the folder
     * */
    public void getFile(File file){
        try {

            File[] list = file.listFiles();

            assert list != null;
            for (File f : list) {
                if(f.isDirectory()){
                    getFile(f);
                }
                else{
                    int index=f.getPath().indexOf("\\");
                    files.append(f.getPath().substring(index+1)).append(":");
                }
            }
        }
        catch (Exception e){

        }
    }
}
