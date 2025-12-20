package genki.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import org.bson.types.ObjectId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GsonUtility {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                // Handle ObjectId serialization
                .registerTypeAdapter(ObjectId.class,
                        (JsonSerializer<ObjectId>) (src, typeOfSrc, context) ->
                                context.serialize(src.toString()))
                .registerTypeAdapter(ObjectId.class,
                        (JsonDeserializer<ObjectId>) (json, typeOfT, context) ->
                                new ObjectId(json.getAsString()))
                // Handle LocalDateTime serialization
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                                context.serialize(src.format(formatter)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                                LocalDateTime.parse(json.getAsString(), formatter))
                .create();
    }

    public static Gson getGson() {
        return gson;
    }
}
