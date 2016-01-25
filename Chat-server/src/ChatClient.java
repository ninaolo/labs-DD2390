import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * En klass för ett användargränssnitt som beskriver en klient till chatt-programmet.
 */
public class ChatClient {

	Socket clientSocket;
	DataInputStream serverInput;
	DataOutputStream output;
	BufferedReader clientInput;
	String username;


	public ChatClient(String username, String serverIP, int port) {
		this.username = username;
		try {

			// Starta upp vår connection
			System.out.println("Trying to connect...");
			clientSocket = new Socket(serverIP, port);
			System.out.println("Successfully connected to port " + clientSocket.getPort() + " on host "
					+ clientSocket.getLocalAddress() + ".");

			// Hämta input och output-strömmar
			serverInput = new DataInputStream(clientSocket.getInputStream());
			output = new DataOutputStream(clientSocket.getOutputStream());
			clientInput = new BufferedReader(new InputStreamReader(System.in));

			// Skickar username på output så ServerThread kan hämta det
			username = this.getValidUserName(username);

			// Starta upp en tråd som lyssnar på meddelanden från servern
			new Thread(new ChatListener()).start();

		} catch (UnknownHostException e) {
			System.out.println("Unknown host");
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}

		while (true) {

			// Väntar på att klienten ska skriva meddelanden i chatten
			try {
				String message = clientInput.readLine()+"";
				output.writeUTF(message);
				if (message.compareTo("leave") == 0){
					System.out.println("Goodbye "+username);
					System.exit(0);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
	}
	private String getValidUserName(String user){
		String valid="";
		user = user.replaceAll("\\s+","");
		try{
			output.writeUTF(user);
			valid = serverInput.readUTF();
			while(valid.compareTo("false")==0){
				System.out.print("Username already taken..., please pick another: ");
				user = clientInput.readLine();
				user = user.replaceAll("\\s+","");
				output.writeUTF(user);
				valid = serverInput.readUTF();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return user;
		
		
	}
	

	/*
	 * En privat klass som gör att vi kan ha en kontinuerlig tråd som kör och
	 * väntar på att meddelanden ska komma från servern.
	 */
	private class ChatListener extends Thread {

		public void run() {

			try {

				while (true) {
					// Hämta meddelanden hela tiden och printa ut
					String message = serverInput.readUTF();
					System.out.println(message);
				}

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/*
	 * 127.0.0.1 är den lokala datorn och args[0] är ett användarnamn som man
	 * får välja
	 */
	public static void main(String[] args) {
		new ChatClient(args[0], "127.0.0.1", 1337);
	}

}
