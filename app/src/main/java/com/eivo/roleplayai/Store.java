package com.eivo.roleplayai;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Store {
    private static final String PREFS = "roleplay_store";
    private static final String KEY_ADULT = "adult_confirmed";
    private static final String KEY_CHARS = "characters";
    private static final String KEY_CHAT_MODEL = "chat_model";
    private static final String KEY_IMAGE_MODEL = "image_model";
    private final SharedPreferences prefs;

    public Store(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        ensureSeedCharacter();
    }

    public boolean isAdultConfirmed() { return prefs.getBoolean(KEY_ADULT, false); }
    public void setAdultConfirmed(boolean confirmed) { prefs.edit().putBoolean(KEY_ADULT, confirmed).apply(); }

    public String getChatModel() { return prefs.getString(KEY_CHAT_MODEL, "openrouter/free"); }
    public void setChatModel(String model) { prefs.edit().putString(KEY_CHAT_MODEL, model.trim()).apply(); }

    public String getImageModel() { return prefs.getString(KEY_IMAGE_MODEL, "google/gemini-2.5-flash-image"); }
    public void setImageModel(String model) { prefs.edit().putString(KEY_IMAGE_MODEL, model.trim()).apply(); }

    public List<CharacterProfile> getCharacters() {
        List<CharacterProfile> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(KEY_CHARS, "[]"));
            for (int i = 0; i < arr.length(); i++) out.add(CharacterProfile.fromJson(arr.getJSONObject(i)));
        } catch (Exception ignored) {}
        return out;
    }

    public void addCharacter(String name, String persona, String greeting) {
        List<CharacterProfile> chars = getCharacters();
        chars.add(new CharacterProfile(UUID.randomUUID().toString(), name.trim(), persona.trim(), greeting.trim()));
        saveCharacters(chars);
    }

    public void deleteCharacter(String id) {
        List<CharacterProfile> chars = getCharacters();
        List<CharacterProfile> kept = new ArrayList<>();
        for (CharacterProfile c : chars) if (!c.id.equals(id)) kept.add(c);
        saveCharacters(kept);
        prefs.edit().remove(chatKey(id)).apply();
        ensureSeedCharacter();
    }

    private void saveCharacters(List<CharacterProfile> chars) {
        JSONArray arr = new JSONArray();
        try {
            for (CharacterProfile c : chars) arr.put(c.toJson());
            prefs.edit().putString(KEY_CHARS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public List<ChatMessage> getMessages(CharacterProfile character) {
        List<ChatMessage> out = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(prefs.getString(chatKey(character.id), "[]"));
            for (int i = 0; i < arr.length(); i++) out.add(ChatMessage.fromJson(arr.getJSONObject(i)));
        } catch (Exception ignored) {}
        if (out.isEmpty() && character.greeting != null && !character.greeting.trim().isEmpty()) {
            out.add(ChatMessage.text("assistant", character.greeting));
            saveMessages(character.id, out);
        }
        return out;
    }

    public void saveMessages(String characterId, List<ChatMessage> messages) {
        JSONArray arr = new JSONArray();
        try {
            for (ChatMessage m : messages) arr.put(m.toJson());
            prefs.edit().putString(chatKey(characterId), arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    public void clearMessages(CharacterProfile c) {
        prefs.edit().remove(chatKey(c.id)).apply();
    }

    private String chatKey(String id) { return "chat_" + id; }

    private void ensureSeedCharacter() {
        if (!getCharacters().isEmpty()) return;
        JSONArray arr = new JSONArray();
        try {
            CharacterProfile seed = new CharacterProfile(
                    UUID.randomUUID().toString(),
                    "Luna",
                    "A fictional 25-year-old adult woman. Confident, witty, warm, playful, emotionally perceptive. She speaks naturally and stays consistent with the scene. All romantic or mature interactions are between consenting adults.",
                    "You made it. I was wondering when you'd show up. So... what kind of night are we having?"
            );
            arr.put(seed.toJson());
            prefs.edit().putString(KEY_CHARS, arr.toString()).apply();
        } catch (Exception ignored) {}
    }
}
