package net.myplayplanet.wsk.objects;

import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.myplayplanet.wsk.Config;
import net.myplayplanet.wsk.arena.Arena;
import net.myplayplanet.wsk.arena.ArenaState;
import net.myplayplanet.wsk.event.*;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
@ToString
public class Team implements Iterable<WSKPlayer>, Comparable<Team> {

    private List<WSKPlayer> members = new ArrayList<>();
    private final TeamProperties properties;
    private final Arena arena;
    private final Ship ship = new Ship(this);
    @Setter
    private int points = 0;
    private double maxPercentage;
    private double pointsPerPercentage;

    public double getPercentDamage() {
        double blocks = ship.getBlocks();
        double diff = (ship.getInitBlocks() - blocks) / ship.getInitBlocks();
        double ret = diff * 100;
        if (ret < 0)
            return 0;
        if (ret > maxPercentage)
            return maxPercentage;
        return ret;
    }

    public int calculatePoints() {
        int points = this.points;
        for (Team team : arena.getTeams()) {
            if (this == team)
                continue;
            points += team.calculatePointsForDamage();
        }
        return points;
    }

    public int calculatePointsForDamage() {
        return (int) (pointsPerPercentage * getPercentDamage());
    }

    public void setCalculations() {
        Team thisTeam = this;
        arena.getTeams().stream().filter(t -> t != thisTeam).forEach((team) -> {
            double factor;

            double initBlocks = ship.getInitBlocks();
            double foreigenInitBlocks = team.getShip().getInitBlocks();
            if (initBlocks < foreigenInitBlocks)
                factor = initBlocks / foreigenInitBlocks;
            else
                factor = foreigenInitBlocks / initBlocks;

            if (factor < Config.getMinFactor())
                factor = Config.getMinFactor();
            if (factor > Config.getMaxFactor())
                factor = Config.getMaxFactor();


            if (foreigenInitBlocks < initBlocks) {
                maxPercentage = 20;
                pointsPerPercentage = 100;
            } else {
                maxPercentage = Math.round(20 / factor);
                pointsPerPercentage = Math.round(100 * factor);
            }
        });
    }

    public void addMember(WSKPlayer player) {
        Objects.requireNonNull(player);
        Preconditions.checkArgument(player.getTeam() == null, "Player must not be in a team");

        TeamAddMemberArenaEvent event = new TeamAddMemberArenaEvent(arena, this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (members.size() == 0)
            player.setCaptain(true);

        members.add(event.getPlayer());
        player.setTeam(this);

        if (player.isCaptain()) {
            TeamCaptainSetArenaEvent captainArenaEvent = new TeamCaptainSetArenaEvent(arena, this, player);
            Bukkit.getPluginManager().callEvent(captainArenaEvent);
        }

        checkState(arena);
    }


    public void removeMember(WSKPlayer player) {
        Objects.requireNonNull(player);
        Preconditions.checkArgument(members.contains(player), "player must be member of team");

        TeamRemoveMemberArenaEvent event = new TeamRemoveMemberArenaEvent(arena, this, player);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (player.isCaptain()) {
            TeamCaptainRemoveArenaEvent captainArenaEvent = new TeamCaptainRemoveArenaEvent(arena, this, player);
            Bukkit.getPluginManager().callEvent(captainArenaEvent);
        }

        members.remove(event.getPlayer());
        player.setTeam(null);
        player.setCaptain(false);
        checkState(arena);
    }

    public void setCaptain(WSKPlayer player) {
        Objects.requireNonNull(player);
        Preconditions.checkArgument(members.contains(player), "player must be member of team");

        WSKPlayer captain = getCaptain();
        Preconditions.checkArgument(!captain.equals(player), "player is already captain");

        if (captain != null) {
            TeamCaptainRemoveArenaEvent captainRemoveArenaEvent = new TeamCaptainRemoveArenaEvent(arena, this, captain);
            Bukkit.getPluginManager().callEvent(captainRemoveArenaEvent);
            captain.setCaptain(false);
        }

        player.setCaptain(true);
        TeamCaptainSetArenaEvent captainSetArenaEvent = new TeamCaptainSetArenaEvent(arena, this, player);
        Bukkit.getPluginManager().callEvent(captainSetArenaEvent);
    }

    private void checkState(Arena arena) {
        if (!arena.getState().isInGame()) {
            if (arena.getState() == ArenaState.IDLE) {
                for (Team team : arena.getTeams()) {
                    if (team.getMembers().size() > 0) {
                        ArenaStateChangeEvent changeEvent = new ArenaStateChangeEvent(arena.getState(), ArenaState.SETUP, arena);
                        Bukkit.getPluginManager().callEvent(changeEvent);
                        return;
                    }
                }
            } else if (arena.getState() == ArenaState.SETUP) {
                for (Team team : arena.getTeams()) {
                    if (team.getMembers().size() > 0) return;
                }
                ArenaStateChangeEvent changeEvent = new ArenaStateChangeEvent(arena.getState(), ArenaState.IDLE, arena);
                Bukkit.getPluginManager().callEvent(changeEvent);
            }
        }
    }

    public boolean isTeam2() {
        return arena.getTeams().indexOf(this) == 1;
    }

    public boolean isTeam1() {
        return arena.getTeams().indexOf(this) == 0;
    }

    /**
     * Calculates points and sets them to the field "points"
     */
    public void setPoints() {
        points = calculatePoints();
    }

    public void addPoints(int points) {
        setPoints(this.points + points);
    }

    public WSKPlayer getCaptain() {
        return members.stream().filter(WSKPlayer::isCaptain).collect(Collectors.toList()).get(0);
    }

    public Iterator<WSKPlayer> iterator() {
        return members.iterator();
    }

    public List<WSKPlayer> getAliveMembers() {
        return members.stream().filter(WSKPlayer::isAlive).collect(Collectors.toList());
    }

    @Override
    public int compareTo(Team team) {
        return Integer.compare(team.getPoints(), this.getPoints());
    }
}
