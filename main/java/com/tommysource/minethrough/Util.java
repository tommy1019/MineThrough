package com.tommysource.minethrough;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

public class Util
{
	public static final String BASE_URL = "http://tommysource.com/";
	public static final String JOIN_PATH = "join.php";
	public static final String REGISTER_PATH = "host.php";
	public static final String RECORD_PATH = "record.php";

	public static final int STUN_PORT = 19305;

	public static final int RETRY_AMT = 3;

	public static int foundPort = -1;
	public static String foundIp = "";

	enum RecordStatus
	{
		SUCCESS(1), FAILURE(2), PT_SERVER_TIMEOUT(3), PT_SERVER_ERROR(4), PT_SERVER_NOT_FOUND(5);

		int val;

		RecordStatus(int val)
		{
			this.val = val;
		}
	}

	public static boolean punchHole(String ip, int localPort)
	{
		Socket hpSocket = new Socket();
		try
		{
			hpSocket.setReuseAddress(true);
			hpSocket.bind(new InetSocketAddress(localPort));
			hpSocket.connect(new InetSocketAddress(ip, STUN_PORT), 500);
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("[MineThrough] Hole punched for new connection");
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				hpSocket.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	public static boolean registerServer(String username, String password, int localPort, int type, DatagramSocket socket)
	{
		try
		{
			Util.fetchSTUNData(localPort);
		}
		catch (IOException e)
		{
			if (Util.foundIp.equals("") || Util.foundPort == -1)
			{
				e.printStackTrace();
				return false;
			}
		}

		String webServerReponse = "";
		try
		{
			webServerReponse = getRegisterResult(username, password, Util.foundIp, Util.foundPort, type);
		}
		catch (IOException e)
		{
			System.out.println("[MineThrough] Error registering with webserver");
			e.printStackTrace();
			return false;
		}

		if (!webServerReponse.trim().equals("SUCCESS"))
		{
			System.out.println("[MineThrough] Error registering with webserver: " + webServerReponse);
			return false;
		}

		try
		{
			String requestMsg = "REG|" + Util.foundIp + ":" + Util.foundPort;

			for (int i = 0; i < RETRY_AMT; i++)
			{
				UDPUtil.sendMessage(requestMsg, socket);

				try
				{
					String reponse = UDPUtil.receiveMessage(socket);

					if (reponse.trim().equals("SUCCESS"))
					{
						getRecordResult(username, RecordStatus.SUCCESS, 1);
						return true;
					}
					else
					{
						System.out.println("[MineThrough] Error registering with punchthrough server: " + reponse);
						getRecordResult(username, RecordStatus.PT_SERVER_ERROR, 1);
						return false;
					}
				}
				catch (SocketTimeoutException e)
				{
					continue;
				}
			}

			System.out.println("[MineThrough] No reponse from punchthrough server");
			getRecordResult(username, RecordStatus.PT_SERVER_TIMEOUT, 1);
			return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public static String getJoinResult(String username, String password) throws IOException
	{
		Map<String, String> arguments = new HashMap<>();
		arguments.put("usr", username);
		arguments.put("psw", password);

		return getHTTPResult(arguments, BASE_URL + JOIN_PATH);
	}

	public static String getRegisterResult(String username, String password, String ip, int port, int type) throws IOException
	{
		Map<String, String> arguments = new HashMap<>();
		arguments.put("usr", username);
		arguments.put("psw", password);
		arguments.put("ip", ip);
		arguments.put("port", "" + port);
		arguments.put("version", MineThrough.VERSION);
		arguments.put("type", "" + type);

		return getHTTPResult(arguments, BASE_URL + REGISTER_PATH);
	}

	public static String getRecordResult(String username, RecordStatus status, int isServer) throws IOException
	{
		Map<String, String> arguments = new HashMap<>();
		arguments.put("usr", username);
		arguments.put("status", "" + status.val);
		arguments.put("isserver", "" + isServer);
		arguments.put("version", MineThrough.VERSION);

		return getHTTPResult(arguments, BASE_URL + RECORD_PATH);
	}

	public static String getHTTPResult(Map<String, String> arguments, String requestURL) throws IOException
	{
		StringJoiner sj = new StringJoiner("&");
		for (Map.Entry<String, String> entry : arguments.entrySet())
			sj.add(URLEncoder.encode(entry.getKey(), "UTF-8") + "=" + URLEncoder.encode(entry.getValue(), "UTF-8"));
		byte[] out = sj.toString().getBytes(StandardCharsets.UTF_8);
		int length = out.length;

		URL url = new URL(requestURL);
		URLConnection con = url.openConnection();

		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST");
		http.setFixedLengthStreamingMode(length);
		http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
		http.setDoOutput(true);
		http.connect();

		try (OutputStream os = http.getOutputStream())
		{
			os.write(out);
		}

		StringBuilder result = new StringBuilder();

		BufferedReader rd = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null)
		{
			result.append(line);
		}
		rd.close();
		http.disconnect();

		return result.toString();
	}

	public static void fetchSTUNData(int localPort) throws IOException
	{
		Socket s = new Socket();
		s.setReuseAddress(true);
		s.bind(new InetSocketAddress(localPort));
		s.connect(new InetSocketAddress("stun1.l.google.com", STUN_PORT));
		DataOutputStream out = new DataOutputStream(s.getOutputStream());
		DataInputStream in = new DataInputStream(s.getInputStream());

		// Request Header
		out.writeByte(0x00);
		out.writeByte(0x01);

		// Message Length
		out.writeByte(0x00);
		out.writeByte(0x00);

		// Magic Cookie - 0x2112A442
		out.writeByte(0x21);
		out.writeByte(0x12);
		out.writeByte(0xA4);
		out.writeByte(0x42);

		Random r = new Random();
		byte[] messageId = new byte[12];
		r.nextBytes(messageId);

		out.write(messageId);
		out.flush();

		in.readShort();

		int messageLength = (int) in.readByte() << 8 | (int) in.readByte();

		byte[] magicCooke = new byte[4];
		in.read(magicCooke);

		if (magicCooke[0] != (byte) 0x21 || magicCooke[1] != (byte) 0x12 || magicCooke[2] != (byte) 0xA4 || magicCooke[3] != (byte) 0x42)
		{
			System.out.println("[STUN Request] - Incorrect magic cookie");
		}

		for (int i = 0; i < 12; i++)
			if (in.readByte() != messageId[i])
				System.out.println("[STUN Request] - Incorrect session id byte# " + i);

		int bytesLeft = messageLength;

		while (bytesLeft > 0)
		{
			short type = in.readShort();
			short length = in.readShort();

			bytesLeft -= length + 4;

			switch (type)
			{
			case 32:
				in.readByte();

				int family = in.readByte();

				int port = Byte.toUnsignedInt((byte) (in.readByte() ^ 0x21)) << 8 | Byte.toUnsignedInt((byte) (in.readByte() ^ 0x12));

				if (family == 1)
				{
					int hostPort = port;
					String hostIP = Byte.toUnsignedInt((byte) (in.readByte() ^ 0x21)) + "." + Byte.toUnsignedInt((byte) (in.readByte() ^ 0x12)) + "."
							+ Byte.toUnsignedInt((byte) (in.readByte() ^ 0xA4)) + "." + Byte.toUnsignedInt((byte) (in.readByte() ^ 0x42));

					foundIp = hostIP;
					foundPort = port;

					System.out.println("[STUN Request] - " + hostIP + ":" + port);
				}
				else if (family == 2)
				{
					System.out.println("[STUN Request] - IPv6 Not currently supported");
				}

				break;
			default:
				System.out.println("[STUN Request] - Uknown type: " + type);
				in.skip(length);
				break;
			}
		}

		s.close();
	}
}
