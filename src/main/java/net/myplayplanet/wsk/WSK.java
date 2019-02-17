package net.myplayplanet.wsk;

import lombok.Getter;
import net.myplayplanet.commandframework.CommandFramework;
import net.myplayplanet.wsk.arena.ArenaManager;
import net.myplayplanet.wsk.commands.ArenaCommand;
import net.myplayplanet.wsk.commands.SetupCommand;
import net.myplayplanet.wsk.commands.TeamCommand;
import net.myplayplanet.wsk.commands.WSKCommand;
import net.myplayplanet.wsk.listener.ArenaListener;
import net.myplayplanet.wsk.listener.PlayerListener;
import net.myplayplanet.wsk.objects.ScoreboardManager;
import net.myplayplanet.wsk.objects.WSKPlayer;
import net.myplayplanet.wsk.util.Logger;
import net.myplayplanet.wsk.util.RegionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.concurrent.ForkJoinPool;

public class WSK extends JavaPlugin {

    @Getter
    private CommandFramework framework;
    private static WSK instance;
    @Getter
    private static boolean fawe;
    @Getter
    private ArenaManager arenaManager;
    private RegionUtil regionUtil;
    public static final String PREFIX = "§8[§6WSK§8] §e";

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        Logger.BOOT.log("Loading config...");
        Config.load();

        fawe = Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") != null;

        Logger.BOOT.log("Registering commands");
        framework = new CommandFramework(this);
        framework.registerCommands(new WSKCommand(this));
        framework.registerCommands(new SetupCommand(this));
        if (!Config.isSetup()) {
            framework.registerCommands(new TeamCommand(this));
            framework.registerCommands(new ArenaCommand(this));
        }

        // Create arenas folder
        File file = new File(getDataFolder(), "arenas");
        file.mkdirs();

        // Register listeners
        PluginManager pm = Bukkit.getPluginManager();
        if (!Config.isSetup()) {
            pm.registerEvents(new PlayerListener(this), this);
            pm.registerEvents(new ArenaListener(this), this);
        }

        // Initialize ArenaManager with WSK instance
        Logger.BOOT.log("Initialize arena...");
        arenaManager = new ArenaManager(this);

        if(!Config.isSetup())
            regionUtil = new RegionUtil(this);

        // Init Scoreboard to get all new teams
        ScoreboardManager.getInstance().init(this);

        for (Player player : Bukkit.getOnlinePlayers()) {
            WSKPlayer.add(player);
            ScoreboardManager.getInstance().getGuestTeam().addPlayer(player);
        }

        Logger.BOOT.log("Async threads will be executed with a parallelism of: " + ForkJoinPool.getCommonPoolParallelism());
        Logger.NORMAL.log("WSK v" + getDescription().getVersion() + " by Butzlabben was successfully enabled");
    }

    @Override
    public void onDisable() {
        if(arenaManager.getCurrentArena() != null)
        arenaManager.getCurrentArena().getGameWorld().unloadCompletly();
    }
}
