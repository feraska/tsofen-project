package requestResponsePkg;

import org.json.JSONObject;

public class Request  {
    private JSONObject content_param = new JSONObject();//param  object json
    private JSONObject main=new JSONObject();// request object json
    private JSONObject content_body = new JSONObject();//body object json
    private JSONObject content_header = new JSONObject();//header object json
    /**
     * add header request
     * @param key
     * @param value
     * */
    public void addHeader(String key,Object value){
        content_header.put(key,value);
    }
    /**
     * add body request
     * @param key
     * @param value
     * */

    public void addBody(String key,Object value){
        content_body.put(key,value);
    }

    /**
     * request string
     * @return request string
     * */
    @Override
    public String toString() {
        return main.toString();
    }
    /**
     * add parameter request
     * @param key
     * @param value
     * */
    public void addParam(String key,Object value){
        content_param.put(key,value);
    }

    public JSONObject getContent_body() {
        return content_body;
    }

    public JSONObject getContent_header() {
        return content_header;
    }

    public JSONObject getContent_param() {
        return content_param;
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
        main.put("param",content_param);
    }
    public String print(){
        return main.get("header").toString();
    }
}
