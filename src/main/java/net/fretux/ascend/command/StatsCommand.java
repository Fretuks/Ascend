package net.fretux.ascend.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StatsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("stats")
                .then(Commands.literal("show")
                        .executes(ctx -> {
                            var player = ctx.getSource().getPlayerOrException();
                            player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                                player.sendSystemMessage(Component.literal("Unspent points: " + stats.getUnspentPoints()));
                                for (String key : stats.serializeNBT().getCompound("Attributes").getAllKeys()) {
                                    int lvl = stats.getAttributeLevel(key);
                                    int xp = stats.getXP(key);
                                    int needed = stats.getXPToNextLevel(key);
                                    player.sendSystemMessage(Component.literal(
                                            key + " → Lvl " + lvl + " | XP: " + xp + "/" + needed
                                    ));
                                }
                            });
                            return 1;
                        }))
                .then(Commands.literal("spend")
                        .then(Commands.argument("attribute", StringArgumentType.string())
                                .executes(ctx -> {
                                    var player = ctx.getSource().getPlayerOrException();
                                    String attr = StringArgumentType.getString(ctx, "attribute");
                                    player.getCapability(PlayerStatsProvider.PLAYER_STATS).ifPresent(stats -> {
                                        int cost = stats.getXPToNextLevel(attr);
                                        boolean success = stats.spendPoints(attr);
                                        if (success) {
                                            player.sendSystemMessage(Component.literal("You invested a point in " + attr + "!"));
                                        } else {
                                            player.sendSystemMessage(Component.literal("Not enough points to upgrade " + attr + "."));
                                        }
                                    });
                                    return 1;
                                })))
        );
    }
}
