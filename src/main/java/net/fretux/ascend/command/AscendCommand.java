package net.fretux.ascend.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.fretux.ascend.network.ClientStatsPayload;
import net.fretux.ascend.player.PlayerStats;
import net.fretux.ascend.player.PlayerStatsProvider;
import net.fretux.ascend.player.StatEffects;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class AscendCommand {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("ascend")
                        .requires(src -> src.hasPermission(2))
                        .then(Commands.literal("get")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AscendCommand::getStats)))
                        .then(Commands.literal("set")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("attribute", StringArgumentType.string())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(AscendCommand::setAttribute)))))
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                                .executes(AscendCommand::addXP))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(AscendCommand::resetStats)))
        );
    }

    private static int getStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        var stats = target.getData(PlayerStatsProvider.PLAYER_STATS);
        ctx.getSource().sendSuccess(() -> Component.literal("=== Ascend Stats for " + target.getName().getString() + " ==="), false);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Level: " + stats.getAscendLevel() + " | XP: " + stats.getAscendXP() + "/" + stats.getXPToNextAscendLevel()), false);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Unspent Points: " + stats.getUnspentPoints()), false);
        CompoundTag attrTag = stats.serializeNBT().getCompound("Attributes");
        for (String key : attrTag.getAllKeys()) {
            int val = attrTag.getInt(key);
            ctx.getSource().sendSuccess(() -> Component.literal(" - " + key + ": " + val), false);
        }
        return 1;
    }

    private static int setAttribute(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        String attr = StringArgumentType.getString(ctx, "attribute");
        int value = IntegerArgumentType.getInteger(ctx, "value");
        var stats = target.getData(PlayerStatsProvider.PLAYER_STATS);
        stats.setAttributeLevel(attr, value);
        StatEffects.applyAll(target);
        sync(target, stats);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Set " + target.getName().getString() + "'s " + attr + " to " + value), true);
        return 1;
    }

    private static int addXP(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");
        var stats = target.getData(PlayerStatsProvider.PLAYER_STATS);
        stats.addAscendXP(amount);
        StatEffects.applyAll(target);
        sync(target, stats);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Added " + amount + " Ascend XP to " + target.getName().getString()), true);
        return 1;
    }

    private static int resetStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer target = EntityArgument.getPlayer(ctx, "player");
        var stats = target.getData(PlayerStatsProvider.PLAYER_STATS);
        PlayerStats fresh = new PlayerStats();
        stats.deserializeNBT(fresh.serializeNBT());
        StatEffects.applyAll(target);
        sync(target, stats);
        ctx.getSource().sendSuccess(() ->
                Component.literal("Reset " + target.getName().getString() + "'s Ascend stats."), true);
        return 1;
    }

    private static void sync(ServerPlayer player, PlayerStats stats) {
        PacketDistributor.sendToPlayer(player, new ClientStatsPayload(stats.serializeNBT()));
    }
}
