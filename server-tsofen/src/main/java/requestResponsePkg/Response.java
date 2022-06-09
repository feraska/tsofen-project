package requestResponsePkg;

import org.json.JSONObject;

public class Response{
   // private JSONObject content_param = new JSONObject();
    private JSONObject main=new JSONObject();
    private JSONObject content_body = new JSONObject();
    private JSONObject content_header = new JSONObject();
    public void addHeader(String key,Object value){
        content_header.put(key,value);
    }

    public void addBody(String key,Object value){
        content_body.put(key,value);
    }

    @Override
    public String toString() {
        return main.toString();
    }
    public String print(){
        return main.get("header").toString();
    }


    // public void addParam(String key,Object value){
   //     content_param.put(key,value);
   // }

    public JSONObject getContent_body() {
        return content_body;
    }

    public JSONObject getContent_header() {
        return content_header;
    }

  //  public JSONObject getContent_param() {
   //     return content_param;
  //  }

    public JSONObject getMain() {
        return main;
    }

    public void addContent() {
        main.put("body",content_body);
        main.put("header",content_header);
       // main.put("param",content_param);
    }

}
