package com.tommysource.minethrough;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import scala.actors.threadpool.Arrays;

public class HostThread extends Thread
{
	public static final int TYPE_NEW = 1;
	public static final int TYPE_UPDATE = 2;
	
	public static final int PING_DELAY = 5 * 60;
	
	static int REGISTER_PORT = 19305;

	public boolean running = true;

	public String username = "";
	public String password = "";

	public MinecraftServer server;

	int localPort = (int) (Math.random() * 10000) + 49152;

	public void broadcastString(String text)
	{
		server.getPlayerList().getPlayers().forEach((p) ->
		{
			p.sendMessage(new TextComponentString("[MineThrough] " + text));
		});
	}

	public void run()
	{
		broadcastString("Starting...");

		System.out.println("[MineThrough]: Current port:" + localPort);

		DatagramSocket socket;
		try
		{
			socket = new DatagramSocket();
			socket.setSoTimeout(1000);
		}
		catch (SocketException e)
		{
			e.printStackTrace();
			broadcastString("Failure to start, check the console for more details");
			return;
		}

		if (!Util.registerServer(username, password, localPort, TYPE_NEW, socket))
		{
			broadcastString("Failure to start, could not register server with MineThrough, check the console for more details");
		}

		broadcastString("Server started!");

		
		int pingCount = 0;
		
		
		while (running)
		{
			try
			{
				String[] inRequest;
				
				try
				{
					inRequest = UDPUtil.receiveMessage(socket).split("\\|");
				}
				catch (SocketTimeoutException e)
				{
					pingCount++;
										
					if (pingCount >= PING_DELAY) //Keep the UDP hole alive
					{
						UDPUtil.sendMessage("PING", socket);
						pingCount = 0;
					}
					
					continue;
				}

				if (!inRequest[0].startsWith("CREATE"))
				{
					continue;
				}

				String requestedHole = inRequest[1].split(":")[0];
				
				System.out.println("[MineThrough]: Got a request for punchthrough at " + inRequest[1]);
				if (!Util.punchHole(requestedHole, localPort))
				{
					System.out.println("[MineThrough]: Failed to punch hole for incoming connection");
				}

				try
				{
					server.getNetworkSystem().addLanEndpoint(null, localPort);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				String reply = "CREATE|" + inRequest[2];
				UDPUtil.sendMessage(reply, socket);
				UDPUtil.sendMessage(reply, socket);
				UDPUtil.sendMessage(reply, socket);
				
				localPort = (int) (Math.random() * 10000) + 49152;
				System.out.println("[MineThrough]: Current port: " + localPort);
				if (!Util.registerServer(username, password, localPort, TYPE_UPDATE, socket))
				{
					broadcastString("Error updating server with MineThrough, no new players will be able to join");
				}
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		socket.close();
	}
}
