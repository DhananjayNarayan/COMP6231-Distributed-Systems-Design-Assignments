package socketChat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClientSoc extends Thread {
	private Socket socket;

    public ClientSoc (Socket s) throws IOException  {
        this.socket = s;
    }
    
       public static void main(String[] args) {
    	String ip="localhost";
		int port = 9995;
		try (Socket socket = new Socket(ip, port))
		{
            PrintWriter output = new PrintWriter(socket.getOutputStream(),true);   
            Scanner sc = new Scanner(System.in);
            String message;
            String name = null;
            int i=1; //to ask the user to enter message on the console only once. After displaying once, it is incremented. 
            ClientSoc cthr = new ClientSoc(socket);
            cthr.start();
                      
           while(true) {       	 
               if (name==null)
               {
            	   
                    System.out.print("Enter your name: ");                   
                    name=sc.nextLine();
                    output.println(name+ " Joined the Group Chat.");              
               } 
               else {           	                	                     
                    message = sc.nextLine();                 
                    output.println(name + ": " + message);
                    System.out.println();
                    
                    if (message.equals("bye")) // chat window||loop will break if user enters bye
                    {
                    	System.out.println("Connection Closed by Client");
                        break;
                        
                    }
                }
               
               if(i==1) {
           		   System.out.println("Enter Message: \nEnter bye to terminate the chat.");
           		   i+=1;
           	   }
           } 
                
        } catch (Exception e) 
		{
            System.out.println("Connection Closed");
        }
	}
    public void run() 
    {
        
            try 
            {   BufferedReader br = new BufferedReader( new InputStreamReader(socket.getInputStream()));
            
                while(true) 
                {
                    String response = br .readLine();
                    System.out.println(response);
                }
            } catch (IOException e) 
            {
            	System.out.println("Connection Closed");
            } 
     
    }


}


