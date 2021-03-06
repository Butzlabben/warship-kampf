package net.myplayplanet.wsk.listener;

import lombok.AllArgsConstructor;
import net.myplayplanet.wsk.Config;
import net.myplayplanet.wsk.WSK;
import net.myplayplanet.wsk.arena.Arena;
import net.myplayplanet.wsk.arena.ArenaState;
import net.myplayplanet.wsk.event.PlayerEnterEvent;
import net.myplayplanet.wsk.event.TeamMemberDieEvent;
import net.myplayplanet.wsk.objects.Team;
import net.myplayplanet.wsk.objects.WSKPlayer;
import net.myplayplanet.wsk.util.RegionUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public class PlayerListener implements Listener {

    private final WSK wsk;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Arena arena = wsk.getArenaManager().getCurrentArena();

        player.setScoreboard(arena.getScoreboardManager().getScoreboard());
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20);
        player.setFoodLevel(20);

        WSKPlayer.handle(event);
        arena.getScoreboardManager().handleJoinEvent(event);

        player.setDisplayName("§7" + player.getName() + "§r");

        if (!arena.getState().isInGame()) {
            player.teleport(wsk.getArenaManager().getCurrentArena().getArenaConfig().getSpawn());
            if (Bukkit.getOnlinePlayers().size() == 1) {
                arena.getScoreboardManager().getSidebar().updateScoreboard();
            }
        } else {
            player.setGameMode(GameMode.SPECTATOR);
            player.teleport(wsk.getArenaManager().getCurrentArena().getArenaConfig().getSpectatorSpawn());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Arena arena = wsk.getArenaManager().getCurrentArena();
        arena.getScoreboardManager().handleQuitEvent(event);

        WSKPlayer player = WSKPlayer.getPlayer(event.getPlayer());

        if (player.getTeam() != null) {
            if (arena.getState().isInGame())
                Bukkit.getPluginManager().callEvent(new TeamMemberDieEvent(arena, player.getTeam(), player));

            player.getTeam().removeMember(player);
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (Config.isSetup())
            return;

        Arena arena = wsk.getArenaManager().getCurrentArena();

        if (arena.getState().isInGame()) {
            event.getPlayer().setGameMode(GameMode.SPECTATOR);
            event.setRespawnLocation(arena.getArenaConfig().getSpectatorSpawn());
        } else
            event.setRespawnLocation(arena.getArenaConfig().getSpawn());
    }

    @EventHandler
    public void onDie(PlayerDeathEvent event) {
        WSKPlayer player = WSKPlayer.getPlayer(event.getEntity());

        if (player.isDead())
            return;

        if (player.getTeam() != null) {
            Bukkit.getPluginManager().callEvent(new TeamMemberDieEvent(wsk.getArenaManager().getCurrentArena(), player.getTeam(), player));
            event.setDeathMessage(player.getPlayer().getDisplayName() + " §7ist gestorben");
        } else
            event.setDeathMessage("");
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("wsk.bypass.build"))
            return;

        WSKPlayer player = WSKPlayer.getPlayer(event.getPlayer());

        Arena arena = wsk.getArenaManager().getCurrentArena();

        if (!arena.getState().isInGame() && arena.getState() != ArenaState.PRERUNNING)
            return;
        if (player.getTeam() == null)
            return;

        boolean build = false;
        for (Team team : arena.getTeams()) {
            if (RegionUtil.isInLargeShipArea(team, event.getBlockPlaced().getLocation())) {
                build = true;
                break;
            }
        }
        if (event.getBlockPlaced().getType() == Material.TNT && !player.getRole().getRole().isCanTnt())
            build = false;
        event.setBuild(build);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (event.getPlayer().hasPermission("wsk.bypass.move"))
            return;

        WSKPlayer player = WSKPlayer.getPlayer(event.getPlayer());

        Arena arena = wsk.getArenaManager().getCurrentArena();

        boolean canMove = true;

        if (arena.getState() == ArenaState.IDLE)
            return;

        if (arena.getState().isInGame() && player.isInTeam()) {
            Team enemyTeam = arena.getEnemyTeam(player.getTeam());

            if (!RegionUtil.isInTinyShipArea(enemyTeam, event.getFrom()) && RegionUtil.isInTinyShipArea(enemyTeam, event.getTo())) {
                Bukkit.getPluginManager().callEvent(new PlayerEnterEvent(arena, enemyTeam, player));
            }
        }

        if (player.getTeam() != null && arena.getState() != ArenaState.SPECTATE) {

            if (arena.getState() == ArenaState.ENTER && player.getRole().getRole().isCanEnter())
                return;
            if (arena.getState() == ArenaState.ENTER_ALL && player.getRole().getRole().isCanEnterAtAll())
                return;


            if (RegionUtil.isInLargeShipArea(player.getTeam(), event.getFrom()) && !RegionUtil.isInLargeShipArea(player.getTeam(), event.getTo())) {
                event.getPlayer().sendMessage(WSK.PREFIX + "§cDu darfst noch nicht entern");
                canMove = false;
            }
        }
        if (arena.getState() == ArenaState.SPECTATE || player.getTeam() == null) {
            for (Team team : arena.getTeams()) {
                if (!RegionUtil.isInTinyShipArea(team, event.getFrom()) &&
                        RegionUtil.isInTinyShipArea(team, event.getTo())) {
                    canMove = false;
                    break;
                }
            }
        }

        event.setCancelled(!canMove);
    }
}
