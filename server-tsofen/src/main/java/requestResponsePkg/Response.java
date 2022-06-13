package requestResponsePkg;

import org.json.JSONObject;

public class Response{
    private JSONObject main=new JSONObject();// main object(response)
    private JSONObject content_body = new JSONObject();//body
    private JSONObject content_header = new JSONObject();//header
    /**
     * add header to header json object
     * @param key string key
     * @param value object value
     * */
    public void addHeader(String key,Object value){
        content_header.put(key,value);
    }
    /**
     * add body to body json object
     * @param key string key
     * @param value object value
     * */

    public void addBody(String key,Object value){
        content_body.put(key,value);
    }
    /**
     * give response string
     * @return response string
     * */

    @Override
    public String toString() {
        return main.toString();
    }
    public String print(){
        return main.get("header").toString();
    }

    public JSONObject getContent_body() {
        return content_body;
    }

    public JSONObject getContent_header() {
        return content_header;
    }


    public JSONObject getMain() {
        return main;
    }
    /**
     * add body and header to the response
     *
     * */
    public void addContent() {
        main.put("body",content_body);
        main.put("header",content_header);
    }

}
