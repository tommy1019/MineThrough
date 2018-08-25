package com.tommysource.minethrough;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.client.event.GuiScreenEvent.ActionPerformedEvent;
import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy implements IProxy
{
	public static final int MINETHROUGH_BUTTON_ID = 174026;
	
	@Override
	public void preInit(FMLPreInitializationEvent event)
	{
		MinecraftForge.EVENT_BUS.register(this);
		
		File configFile = new File(Minecraft.getMinecraft().mcDataDir, ".MineThrough_lastUser.cfg");

		try
		{
			BufferedReader in = new BufferedReader(new FileReader(configFile));
			MineThrough.curUsername = in.readLine();
			
			if (MineThrough.curUsername == null)
				MineThrough.curUsername = "";
			
			in.close();
		}
		catch (IOException e)
		{
		}
	}
	
	@SubscribeEvent
	public void open(InitGuiEvent.Post e)
	{
		if (e.getGui() instanceof GuiMultiplayer)
		{
			e.getButtonList().add(new GuiButton(MINETHROUGH_BUTTON_ID, e.getGui().width - 80, 5, 75, 20, "MineThrough"));
		}
		else if (e.getGui() instanceof GuiWorldSelection)
		{
			e.getButtonList().add(new GuiButton(MINETHROUGH_BUTTON_ID, e.getGui().width - 80, 5, 75, 20, "MineThrough"));
		}
	}

	@SubscribeEvent
	public void action(ActionPerformedEvent.Post e)
	{
		if (e.getGui() instanceof GuiMultiplayer && e.getButton().id == MINETHROUGH_BUTTON_ID)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GUIConnect(Minecraft.getMinecraft().currentScreen));
		}
		else if (e.getGui() instanceof GuiWorldSelection && e.getButton().id == MINETHROUGH_BUTTON_ID)
		{
			Minecraft.getMinecraft().displayGuiScreen(new GUIHostWorld(Minecraft.getMinecraft().currentScreen));
		}
	}
}
