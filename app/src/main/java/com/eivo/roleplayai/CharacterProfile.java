package com.eivo.roleplayai;

import org.json.JSONException;
import org.json.JSONObject;

public class CharacterProfile {
    public final String id;
    public final String name;
    public final String persona;
    public final String greeting;

    public CharacterProfile(String id, String name, String persona, String greeting) {
        this.id = id;
        this.name = name;
        this.persona = persona;
        this.greeting = greeting;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject o = new JSONObject();
        o.put("id", id);
        o.put("name", name);
        o.put("persona", persona);
        o.put("greeting", greeting);
        return o;
    }

    public static CharacterProfile fromJson(JSONObject o) {
        return new CharacterProfile(
                o.optString("id"),
                o.optString("name", "Character"),
                o.optString("persona", ""),
                o.optString("greeting", "Hello.")
        );
    }
}
