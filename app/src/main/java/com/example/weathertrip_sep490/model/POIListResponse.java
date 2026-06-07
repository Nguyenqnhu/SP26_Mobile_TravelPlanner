package com.example.weathertrip_sep490.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class POIListResponse {
    @SerializedName(value = "data", alternate = {"result", "value", "items"})
    private List<POI> list;

    public List<POI> getList() {
        return list != null ? list : Collections.emptyList();
    }

    public static class Deserializer implements JsonDeserializer<List<POI>> {
        private final Gson gson = new Gson();

        @Override
        public List<POI> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null || json.isJsonNull()) return Collections.emptyList();
            if (json.isJsonArray()) {
                List<POI> result = new ArrayList<>();
                for (JsonElement e : json.getAsJsonArray()) {
                    result.add(gson.fromJson(e, POI.class));
                }
                return result;
            }
            if (json.isJsonObject()) {
                POIListResponse w = gson.fromJson(json, POIListResponse.class);
                return w.getList();
            }
            return Collections.emptyList();
        }
    }
}
