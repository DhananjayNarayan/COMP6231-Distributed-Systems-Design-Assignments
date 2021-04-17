package naming;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import rmi.*;
import common.*;
import storage.*;

/** Naming server.

    <p>
    Each instance of the filesystem is centered on a single naming server. The
    naming server maintains the filesystem directory tree. It does not store any
    file data - this is done by separate storage servers. The primary purpose of
    the naming server is to map each file name (path) to the storage server
    which hosts the file's contents.

    <p>
    The naming server provides two interfaces, <code>Service</code> and
    <code>Registration</code>, which are accessible through RMI. Storage servers
    use the <code>Registration</code> interface to inform the naming server of
    their existence. Clients use the <code>Service</code> interface to perform
    most filesystem operations. The documentation accompanying these interfaces
    provides details on the methods supported.

    <p>
    Stubs for accessing the naming server must typically be created by directly
    specifying the remote network address. To make this possible, the client and
    registration interfaces are available at well-known ports defined in
    <code>NamingStubs</code>.
 */
public class NamingServer implements Service, Registration
{
	
	 Skeleton<Service> sSkeleton;
	 Skeleton<Registration> rSkeleton;
	 static Set<Storage> storageStubs;
	 static Set<Command> commandStubs;
	 static DirectoryMap directoryMap;
    /** Creates the naming server object.

        <p>
        The naming server is not started.
     */
    public NamingServer()
    {
    	 storageStubs = new HashSet<>();
         commandStubs = new HashSet<>();
         directoryMap = new DirectoryMap();
    }

    /** Starts the naming server.

        <p>
        After this method is called, it is possible to access the client and
        registration interfaces of the naming server remotely.

        @throws RMIException If either of the two skeletons, for the client or
                             registration server interfaces, could not be
                             started. The user should not attempt to start the
                             server again if an exception occurs.
     */
    public synchronized void start() throws RMIException
    {
   	
        sSkeleton = new Skeleton<>(
                Service.class, new NamingServer(), new InetSocketAddress("127.0.0.1", NamingStubs.SERVICE_PORT));
        
        rSkeleton = new Skeleton<>(
                Registration.class, new NamingServer(), new InetSocketAddress("127.0.0.1", NamingStubs.REGISTRATION_PORT));

        //Start this skeletons
        sSkeleton.start();
        rSkeleton.start();
    }

    /** Stops the naming server.

        <p>
        This method waits for both the client and registration interface
        skeletons to stop. It attempts to interrupt as many of the threads that
        are executing naming server code as possible. After this method is
        called, the naming server is no longer accessible remotely. The naming
        server should not be restarted.
     */
    public void stop()
    {
    	 sSkeleton.stop();
         rSkeleton.stop();
         stopped(null);
    }

    /** Indicates that the server has completely shut down.

        <p>
        This method should be overridden for error reporting and application
        exit purposes. The default implementation does nothing.

        @param cause The cause for the shutdown, or <code>null</code> if the
                     shutdown was by explicit user request.
     */
    protected void stopped(Throwable cause)
    {
    }
    
    
    // The following methods are documented in Service.java.
    @Override
    public boolean isDirectory(Path path) throws FileNotFoundException
    {
    	if (path == null) {
            throw new NullPointerException("Path is null");
        }
        if (!directoryMap.ifPathExists(path)) {
            throw new FileNotFoundException("Directory does not exists");
        }

        return directoryMap.ifIsFolder(path);
    }

    @Override
    public String[] list(Path directory) throws FileNotFoundException
    {
    	if (directory == null) {
            throw new NullPointerException("Directory is null");
        }
        if (!directoryMap.ifPathExists(directory)) {
            throw new FileNotFoundException("Directory not available");
        }
        if (!directoryMap.ifIsFolder(directory)) {
            throw new FileNotFoundException("It is not a directory");
        }

        return directoryMap.list(directory);
    }


    @Override
    public boolean createFile(Path file)
        throws RMIException, FileNotFoundException
    {
    	if (file == null) {
            throw new NullPointerException("File is Null");
        }
    	
    	if (!directoryMap.ifParentExists(file)) {
            throw new FileNotFoundException("Parent Directory doesn't exist");
        }
        if (directoryMap.ifPathExists(file)) {
            return false;
        }
        commandStubs.iterator().next().create(file);
        return directoryMap.addPath(file, storageStubs.iterator().next(), commandStubs.iterator().next());
        
    }

    @Override
    public boolean createDirectory(Path directory) throws FileNotFoundException
    {
    	if (directory == null) {
            throw new NullPointerException("Directory is null");
        }
        if (directoryMap.ifPathExists(directory)) {
            return false;
        }
        if (!directoryMap.ifParentExists(directory)) {
            throw new FileNotFoundException("Parent Directory doesn't exist");
        }
        return directoryMap.addPathDirectory(directory, storageStubs.iterator().next(), commandStubs.iterator().next());
    }

    @Override
    public boolean delete(Path path) throws FileNotFoundException
    {
    	if(path == null) {
            throw new NullPointerException("Path is null");
        }
        if(!directoryMap.ifPathExists(path)) {
            throw new FileNotFoundException("Directory doesn't exists");
        }

        try {
            directoryMap.getCommandStub(path).delete(path);
            directoryMap.deletePath(path);
            return true;
        } catch (RMIException e) {
            System.out.println("Could not delete");
            return false;
        }
    }

