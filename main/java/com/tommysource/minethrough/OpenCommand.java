package com.tommysource.minethrough;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class OpenCommand extends CommandBase
{
	private final List aliases;

	public OpenCommand()
	{
		aliases = new ArrayList();
		aliases.add("minethrough");
		aliases.add("mt");
	}

	@Override
	public String getName()
	{
		return "minethrough";
	}

	@Override
	public List<String> getAliases()
	{
		return aliases;
	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "/minethrough open <password>";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		if (!(sender instanceof EntityPlayer))
		{
			sender.sendMessage(new TextComponentString("Must be executed by a player"));
			return;
		}

		if (args.length != 2)
		{
			sender.sendMessage(new TextComponentString("Incorrect number of arguments"));
			return;
		}

		if (!args[0].toLowerCase().equals("open"))
		{
			sender.sendMessage(new TextComponentString("Unknown operation"));
			return;
		}

		String username = "error";
		String password = "error";

		username = sender.getDisplayName().getUnformattedText();
		password = args[1];

		sender.sendMessage(new TextComponentString("Starting MineThrough with username: \"" + username + "\" and server password: \"" + password + "\""));

		MineThrough.hostThread = new HostThread();
		MineThrough.hostThread.username = username;
		MineThrough.hostThread.password = password;
		MineThrough.hostThread.server = server;
		MineThrough.hostThread.start();
	}

}
