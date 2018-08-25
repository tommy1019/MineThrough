package com.tommysource.minethrough;

import java.io.IOException;

import org.apache.logging.log4j.Logger;

import net.minecraft.command.CommandHandler;
import net.minecraft.command.CommandServerKick;
import net.minecraft.command.CommandSetPlayerTimeout;
import net.minecraft.command.server.CommandBanIp;
import net.minecraft.command.server.CommandBanPlayer;
import net.minecraft.command.server.CommandListBans;
import net.minecraft.command.server.CommandListPlayers;
import net.minecraft.command.server.CommandPardonIp;
import net.minecraft.command.server.CommandPardonPlayer;
import net.minecraft.command.server.CommandWhitelist;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;

@Mod(modid = MineThrough.MODID, name = MineThrough.NAME, version = MineThrough.VERSION)
@Mod.EventBusSubscriber
public class MineThrough
{
	public static final String MODID = "minethrough";
	public static final String NAME = "MineThrough";
	public static final String VERSION = "1.1.0";

	@SidedProxy(clientSide = "com.tommysource.minethrough.ClientProxy", serverSide = "com.tommysource.minethrough.ServerProxy")
	public static IProxy proxy;

	public static Logger logger;

	public static String curUsername = "";
	public static String curPassword = "";

	static Configuration config;

	public static HostThread hostThread;

	public static boolean hostOnLoad = false;
	public static String hostPassword = "";
	
	public static boolean tryingToConnect = false;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		proxy.preInit(event);
		logger = event.getModLog();

		MinecraftForge.EVENT_BUS.register(this);
		hostThread = new HostThread();
	}

	@EventHandler
	public void serverLoad(FMLServerStartingEvent event)
	{
		event.registerServerCommand(new OpenCommand());
	}

	@SubscribeEvent
	public void onConnect(ClientConnectedToServerEvent event)
	{
		if (tryingToConnect)
		{
			tryingToConnect = false;
			try
			{
				Util.getRecordResult(curUsername, Util.RecordStatus.SUCCESS, 0);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@SubscribeEvent
	public void playerJoin(PlayerLoggedInEvent event)
	{
		if (event.player.getServer().isSinglePlayer())
			if (event.player.getServer().getCurrentPlayerCount() == 1)
			{
				if (hostOnLoad)
				{
					MineThrough.hostThread = new HostThread();
					MineThrough.hostThread.username = event.player.getDisplayNameString();
					MineThrough.hostThread.password = hostPassword;
					MineThrough.hostThread.server = event.player.getServer();
					MineThrough.hostThread.start();

					CommandHandler cm = (CommandHandler) event.player.getServer().commandManager;

					cm.registerCommand(new CommandBanIp());
					cm.registerCommand(new CommandPardonIp());
					cm.registerCommand(new CommandBanPlayer());
					cm.registerCommand(new CommandListBans());
					cm.registerCommand(new CommandPardonPlayer());
					cm.registerCommand(new CommandServerKick());
					cm.registerCommand(new CommandListPlayers());
					cm.registerCommand(new CommandWhitelist());
					cm.registerCommand(new CommandSetPlayerTimeout());
				}
			}
			else
			{
				if (event.player.getServer() instanceof IntegratedServer)
				{
					((IntegratedServer) event.player.getServer()).shareToLAN(GameType.NOT_SET, false);
				}
			}
	}

	@EventHandler
	public void serverClose(FMLServerStoppingEvent event)
	{
		if (hostThread != null)
		{
			hostThread.running = false;
			hostThread.interrupt();
		}
	}
}
