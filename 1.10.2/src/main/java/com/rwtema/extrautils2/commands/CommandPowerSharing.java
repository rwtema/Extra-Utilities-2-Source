package com.rwtema.extrautils2.commands;

import com.rwtema.extrautils2.gui.ContainerPlayerAlliances;
import com.rwtema.extrautils2.gui.backend.GuiHandler;
import com.rwtema.extrautils2.network.NetworkHandler;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nonnull;

public class CommandPowerSharing extends CommandBase {

	public static final String COMMAND_NAME = "xu_powersharing";

	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Nonnull
	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "/" + COMMAND_NAME;
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		NetworkHandler.sendPacketToServer(new GuiHandler.PacketOpenGui(ContainerPlayerAlliances.ID));
	}
}
