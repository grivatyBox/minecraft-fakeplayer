package net.minecraft.commands.arguments;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.IChatBaseComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class WaypointArgument {

    public static final SimpleCommandExceptionType ERROR_NOT_A_WAYPOINT = new SimpleCommandExceptionType(IChatBaseComponent.translatable("argument.waypoint.invalid"));

    public WaypointArgument() {}

    public static WaypointTransmitter getWaypoint(CommandContext<CommandListenerWrapper> commandcontext, String s) throws CommandSyntaxException {
        Entity entity = ((EntitySelector) commandcontext.getArgument(s, EntitySelector.class)).findSingleEntity((CommandListenerWrapper) commandcontext.getSource());

        if (entity instanceof WaypointTransmitter waypointtransmitter) {
            return waypointtransmitter;
        } else {
            throw WaypointArgument.ERROR_NOT_A_WAYPOINT.create();
        }
    }
}
