package net.myplayplanet.wsk.listener;

import lombok.AllArgsConstructor;
import net.myplayplanet.wsk.WSK;
import net.myplayplanet.wsk.arena.Arena;
import net.myplayplanet.wsk.objects.ScoreboardManager;
import net.myplayplanet.wsk.objects.WSKPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@AllArgsConstructor
public class PlayerListener implements Listener {

    private final WSK wsk;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.setScoreboard(ScoreboardManager.getInstance().getScoreboard());
        player.getInventory().clear();

        WSKPlayer.handle(event);
        ScoreboardManager.getInstance().handleJoinEvent(event);

        player.setDisplayName("§7" + player.getName() + "§r");

        Arena arena = wsk.getArenaManager().getCurrentArena();
        if (!arena.getState().isInGame())
            player.teleport(wsk.getArenaManager().getCurrentArena().getArenaConfig().getSpawn());
        else
            player.teleport(wsk.getArenaManager().getCurrentArena().getArenaConfig().getSpectatorSpawn());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ScoreboardManager.getInstance().handleQuitEvent(event);

        WSKPlayer player = WSKPlayer.getPlayer(event.getPlayer());
        if (player.getTeam() != null)
            player.getTeam().removeMember(player);
    }
}
