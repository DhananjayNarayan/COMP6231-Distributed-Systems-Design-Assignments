package rmi;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.util.Objects;
import java.lang.reflect.Proxy;
/** RMI stub factory.

    <p>
    RMI stubs hide network communication with the remote server and provide a
    simple object-like interface to their users. This class provides methods for
    creating stub objects dynamically, when given pre-defined interfaces.

    <p>
    The network address of the remote server is set when a stub is created, and
    may not be modified afterwards. Two stubs are equal if they implement the
    same interface and carry the same remote server address - and would
    therefore connect to the same skeleton. Stubs are serializable.
 */
public abstract class Stub
{
    /** Creates a stub, given a skeleton with an assigned adress.

        <p>
        The stub is assigned the address of the skeleton. The skeleton must
        either have been created with a fixed address, or else it must have
        already been started.

        <p>
        This method should be used when the stub is created together with the
        skeleton. The stub may then be transmitted over the network to enable
        communication with the skeleton.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose network address is to be used.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned an
                                      address by the user and has not yet been
                                      started.
        @throws UnknownHostException When the skeleton address is a wildcard and
                                     a port is assigned, but no address can be
                                     found for the local host.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton) throws UnknownHostException
    {
    	    			

		if (c == null || skeleton == null) {
			throw new NullPointerException("Interface or the skeleton is Null");
		}
		
		
		if(!skeleton.ServerRunning()) {
            throw new IllegalStateException("Server is not running");
        }
		
		 ifRemoteInterfaceCheck(c);
		
		 InetSocketAddress address = skeleton.getAddress();   

			if (address == null) {
				throw new NullPointerException("Address is Null");
			}
	        try(Socket socket = new Socket(address.getHostName(), address.getPort())) {

	        } catch(UnknownHostException e) {
	            throw new UnknownHostException("Host address cannot be determined");
	        } catch (IOException e) {
	            System.out.println("IOException in Stub create");
	        
	        }
	         if (address.isUnresolved()) {
				throw new UnknownHostException();
			}		 		
		
		StubProxyHandler p= new StubProxyHandler(skeleton.getServer(), skeleton);
		T newStub = (T)Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, p);

	        return newStub;     
    }
    
    private static<T> void ifRemoteInterfaceCheck(Class<T> c) {

        Method[] methods = c.getDeclaredMethods();

        for(Method method : methods) {
            Class<?>[] exceptions = method.getExceptionTypes();
            boolean hasRMIExcept = false;
            for(Class<?> exception : exceptions) {
                if(exception.toString().equals("class rmi.RMIException")) {
                    hasRMIExcept = true;
                    break;
                }
            }
            if(!hasRMIExcept){
                throw new Error("Interface is not a remote interface");
            }
        }
    }

    /** Creates a stub, given a skeleton with an assigned address and a hostname
        which overrides the skeleton's hostname.

        <p>
        The stub is assigned the port of the skeleton and the given hostname.
        The skeleton must either have been started with a fixed port, or else
        it must have been started to receive a system-assigned port, for this
        method to succeed.

        <p>
        This method should be used when the stub is created together with the
        skeleton, but firewalls or private networks prevent the system from
        automatically assigning a valid externally-routable address to the
        skeleton. In this case, the creator of the stub has the option of
        obtaining an externally-routable address by other means, and specifying
        this hostname to this method.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param skeleton The skeleton whose port is to be used.
        @param hostname The hostname with which the stub will be created.
        @return The stub created.
        @throws IllegalStateException If the skeleton has not been assigned a
                                      port.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, Skeleton<T> skeleton,
                               String hostname)
    {
    	InetSocketAddress address = skeleton.getAddress();   
    	if (c == null || skeleton == null) {
			throw new NullPointerException("Interface or the skeleton is Null");
		}
		
		if (address == null) {
			throw new NullPointerException("Address is Null");
		}
		if (hostname == null) {
			throw new NullPointerException("Hostname is null");
		}
		ifRemoteInterfaceCheck(c);
		if((skeleton.getAddress() == null) || skeleton.getAddress().getPort() == 0) {
	            throw new IllegalArgumentException("Skeleton Port Not Assigned");
	        }
		
		StubProxyHandler p= new StubProxyHandler(skeleton.getServer(), skeleton);
		T newStub = (T)Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, p);

	        return newStub;		
    }

    /** Creates a stub, given the address of a remote server.

        <p>
        This method should be used primarily when bootstrapping RMI. In this
        case, the server is already running on a remote host but there is
        not necessarily a direct way to obtain an associated stub.

        @param c A <code>Class</code> object representing the interface
                 implemented by the remote object.
        @param address The network address of the remote skeleton.
        @return The stub created.
        @throws NullPointerException If any argument is <code>null</code>.
        @throws Error If <code>c</code> does not represent a remote interface
                      - an interface in which each method is marked as throwing
                      <code>RMIException</code>, or if an object implementing
                      this interface cannot be dynamically created.
     */
    public static <T> T create(Class<T> c, InetSocketAddress address)
    {
    	
    	if (c == null) {
			throw new NullPointerException("Interface is Null");
		}
		
		if (address == null) {
			throw new NullPointerException("Address is Null");
		}
		 ifRemoteInterfaceCheck(c);
		 
		 T newStub = null;

	        if(!Skeleton.skeletonMap.containsKey(address)) {
	            newStub = (T)Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, new StubProxyHandler());
	        } else {
	            Skeleton skeleton = Skeleton.skeletonMap.get(address);
	            newStub = (T)Proxy.newProxyInstance(c.getClassLoader(), new Class[] {c}, new StubProxyHandler(skeleton.getServer(), skeleton));
	        }

		        return newStub;		        
    }
    
   
	public static class StubProxyHandler implements InvocationHandler {
	 
	    Object c; 
	    Skeleton skeleton; 

	    public StubProxyHandler(){
	      c = null;
	      skeleton = null;
	    }

	    public StubProxyHandler(Object object, Skeleton skeleton){
	        c = object;
	        this.skeleton = skeleton;
	    }
		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if(method.getName().equals("equals")) {
	            return isEqual(args);
	        }
			
	        if(method.getName().equals("hashCode")) {
	            return Objects.hash(skeleton);
	        }
	        if(method.getName().equals("toString")) {
	            return (this.toString());
	        }
	        if (skeleton == null || c == null) {
	            throw new RMIException("No skeleton or caller");
	        }
	        
	        try {
	            return method.invoke(c, args);
	        }
	        catch (InvocationTargetException e) {
	            throw e.getTargetException();
	        }
		}
		
		private boolean isEqual(Object[] args) {	      
	        if(args[0] == null) {
	            return false;
	        }

	        if(args[0].equals(skeleton)) {
	            return true;
	        } else {
	            return false;
	        }
	    }

		
	}
}
