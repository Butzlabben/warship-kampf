package net.myplayplanet.wsk;

import lombok.Getter;
import net.myplayplanet.commandframework.CommandFramework;
import net.myplayplanet.wsk.arena.ArenaManager;
import net.myplayplanet.wsk.commands.SetupCommand;
import net.myplayplanet.wsk.commands.WSKCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WSK extends JavaPlugin {

    @Getter
    private CommandFramework framework;
    private static WSK instance;
    public static final String PREFIX = "§8[§6WSK§8] §e";

    @Override
    public void onEnable() {
        instance = this;
        framework = new CommandFramework(this);
        framework.registerCommands(new WSKCommand(this));
        framework.registerCommands(new SetupCommand(this));

        // Create arenas folder
        File file = new File(getDataFolder(), "arenas");
        file.mkdirs();

        // Load config
        Config.load();

        // Initialize ArenaManager with WSK instance
        new ArenaManager(this);
    }

    public static WSK getInstance() {
        return instance;
    }

}
