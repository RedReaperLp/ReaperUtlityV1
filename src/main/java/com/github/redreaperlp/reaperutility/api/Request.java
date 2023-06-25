package com.github.redreaperlp.reaperutility.api;

import org.json.JSONObject;

public class Request {

    JSONObject request;

    public Request(String request) {
        this.request = new JSONObject(request);
    }

    public ERequest request() {
        return ERequest.get(request.getString("request"));
    }

    public JSONObject data() {
        return request.getJSONObject("data");
    }
}
