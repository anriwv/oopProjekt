package com.tlc.util;

import com.google.gson.*;
import com.tlc.model.TierItem;
import com.tlc.model.TextTierItem;
import com.tlc.model.ImageTierItem;
import com.tlc.repository.TierListRepository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PersistenceUtil {
    private static final String FILE_PATH = "tierlists.json";
    
    private static final Gson gson;
    
    static {
        //  RuntimeTypeAdapterFactory Google abimeetod TierItem jaoks:
        RuntimeTypeAdapterFactory<TierItem> tierItemAdapter =
            RuntimeTypeAdapterFactory.of(TierItem.class, "type")
                .registerSubtype(TextTierItem.class, "text")
                .registerSubtype(ImageTierItem.class, "image");
        
        gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                @Override
                public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            })
            .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                @Override
                public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {

                    if (json.isJsonPrimitive()) {
                        return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    }

                    else if (json.isJsonObject()) {
                        JsonObject obj = json.getAsJsonObject();

                        JsonObject dateObj = obj.getAsJsonObject("date");
                        JsonObject timeObj = obj.getAsJsonObject("time");
                        if (dateObj == null || timeObj == null) {
                            throw new JsonParseException("Invalid legacy format for LocalDateTime");
                        }
                        int year = dateObj.get("year").getAsInt();
                        int month = dateObj.get("month").getAsInt();
                        int day = dateObj.get("day").getAsInt();
                        int hour = timeObj.get("hour").getAsInt();

                        int minute = timeObj.has("minute") ? timeObj.get("minute").getAsInt() : 0;
                        int second = timeObj.has("second") ? timeObj.get("second").getAsInt() : 0;
                        return LocalDateTime.of(year, month, day, hour, minute, second);
                    }
                    else {
                        throw new JsonParseException("Unexpected JSON type for LocalDateTime: " + json);
                    }
                }
            })
            .registerTypeAdapterFactory(tierItemAdapter)
            .setPrettyPrinting()
            .create();
    }

    // repo -> disk
    public static void saveRepository(TierListRepository repository) {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            gson.toJson(repository, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // disk -> repo (v uus)
    public static TierListRepository loadRepository() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                TierListRepository repo = gson.fromJson(reader, TierListRepository.class);
                if (repo != null) {
                    return repo;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new TierListRepository();
    }
}
