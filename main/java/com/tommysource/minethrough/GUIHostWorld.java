package com.tommysource.minethrough;

import java.awt.Color;
import java.io.IOException;

import javax.sound.midi.MidiUnavailableException;

import org.lwjgl.input.Keyboard;

import com.mojang.authlib.AuthenticationService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GUIHostWorld extends GuiScreen
{
	private GuiTextField password;

	private GuiButton back;
	private GuiCheckBox host;

	private GuiScreen prev;

	GUIHostWorld(GuiScreen prev)
	{
		this.mc = Minecraft.getMinecraft();
		this.fontRenderer = mc.fontRenderer;
		this.prev = prev;
		
		this.password = new GuiTextField(0, this.fontRenderer, this.width / 2 - 155, 70, 2 * 155, 20);
		this.password.setMaxStringLength(512);
		this.password.setFocused(true);
		this.password.setText(MineThrough.hostPassword);
	}

	@Override
	protected void actionPerformed(GuiButton b)
	{
		switch (b.id)
		{
		case 3:
						
			MineThrough.hostOnLoad = host.isChecked();
			MineThrough.hostPassword = password.getText();
			
			this.mc.displayGuiScreen(prev);
			break;
		}
	}
	
	@Override
	public void drawScreen(int a, int b, float c)
	{
		this.drawDefaultBackground();

		this.drawCenteredString(this.fontRenderer, "MineThrough World Hosting", this.width / 2, 20, Color.WHITE.getRGB());
		
		this.drawCenteredString(this.fontRenderer, "Server Password:", this.width / 2, 50, Color.WHITE.getRGB());
		this.drawCenteredString(this.fontRenderer, "Do not use your minecraft.net password!", this.width / 2, 100, Color.WHITE.getRGB());
		this.drawCenteredString(this.fontRenderer, "Host MineThrough On World Load", this.width / 2, 150, Color.WHITE.getRGB());
		
		this.password.drawTextBox();

		super.drawScreen(a, b, c);
	}

	@Override
	public void updateScreen()
	{
		super.updateScreen();
		this.password.drawTextBox();
	}

	@Override
	public void initGui()
	{
		super.initGui();
		Keyboard.enableRepeatEvents(true);
		
		this.password.x = this.width / 2 - 155;
		
		this.host = new GuiCheckBox(2, this.width / 2 - 5, 130, "", MineThrough.hostOnLoad);
		this.buttonList.add(host);

		this.back = new GuiButton(3, this.width / 2 - 100, 210, 200, 20, "Back");
		this.buttonList.add(this.back);
	}

	@Override
	protected void keyTyped(char c, int k) throws IOException
	{
		super.keyTyped(c, k);
		this.password.textboxKeyTyped(c, k);
	}

	@Override
	protected void mouseClicked(int x, int y, int b) throws IOException
	{
		super.mouseClicked(x, y, b);
		this.password.mouseClicked(x, y, b);
	}

	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
	}
}
