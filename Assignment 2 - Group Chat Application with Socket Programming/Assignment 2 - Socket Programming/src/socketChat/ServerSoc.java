package socketChat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerSoc extends Thread {
	private Socket s;
    private ArrayList<ServerSoc> threadList;  //ArrayList to store the thread objects
    private PrintWriter out;

    public ServerSoc(Socket socket, ArrayList<ServerSoc> threads) {
        this.s = socket;
        this.threadList = threads;
    }

    public static void main(String[] args) {
		ArrayList<ServerSoc> threadList = new ArrayList<>();
		
		System.out.println("Group Chat Server has started.");		
		System.out.println("Server is waiting for clients to join the Group Chat.");
        try (ServerSocket ss = new ServerSocket(9995)){
            while(true) {
                Socket socket = ss.accept();
                System.out.println("A new Client has connected to server.");
                ServerSoc sthr = new ServerSoc(socket, threadList);
                threadList.add(sthr); 
                sthr.start();
            }
            
        } catch (Exception e) 
        {
            System.out.println("Connection Closed");
        }
	}  
    
    public void run() {
        try {
            BufferedReader br = new BufferedReader( new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(),true);

            while(true) {
                String print = br.readLine();                
                if(print.equals("bye")) {  // chat window||loop will break if user enters bye
                    break;
                }
                broadcast(print);              
            }

        } catch (Exception e) {
            System.out.println("Connection Closed by the Client.");
        }
    }

    private void broadcast(String print) {
        for( ServerSoc th: threadList) {
            th.out.println(print);
        }
    }        
}
