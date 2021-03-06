package com.superluli.socketchatserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

public class Server {

    private static Map<String, Socket> SOCKET_POOL = new HashMap<String, Socket>();

    private static ChatterBotSession SESSION;

    static {

	try {
	    SESSION = new ChatterBotFactory().create(ChatterBotType.CLEVERBOT).createSession();
	} catch (Exception e) {

	}
    }

    public static void main(String[] args) throws Exception {

	@SuppressWarnings("resource")
	ServerSocket server = new ServerSocket(8888);

	while (true) {
	    final Socket connection = server.accept();
	    new Thread(new Runnable() {
		@Override
		public void run() {
		    handleConnection(connection);
		}
	    }).start();
	}
    }

    private static void handleConnection(Socket connection) {

	try {
	    String userName = null;
	    boolean handShake = true;
	    BufferedReader br = new BufferedReader(new InputStreamReader(
		    connection.getInputStream()));
	    OutputStream out = connection.getOutputStream();
	    OutputStreamWriter ow = new OutputStreamWriter(out);

	    ow.write("Welcome to LuBot's chatroom, choose a name : " + "\n");
	    ow.flush();

	    String line = null;
	    while ((line = br.readLine()) != null) {
		if (handShake) {
		    userName = line;
		    SOCKET_POOL.put(userName, connection);
		    handShake = false;
		    broadcast(null, userName + " joined the room, welcome !");
		    broadcast(null, "LusBot" + " : " + botReply("Hi"));
		    System.out.println(SOCKET_POOL);
		} else {
		    broadcast(userName, userName + " : " + line);
		    broadcast(null, "LusBot" + " : " + botReply(line));
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /*
     * senderName == null, means full broadcast
     */

    private static void broadcast(String senderName, String message) {
	try {
	    for (Map.Entry<String, Socket> entry : SOCKET_POOL.entrySet()) {
		if (!entry.getKey().equals(senderName)) {
		    Socket connection = entry.getValue();
		    OutputStream out = connection.getOutputStream();
		    OutputStreamWriter ow = new OutputStreamWriter(out);

		    String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		    ow.write(time + " - " + message + "\n");
		    ow.flush();
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private static String botReply(String input) {

	try {
	    return SESSION.think(input);
	} catch (Exception e) {
	    return "There is a bug in my brain, I need to fix ";
	}

    }
}
