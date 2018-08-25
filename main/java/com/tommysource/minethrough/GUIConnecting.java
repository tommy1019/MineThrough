package com.tommysource.minethrough;

import java.awt.Color;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.tommysource.minethrough.Util.RecordStatus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.TextComponentString;

public class GUIConnecting extends GuiScreen
{
	private GuiScreen prev;

	private GuiButton cancel;

	String username = "";
	String password = "";

	String statusMessage = "Fetching ip...";

	boolean tryToConnect = false;
	String ip = "";

	GUIConnecting(GuiScreen prev, String username, String password)
	{
		this.mc = Minecraft.getMinecraft();
		this.fontRenderer = mc.fontRenderer;
		this.prev = prev;

		this.username = username;
		this.password = password;

		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					try
					{
						Util.fetchSTUNData(Util.STUN_PORT);
					}
					catch (IOException e)
					{
						if (Util.foundIp.equals("") || Util.foundPort == -1)
						{
							statusMessage = "Error: There was a problem fetching the server ip.";
							e.printStackTrace();
							return;
						}
					}

					String status = Util.getJoinResult(username, password);
					if (status.startsWith("ERROR"))
					{
						if (status.startsWith("ERROR: No server found for that username and password"))
						{
							statusMessage = "Error: Incorrect username/password";
						}
						else
						{
							statusMessage = "Error: There was a problem with the MineThrough server";
							System.out.println(status);
						}

						return;
					}

					ip = status.split("_")[1];
					statusMessage = "Requesting path to server...";

					DatagramSocket socket = new DatagramSocket();
					socket.setSoTimeout(3000);

					String request = "REQ|" + ip + "|" + Util.foundIp + ":" + Util.foundPort;

					try
					{
						UDPUtil.sendMessage(request, socket);

						String response = UDPUtil.receiveMessage(socket);

						if (response.trim().equals("NOT_FOUND"))
						{
							status = "Error: The server is not registered";
							Util.getRecordResult(username, RecordStatus.PT_SERVER_NOT_FOUND, 0);
							return;
						}
						else if (response.trim().equals("SUCCESS"))
						{
							status = "Connecting...";
							tryToConnect = true;
							return;
						}
					}
					catch (SocketTimeoutException e)
					{
						status = "Error: The server is not responding";
						Util.getRecordResult(username, RecordStatus.PT_SERVER_TIMEOUT, 0);
					}
				}
				catch (Exception e)
				{
					statusMessage = "Error: There was a problem fetching the server ip.";
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void actionPerformed(GuiButton b)
	{
		switch (b.id)
		{
		case 3:
			this.mc.displayGuiScreen(prev);
			break;
		}
	}

	@Override
	public void initGui()
	{
		super.initGui();

		this.cancel = new GuiButton(3, this.width / 2 - 100, this.height / 2 + this.height / 4, 200, 20, "Cancel");
		this.buttonList.add(this.cancel);
	}

	@Override
	public void drawScreen(int a, int b, float c)
	{
		this.drawDefaultBackground();
		this.drawCenteredString(this.fontRenderer, statusMessage, this.width / 2, this.height / 2 - this.height / 8, Color.WHITE.getRGB());

		super.drawScreen(a, b, c);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();

		if (tryToConnect)
		{
			tryToConnect = false;
			MineThrough.tryingToConnect = true;
			ServerData serverData = new ServerData("MineThrough - " + username, ip, false);
			net.minecraftforge.fml.client.FMLClientHandler.instance().connectToServer(prev, serverData);
		}
	}
}
