package org.kendar.servers.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SerializableResponse {
    byte[] responseBytes;
    String responseText;
    HashMap<String, String> headers;
    int statusCode;
    boolean binaryResponse;

    private List<String> messages = new ArrayList<>();


    public byte[] getResponseBytes() {
        return responseBytes;
    }

    public void setResponseBytes(byte[] responseBytes) {
        this.responseBytes = responseBytes;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isBinaryResponse() {
        return binaryResponse;
    }

    public void setBinaryResponse(boolean binaryResponse) {
        this.binaryResponse = binaryResponse;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getHeader(String s) {
        return RequestUtils.getFromMap(this.headers,s);
    }
}