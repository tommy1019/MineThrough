package com.tommysource.minethrough;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

public class UDPUtil
{
	public static final int MAX_MESSAGE_SIZE = 1024;
	public static final SocketAddress DEFAULT_ADDRESS = new InetSocketAddress("mt.tommysource.com", 8080);
	
	public static void sendMessage(String msg, DatagramSocket socket) throws IOException
	{
		sendMessage(msg, socket, DEFAULT_ADDRESS);
	}
	
	public static void sendMessage(String msg, DatagramSocket socket, SocketAddress addr) throws IOException
	{
		DatagramPacket outPacket = new DatagramPacket(msg.getBytes(), msg.getBytes().length, addr);
		socket.send(outPacket);
	}
	
	public static String receiveMessage(DatagramSocket socket) throws SocketTimeoutException, IOException
	{
		byte[] buffer = new byte[MAX_MESSAGE_SIZE];
		DatagramPacket inPacket = new DatagramPacket(buffer, MAX_MESSAGE_SIZE);
		socket.receive(inPacket);
		return new String(inPacket.getData());
	}
}
