package com.eivo.roleplayai;

import org.json.JSONException;
import org.json.JSONObject;

public class ChatMessage {
    public final String role;
    public final String content;
    public final String imagePath;
    public final long timestamp;

    public ChatMessage(String role, String content, String imagePath, long timestamp) {
        this.role = role;
        this.content = content;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
    }

    public static ChatMessage text(String role, String content) {
        return new ChatMessage(role, content, null, System.currentTimeMillis());
    }

    public static ChatMessage image(String role, String imagePath) {
        return new ChatMessage(role, "", imagePath, System.currentTimeMillis());
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("role", role);
        o.put("content", content);
        if (imagePath != null) o.put("imagePath", imagePath);
        o.put("timestamp", timestamp);
        return o;
    }

    public static ChatMessage fromJson(JSONObject o) {
        String path = o.has("imagePath") ? o.optString("imagePath", null) : null;
        if (path != null && path.isEmpty()) path = null;
        return new ChatMessage(
                o.optString("role", "assistant"),
                o.optString("content", ""),
                path,
                o.optLong("timestamp", System.currentTimeMillis())
        );
    }
}
