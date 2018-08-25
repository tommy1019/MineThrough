package com.tommysource.minethrough;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GUIConnect extends GuiScreen
{
	private GuiTextField username;
	private GuiTextField password;

	private GuiButton join;
	private GuiButton cancel;

	private GuiScreen prev;

	GUIConnect(GuiScreen prev)
	{
		super();
		
		this.mc = Minecraft.getMinecraft();
		this.fontRenderer = mc.fontRenderer;
		this.prev = prev;
		
		this.username = new GuiTextField(0, this.fontRenderer, this.width / 2 - 155, 70, 2 * 155, 20);
		this.username.setMaxStringLength(512);
		this.username.setFocused(true);
		this.username.setText(MineThrough.curUsername);

		this.password = new GuiTextField(1, this.fontRenderer, this.width / 2 - 155, 120, 2 * 155, 20);
		this.password.setMaxStringLength(512);
		this.password.setText(MineThrough.curPassword);
	}

	@Override
	protected void actionPerformed(GuiButton b)
	{
		if (MineThrough.tryingToConnect)
			return;
		
		switch (b.id)
		{
		case 3:
			this.mc.displayGuiScreen(prev);
			break;
		case 2:
			
			MineThrough.curUsername = username.getText();
			MineThrough.curPassword = password.getText();
			
			File configFile = new File(Minecraft.getMinecraft().mcDataDir, ".MineThrough_lastUser.cfg");
			try
			{
				BufferedWriter out = new BufferedWriter(new FileWriter(configFile));
				out.write(username.getText());
				out.close();
			}
			catch (IOException e)
			{
			}
			
			this.mc.displayGuiScreen(new GUIConnecting(this, username.getText(), password.getText()));
			break;
		}
	}
	
	@Override
	public void drawScreen(int a, int b, float c)
	{
		this.drawDefaultBackground();

		this.drawCenteredString(this.fontRenderer, "Join using MineThrough", this.width / 2, 20, Color.WHITE.getRGB());

		this.drawCenteredString(this.fontRenderer, "Username:", this.width / 2, 50, Color.WHITE.getRGB());
		this.drawCenteredString(this.fontRenderer, "Server Password:", this.width / 2, 100, Color.WHITE.getRGB());
		this.drawCenteredString(this.fontRenderer, "Do not enter your minecraft.net password!", this.width / 2, 150, Color.WHITE.getRGB());

		this.username.drawTextBox();
		this.password.drawTextBox();

		super.drawScreen(a, b, c);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		this.username.drawTextBox();
	}

	@Override
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		
		this.username.x = this.width / 2 - 155;
		this.password.x = this.width / 2 - 155;
		
		this.join = new GuiButton(2, this.width / 2 + 75 - 50, 180, 100, 20, "Join");
		this.cancel = new GuiButton(3, this.width / 2 - 75 - 50, 180, 100, 20, "Cancel");
		this.buttonList.add(this.join);
		this.buttonList.add(this.cancel);
		
		if (MineThrough.tryingToConnect)
		{
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						Util.getRecordResult(MineThrough.curUsername, Util.RecordStatus.FAILURE, 0);
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					MineThrough.tryingToConnect = false;
				}
			}).start();
		}
	}

	@Override
	protected void keyTyped(char c, int k) throws IOException
	{
		super.keyTyped(c, k);
		this.username.textboxKeyTyped(c, k);
		this.password.textboxKeyTyped(c, k);

		if (k == Keyboard.KEY_TAB)
		{
			if (this.username.isFocused())
			{
				this.username.setFocused(false);
				this.password.setFocused(true);
			}
		}
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException
	{
		super.mouseClicked(x, y, b);
		this.username.mouseClicked(x, y, b);
		this.password.mouseClicked(x, y, b);
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		Keyboard.enableRepeatEvents(false);
	}
}