package com.eivo.roleplayai;

import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class OpenRouterClient {
    private static final String BASE = "https://openrouter.ai/api/v1";

    public static String chat(String apiKey, String model, CharacterProfile character, List<ChatMessage> messages) throws Exception {
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("temperature", 0.9);
        body.put("max_tokens", 900);
        JSONArray apiMessages = new JSONArray();
        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", "You are roleplaying as the fictional adult character described below. Every character and participant in romantic or mature scenarios must be 18 or older. Stay in character, remember the recent scene, write natural dialogue, and never claim to be a real person. Mature consensual adult themes may be discussed when permitted by the model provider and applicable rules. Character name: " + character.name + "\nCharacter persona: " + character.persona);
        apiMessages.put(system);
        int start = Math.max(0, messages.size() - 36);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            if (m.imagePath != null || m.content == null || m.content.trim().isEmpty()) continue;
            JSONObject item = new JSONObject();
            item.put("role", "user".equals(m.role) ? "user" : "assistant");
            item.put("content", m.content);
            apiMessages.put(item);
        }
        body.put("messages", apiMessages);
        JSONObject result = postJson(BASE + "/chat/completions", apiKey, body);
        JSONArray choices = result.optJSONArray("choices");
        if (choices == null || choices.length() == 0) throw new Exception("Model returned no response.");
        Object content = choices.getJSONObject(0).getJSONObject("message").opt("content");
        if (content instanceof String) return ((String) content).trim();
        if (content instanceof JSONArray) {
            StringBuilder sb = new StringBuilder();
            JSONArray arr = (JSONArray) content;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject part = arr.optJSONObject(i);
                if (part != null) {
                    String text = part.optString("text", "");
                    if (!text.isEmpty()) sb.append(text);
                }
            }
            if (sb.length() > 0) return sb.toString().trim();
        }
        throw new Exception("Unsupported response format from chat model.");
    }

    public static byte[] generateImage(String apiKey, String model, CharacterProfile character, List<ChatMessage> messages) throws Exception {
        StringBuilder context = new StringBuilder();
        int start = Math.max(0, messages.size() - 8);
        for (int i = start; i < messages.size(); i++) {
            ChatMessage m = messages.get(i);
            if (m.content == null || m.content.trim().isEmpty()) continue;
            context.append("user".equals(m.role) ? "User: " : character.name + ": ").append(m.content).append("\n");
        }
        String prompt = "Create a cinematic, realistic visual of the current fictional roleplay scene. All depicted people are clearly adults aged 21+. Character: " + character.name + ". Character description: " + character.persona + ". Recent scene:\n" + context + "\nKeep visual continuity, natural lighting, believable anatomy, no text or watermark. Follow the image model provider's safety rules.";
        JSONObject body = new JSONObject();
        body.put("model", model);
        body.put("prompt", prompt);
        body.put("n", 1);
        JSONObject result = postJson(BASE + "/images", apiKey, body);
        JSONArray data = result.optJSONArray("data");
        if (data == null || data.length() == 0) throw new Exception("Image model returned no image.");
        JSONObject first = data.getJSONObject(0);
        String b64 = first.optString("b64_json", "");
        if (!b64.isEmpty()) return Base64.decode(b64, Base64.DEFAULT);
        String dataUrl = first.optString("url", "");
        if (dataUrl.startsWith("data:image") && dataUrl.contains(",")) return Base64.decode(dataUrl.substring(dataUrl.indexOf(',') + 1), Base64.DEFAULT);
        throw new Exception("Image response did not contain base64 image data.");
    }

    private static JSONObject postJson(String endpoint, String apiKey, JSONObject body) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(endpoint).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);
        conn.setDoOutput(true);
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Title", "Roleplay AI Android");
        byte[] bytes = body.toString().getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) { os.write(bytes); }
        int code = conn.getResponseCode();
        InputStream input = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
        String response = readAll(input);
        conn.disconnect();
        if (code < 200 || code >= 300) {
            String msg = response;
            try {
                JSONObject err = new JSONObject(response);
                JSONObject e = err.optJSONObject("error");
                if (e != null) msg = e.optString("message", response);
            } catch (Exception ignored) {}
            throw new Exception("API " + code + ": " + msg);
        }
        return new JSONObject(response);
    }

    private static String readAll(InputStream input) throws Exception {
        if (input == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }
}
