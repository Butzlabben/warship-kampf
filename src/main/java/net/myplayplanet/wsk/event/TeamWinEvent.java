package net.myplayplanet.wsk.event;

import lombok.Getter;
import net.myplayplanet.wsk.arena.Arena;
import net.myplayplanet.wsk.objects.Team;
import org.bukkit.event.HandlerList;

/**
 * Event gets fired if a team wins
 */
@Getter
public class TeamWinEvent extends ArenaEvent {

    private final Team team;

    public TeamWinEvent(Arena arena, Team team) {
        super(arena);
        this.team = team;
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