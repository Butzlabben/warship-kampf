package net.myplayplanet.wsk.objects.scoreboard;

import net.myplayplanet.wsk.arena.Arena;
import org.bukkit.scoreboard.Objective;

public interface ObjectiveWorker {

    void editObjective(Arena arena, Objective objective);
}
