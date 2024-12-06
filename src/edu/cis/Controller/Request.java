package edu.cis.Controller;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Request {
    private String command;
    private Map<String, String> params;

    public Request(String command) {
        this.command = command;
        this.params = new HashMap<>();
    }

    public static String decode(String str) {
        try {
            return URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return str;
        }
    }

    public static String encode(String str) {
        try {
            return URLEncoder.encode(str, StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            return str;
        }
    }

    public static Request fromUrl(String url) {
        // Parse URL and create Request object
        // This is a simplified version - actual implementation would need more parsing
        // logic
        String[] parts = url.split("\\?");
        Request request = new Request(parts[0]);

        if (parts.length > 1) {
            String[] parameters = parts[1].split("&");
            for (String param : parameters) {
                String[] keyValue = param.split("=");
                if (keyValue.length == 2) {
                    request.params.put(keyValue[0], decode(keyValue[1]));
                }
            }
        }
        return request;
    }

    public String toGetRequest() {
        StringBuilder sb = new StringBuilder(command + "?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(entry.getKey())
                    .append("=")
                    .append(encode(entry.getValue()))
                    .append("&");
        }
        return sb.substring(0, sb.length() - 1);
    }

    // Getters and Setters
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}