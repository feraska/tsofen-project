package serverpkg;

import mainpkg.Server;
import org.json.JSONObject;
import requestResponsePkg.Response;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

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

    private Response response;


    public ClientHandler(Socket socket) {
        this.socket = socket;
        try {

            this.dataOutputStream = new PrintWriter(socket.getOutputStream(),true);
           this.dataInputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        }



    @Override
    public void run() {
        while (!socket.isClosed()) {
            processCommand();
        }
    }
    public void requestNotFount(){
        response.addHeader("response-code",NOTFOUNDREQUEST);
        response.addContent();
        dataOutputStream.println(response);
    }
    public void prepareData(String uniqueId,String responseCode,String action,String fileName,String contentType,String contentFile){
        response.addHeader("request-id",uniqueId);

        response.addHeader("action",action);
        response.addHeader("file-name",fileName);
        response.addHeader("response-code",responseCode);
        response.addHeader("content-type",contentType);
        response.addBody("content-file",contentFile);
        response.addContent();
    }
    public void processCommand(){
        try {

            response = new Response();
            String line =dataInputStream.readLine();
            System.out.println(line);
            request = new JSONObject(line);

            param = request.getJSONObject("param");
            String action = param.getString("action");
            String fileName = param.getString("file-name");
            body = request.getJSONObject("body");
            header = request.getJSONObject("header");


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
                stringBuilder.append(f).append(" ");
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
            dataOutputStream.println(response);
        }
    }

}
