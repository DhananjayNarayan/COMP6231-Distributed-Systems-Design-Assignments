package storage;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import java.util.*;

import common.*;
import rmi.*;
import naming.*;

/** Storage server.

    <p>
    Storage servers respond to client file access requests. The files accessible
    through a storage server are those accessible under a given directory of the
    local filesystem.
 */
public class StorageServer implements Storage, Command
{
	 private File root; 
	 private Skeleton<Storage> sSkeleton;
	 private Skeleton<Command> cSkeleton;
	
    /** Creates a storage server, given a directory on the local filesystem.

        @param root Directory on the local filesystem. The contents of this
                    directory will be accessible through the storage server.
        @throws NullPointerException If <code>root</code> is <code>null</code>.
    */
    public StorageServer(File root)
    {
    	if (root == null) {
			throw new NullPointerException("Root is null");
		}
    	
    	this.root = root;
        sSkeleton = new Skeleton<>(Storage.class, this);
        cSkeleton = new Skeleton<>(Command.class, this);
        
    }

    /** Starts the storage server and registers it with the given naming
        server.

        @param hostname The externally-routable hostname of the local host on
                        which the storage server is running. This is used to
                        ensure that the stub which is provided to the naming
                        server by the <code>start</code> method carries the
                        externally visible hostname or address of this storage
                        server.
        @param naming_server Remote interface for the naming server with which
                             the storage server is to register.
        @throws UnknownHostException If a stub cannot be created for the storage
                                     server because a valid address has not been
                                     assigned.
        @throws FileNotFoundException If the directory with which the server was
                                      created does not exist or is in fact a
                                      file.
        @throws RMIException If the storage server cannot be started, or if it
                             cannot be registered.
     */
    public synchronized void start(String hostname, Registration naming_server)
        throws RMIException, UnknownHostException, FileNotFoundException
    {
    	
    	if (hostname == null ) {
			throw new NullPointerException("Hostname is Null");
		}
    	if (naming_server == null) {
			throw new NullPointerException("NamingServer is Null");
		}
    	
    	sSkeleton.start();
        cSkeleton.start();
        
        Storage sStub = Stub.create(Storage.class, sSkeleton);
		Command cStub = Stub.create(Command.class, cSkeleton);
		
		//Deleting files as per command
		Path[] toDelete = naming_server.register(sStub, cStub, Path.list(root));
		 Arrays.stream(toDelete).forEach(this::delete);
	      deleteEmpty(root); //To Delete all empty folders
           
    }
    
    private void deleteEmpty(File root) {

        if(root.isDirectory()) {
            Arrays.stream(root.listFiles()).forEach(this::deleteEmpty);
            if(root.list().length == 0){
                root.delete();
            }
        }
    }

    /** Stops the storage server.

        <p>
        The server should not be restarted.
     */
    public void stop()
    {
    	
    	if (sSkeleton != null ) {
			sSkeleton.stop();
		
		}

		stopped(null);
        
    }

    /** Called when the storage server has shut down.

        @param cause The cause for the shutdown, if any, or <code>null</code> if
                     the server was shut down by the user's request.
     */
    protected void stopped(Throwable cause)
    {
    }

    
    // The following methods are documented in Storage.java.
    @Override
    public synchronized long size(Path file) throws FileNotFoundException
    {
    	
    	File f = new File(this.root + file.pathName);
    	if (!f.exists()) {
			throw new FileNotFoundException("File does not exist");
		}
    	
    	if (f.isDirectory()) {
			throw new FileNotFoundException("Directory instead of File");
		}
    	
    	return f.length();
        
    }

    
    @Override
    public synchronized byte[] read(Path file, long offset, int length)
        throws FileNotFoundException, IOException
    {
    	File f = new File(this.root + file.pathName);
    	if (!f.exists()) {
			throw new FileNotFoundException("File does not exist");
		}
    	
    	if (f.isDirectory()) {
			throw new FileNotFoundException("Directory instead of File");
		}
    	
    	if (length < 0 || offset < 0) {
			throw new IndexOutOfBoundsException("Invalid Offset values");
			
		}
    	if (offset + length > f.length()) {
			throw new IndexOutOfBoundsException("Invalid Offset values");
		}
    	
    	if (length == 0) {
			return new byte[0];
		}
    	
    	FileInputStream in = new FileInputStream(f);
		byte[] b = new byte[length];
		in.read(b, (int) offset, length);
		
		return b;
       
    }

    @Override
    public synchronized void write(Path file, long offset, byte[] data)
        throws FileNotFoundException, IOException
    {
    	
    	File f = new File(this.root + file.pathName);
    	if (!f.exists()) {
			throw new FileNotFoundException("File does not exist");
		}
    	
    	if (f.isDirectory()) {
			throw new FileNotFoundException("Directory instead of File");
		}
    	
    	if (offset < 0) {
			throw new IndexOutOfBoundsException("Offset less than 0");
		}
    	
    	FileOutputStream out = new FileOutputStream(f);
		FileChannel fc = out.getChannel();
		fc.position(offset);
		fc.write(ByteBuffer.wrap(data));
		out.close();
    }

    // The following methods are documented in Command.java.
    @Override
    public synchronized boolean create(Path file)
    {
    	
    	File f; 
        String[] components;

        
        if (file == null) {

			throw new NullPointerException("Path is null");
		}
       
        if(file.isRoot()) {
            return false;
        }

        f = new File(this.root + file.toString());
        if(f.exists()){
            return false;
        }

        components = file.toString().substring(1).split("/");
        f = this.root;

        for(int i = 0; i < components.length; i++){

           f = new File(f, components[i]);
            if(!f.exists() && i != components.length-1) {
                f.mkdir();
            } 
            else if(i == components.length-1) {
                try {
                    f.createNewFile();
                } catch (IOException e) {
                   System.out.println("Error while creating a new file");
                }
            }
        }

        return true;
       
    }

    @Override
    public synchronized boolean delete(Path path)
    {
    	if (path == null) {

			throw new NullPointerException("Path cannot be null");
		}
    	
    	if(path.isRoot()) 
    		return false; 

        File toDelete = new File(this.root, path.toString());

        if(!toDelete.exists()) {
            return false;
        }
        if(toDelete.isFile()) {
            return toDelete.delete();
        }

        recursiveDelete(toDelete);
        return true;
    }

    /**
     * Deletes a file or folder
     *
     * @param toDelete file/folder to delete
     */

    public void recursiveDelete(File toDelete) {

        for(File file : toDelete.listFiles()) {

            if(file == null) 
            	return;

            if(file.isDirectory()) {
                recursiveDelete(file);
            }
            file.delete();
        }
        toDelete.delete();
    }
       
    
}