    @Override
    public Storage getStorage(Path file) throws FileNotFoundException //easy
    {
    	 if (file == null) {
             throw new NullPointerException("File is null");
         }
         if (!directoryMap.ifPathExists(file)) {
             throw new FileNotFoundException("Directory doesn't exists");
         }
         if (directoryMap.ifIsFolder(file)) {
             throw new FileNotFoundException("No storage stub found");
         }

         return directoryMap.getStorageStub(file);
    }

    // The method register is documented in Registration.java.
    @Override
    public Path[] register(Storage client_stub, Command command_stub,
                           Path[] files)
    {
    	 if (client_stub == null || command_stub == null || files == null) {
             throw new NullPointerException("Null Parameters");
         }
         if (storageStubs.contains(client_stub) || commandStubs.contains(command_stub)) {
             throw new IllegalStateException("Storage server has already been registered");
         }
         
         storageStubs.add(client_stub);
         commandStubs.add(command_stub);
         
         Path[] toDelete = Arrays.stream(files)
                 .filter(file -> !file.toString().equals("/"))
                 .filter(file -> directoryMap.ifPathExists(file)).toArray(Path[]::new);;

                 /*
                  * Remove the paths to be deleted first.
                  *  then register the path by adding to directoryMap
                  */
         Arrays.stream(files)
                 .filter(file -> Arrays.stream(toDelete).noneMatch(toDelFile -> toDelFile == file))
                 .forEach(file -> directoryMap.addPath(file, client_stub, command_stub));

         return toDelete;
    }
    
    private class DirectoryMap {


        Map<String, DirectoryMap> current;
 
        private boolean isFolder;

        private Map<String, Storage> storageStubMap;

        private Map<String, Command> commandStubMap;

        public DirectoryMap() {
            current = new HashMap<>();
            isFolder = true;
            storageStubMap = new HashMap<>();
            commandStubMap = new HashMap<>();
        }


        public boolean addPath(Path path, Storage storageStub, Command commandStub) {

          
            String[] paths;
            Map<String, DirectoryMap> traverse;

            if (path.toString().equals("/")) {
            	return false;
            }
            
            paths = getPathComponents(path);
            traverse = current;        

            for (int i = 0; i < paths.length - 1; i++) {
                if (!traverse.containsKey(paths[i])) {
                    traverse.put(paths[i], new DirectoryMap());
                }
                traverse = traverse.get(paths[i]).current;
            }

            traverse.put(paths[paths.length - 1], new DirectoryMap());           
            traverse.get(paths[paths.length - 1]).isFolder = false;       
            traverse.get(paths[paths.length - 1]).storageStubMap.put(paths[paths.length - 1], storageStub);
            traverse.get(paths[paths.length - 1]).commandStubMap.put(paths[paths.length - 1], commandStub);

            return true;
        }

       
        public boolean addPathDirectory(Path path, Storage storageStub, Command commandStub) {

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length - 1; i++) {
                if (!traverse.containsKey(paths[i])) {
                    traverse.put(paths[i], new DirectoryMap());
                }
                traverse = traverse.get(paths[i]).current;
            }

            traverse.put(paths[paths.length - 1], new DirectoryMap());
            traverse.get(paths[paths.length - 1]).isFolder = true;
            traverse.get(paths[paths.length - 1]).storageStubMap.put(paths[paths.length - 1], storageStub);
            traverse.get(paths[paths.length - 1]).commandStubMap.put(paths[paths.length - 1], commandStub);

            return true;
        }

 
        public boolean ifPathExists(Path path) {

            if (path.toString().equals("/")) return true;

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length; i++) {
                if (!traverse.containsKey(paths[i])) return false;
                traverse = traverse.get(paths[i]).current;
            }
            return true;
        }

      
        private boolean ifParentExists(Path path) {

            String[] paths = getPathComponents(path);
            Path pathTemp = new Path("/");

            for (int i = 0; i < paths.length - 1; i++) {
                pathTemp = new Path(pathTemp, paths[i]);
            }

            return ifPathExists(pathTemp) && ifIsFolder(pathTemp);
        }

 
        public boolean ifIsFolder(Path path) {

            if (path.toString().equals("/")) return true;

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length - 1; i++) {
                traverse = traverse.get(paths[i]).current;
            }

            return traverse.get(paths[paths.length - 1]).isFolder;
        }

 
        public String[] list(Path path) {

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length; i++) {
                traverse = traverse.get(paths[i]).current;
            }

            return traverse.keySet().stream().toArray(String[]::new);
        }

        
        public Storage getStorageStub(Path path) {

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length - 1; i++) {
                traverse = traverse.get(paths[i]).current;
            }

            return traverse.get(paths[paths.length - 1]).storageStubMap.get(paths[paths.length - 1]);
        }

        public Command getCommandStub(Path path) {

            String[] paths =getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length - 1; i++) {
                traverse = traverse.get(paths[i]).current;
            }

            return traverse.get(paths[paths.length - 1]).commandStubMap.get(paths[paths.length - 1]);
        }

        public boolean deletePath(Path path) {

            String[] paths = getPathComponents(path);
            Map<String, DirectoryMap> traverse = current;

            for (int i = 0; i < paths.length - 1; i++) {
                traverse = traverse.get(paths[i]).current;
            }

            return traverse.remove(paths[paths.length - 1]) != null;
        }
        
        public String[] getPathComponents(Path path) {

          
            String[] components;

            components = Arrays
                    .stream(path.toString().split("/"))
                    .filter(e -> e.length() > 0)
                    .toArray(String[]::new);
     
            if(components.length == 0) {
                return new String[]{};
            }

            return components;
        }
    }
}
