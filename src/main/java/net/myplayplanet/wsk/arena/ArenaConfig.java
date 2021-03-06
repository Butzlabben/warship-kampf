package net.myplayplanet.wsk.arena;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.Getter;
import lombok.Setter;
import net.myplayplanet.wsk.objects.TeamProperties;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ArenaConfig {

    private String name = "";
    private String world = "";
    private Map<String, Object> spawnMap = new Location(null, 0, 0, 0).serialize();
    private Map<String, Object> spectatorSpawnMap = new Location(null, 0, 0, 0).serialize();

    private int waterHeight = 50;

    private List<TeamProperties> teams = Arrays.asList(
            new TeamProperties("Team1", "\u00A7c", new Location(null, 0, 0, 0).serialize(),
                    new Vector(0, 0, 0), new Vector(0, 0, 0), new Vector(0, 0, 0), null),
            new TeamProperties("Team2", "\u00A79", new Location(null, 0, 0, 0).serialize(),
                    new Vector(0, 0, 0), new Vector(0, 0, 0), new Vector(0, 0, 0), null));

    private Vector pos1 = new Vector();
    private Vector pos2 = new Vector();

    private transient World loadedWorld;

    public void save(String file) {
        Preconditions.checkNotNull(file);
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (OutputStream out = new FileOutputStream(file);
             JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
            writer.setIndent("  ");
            gson.toJson(this, ArenaConfig.class, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArenaConfig loadFromFile(File file) {
        Preconditions.checkArgument(file.isFile(), file.getAbsolutePath() + " is not a file");
        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (InputStream in = new FileInputStream(file);
             JsonReader reader = new JsonReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            return gson.fromJson(reader, ArenaConfig.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setSpawn(Location location) {
        location.setWorld(null);
        spawnMap = location.serialize();
    }

    public void setSpectatorSpawn(Location location) {
        location.setWorld(null);
        spectatorSpawnMap = location.serialize();
    }

    public Location getSpawn() {
        Location location = Location.deserialize(spawnMap);
        location.setWorld(loadedWorld);
        return location;
    }

    public Location getSpectatorSpawn() {
        Location location = Location.deserialize(spectatorSpawnMap);
        location.setWorld(loadedWorld);
        return location;
    }
}
