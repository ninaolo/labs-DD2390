import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * Klass för en server-tråd som sköter kommunikationen med en klient.
 */
public class ServerThread extends Thread {

	private ChatServer server;
	Socket clientSocket;
	DataInputStream input;
	DataOutputStream output;
	public String username;

	public ServerThread(ChatServer server, Socket clientSocket) {
		this.server = server;
		this.clientSocket = clientSocket;
		try {
			input = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());

			// Vi har gjort så att ChatClient alltid skickar sitt username när
			// den connectar
			username = input.readUTF();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Startar upp tråden genom att kalla på run()
		start();
	}
	
	public String getUsername(){
		return username;
	}
	
	private String getUser(String message){
		String user = "";
		message = message.substring(3,message.length());
		for(int i = 0;i<message.length();i++){
			String c = message.substring(i,i+1);
			if(c.compareTo(" ")==0){
				break;
			}
			else{
				user = user+c;
			}
		}
		return user;
	}



	public void run() {

		try {

			while (true) {

				// Läs klienternas meddelanden från input
				String message = input.readUTF();

				// Skickar ut meddelandet till alla klienter
				String whisper = message.substring(0, 3);
				if (message.compareTo("leave") == 0) {
					server.removeThread(this);
					break;
				}
				//Skicka privat meddelande om första tre i meddelandet = "/w "
				else if (whisper.compareTo("/w ") == 0){
					
					//Få ut vilken användare som meddelandet ska skickas till (efter "/w ")
					String sendToUser = getUser(message);
					
					//Kolla om användaren existerar, om den gör det så skickar den.
					if(server.userExists(sendToUser)){
						message = message.substring(3+sendToUser.length(),message.length());
						server.sendPrivateMessage(sendToUser,"Private message from "+username+":"+message);
					}
					else{
						output.writeUTF("That user does not exist");
					}
				}
				else{
					server.sendMsgToAll(username + ": " + message);
				}
				
			}

		} catch (IOException ioe) {
			ioe.printStackTrace();

		} finally {
			// Om inte detta görs kan det bli massa döda trådar kvar
			server.removeThread(this);
		}

	}
}