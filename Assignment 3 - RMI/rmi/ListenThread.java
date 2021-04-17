package rmi;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ListenThread extends Thread {

   
    ServerSocket ss;
    private boolean run;

    public ListenThread(ServerSocket serverSocket) {

        this.ss = serverSocket;
    }

    @Override
    public void run() {

        run = true;

        while(run){
            Socket socket;
            try {               
                socket = ss.accept();
                System.out.println("New connection from " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                //System.out.println("Exception as Cannot Connect to Socket(Closed or Not accepting)");
            	
            }
        }
    }
 
    public void stopThread() {

        if(!(ss == null) && (!(ss.isClosed()))) {
        	try {
                ss.close();
                run = false;
            } catch (IOException e) {
                System.out.println("IOException when trying to close Server Socket");
                e.printStackTrace();
            }
        }            
    }
}