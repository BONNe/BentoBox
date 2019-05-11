package world.bentobox.bentobox.database.json.adapters;

import static org.bukkit.configuration.serialization.ConfigurationSerialization.SERIALIZED_TYPE_KEY;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * Handles {@link ConfigurationSerializable} types
 * @author tastybento
 * @since 1.5.0
 */

public class BukkitObjectTypeAdapter extends TypeAdapter<ConfigurationSerializable> {

    @SuppressWarnings("rawtypes")
    private final TypeAdapter<Map> map;

    @SuppressWarnings("rawtypes")
    public BukkitObjectTypeAdapter(TypeAdapter<Map> mapAdapter) {
        this.map = mapAdapter;
    }

    public static Map<String, Object> serializeObject(ConfigurationSerializable serializable) {
        Map<String, Object> serialized = new HashMap<>();
        serialized.putAll(serializable.serialize());
        serialized.entrySet().stream()
        .filter(e -> e.getValue() instanceof ConfigurationSerializable)
        .forEach(e -> serialized.put(e.getKey(), serializeObject((ConfigurationSerializable) e.getValue())));
        serialized.put(SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(serializable.getClass()));
        return serialized;
    }

    @SuppressWarnings("unchecked")
    public ConfigurationSerializable deserializeObject(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        // Iteratively expand maps until the Bukkit ConfigurationSerialization can deserialize the base object
        map.forEach((k,v) -> {
            if (v instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>)v;
                if (nestedMap.containsKey(SERIALIZED_TYPE_KEY)) {
                    map.put(k, deserializeObject(nestedMap));
                }
            }
        });

        return ConfigurationSerialization.deserializeObject(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConfigurationSerializable read(JsonReader in) throws IOException {
        return deserializeObject(map.read(in));
    }

    @Override
    public void write(JsonWriter out, ConfigurationSerializable value) throws IOException {
        if (value == null) {
            out.nullValue();
        }
        map.write(out, serializeObject(value));
    }
}