package net.myplayplanet.wsk.event;

import lombok.Getter;
import net.myplayplanet.wsk.arena.Arena;
import net.myplayplanet.wsk.objects.Team;
import net.myplayplanet.wsk.objects.WSKPlayer;
import org.bukkit.event.HandlerList;

/**
 * Event gets fired when a member of a team dies
 */
@Getter
public class TeamMemberDieEvent extends ArenaEvent {

    private final Team team;
    private final WSKPlayer player;

    public TeamMemberDieEvent(Arena arena, Team team, WSKPlayer player) {
        super(arena);
        this.team = team;
        this.player = player;
    }

    public final static HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public final static HandlerList getHandlerList() {
        return handlers;
    }
}