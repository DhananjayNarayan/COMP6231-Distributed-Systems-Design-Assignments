package common;

import java.io.*;
import java.util.*;

/** Distributed filesystem paths.

DSD Assignment 3
@author Dhananjay 
    <p>
    Objects of type <code>Path</code> are used by all filesystem interfaces.
    Path objects are immutable.

    <p>
    The string representation of paths is a forward-slash-delimeted sequence of
    path components. The root directory is represented as a single forward
    slash.

    <p>
    The colon (<code>:</code>) and forward slash (<code>/</code>) characters are
    not permitted within path components. The forward slash is the delimeter,
    and the colon is reserved as a delimeter for application use.
 */
public class Path implements Iterable<String>, Serializable
{
	public static String root= "/";
    public String pathName  ;
    /** Creates a new path which represents the root directory. */
    public Path()
    {   
    	this.pathName = root;
    	
    }

    /** Creates a new path by appending the given component to an existing path.

        @param path The existing path.
        @param component The new component.
        @throws IllegalArgumentException If <code>component</code> includes the
                                         separator, a colon, or
                                         <code>component</code> is the empty
                                         string.
    */
    public Path(Path path, String component)
    {
    	if (component.isEmpty()) {
    		throw new IllegalArgumentException("Empty Component string");
    	}
        
        if (component.contains(":")) {
        	throw new IllegalArgumentException("Component contains colon");
    	}

         if (component.contains("/")) {
        	 
        	 throw new IllegalArgumentException("Component has forward slash");	
}
         
         if(path.pathName == null) {
             this.pathName = root.concat(component);
         }

         if(path.pathName.endsWith("/")) {
             pathName = path.pathName + component;
         } else {
             pathName = path.pathName + "/" + component;
         }
         
    }

    /** Creates a new path from a path string.

        <p>
        The string is a sequence of components delimited with forward slashes.
        Empty components are dropped. The string must begin with a forward
        slash.

        @param path The path string.
        @throws IllegalArgumentException If the path string does not begin with
                                         a forward slash, or if the path
                                         contains a colon character.
     */
    public Path(String path)
    {
    	
    	if(path.equals("")) {
            throw new IllegalArgumentException("Empty Path");
        }
        if(!path.startsWith("/")) {
            throw new IllegalArgumentException("Invalid start for path");
        }
        if(path.contains(":")) {
            throw new IllegalArgumentException("Path contains Colon");
        }
       
        this.pathName = "";
        for(String component : path.split("/")) {
            if(!component.trim().equals("")){
                pathName = pathName.concat("/");
                pathName = pathName.concat(component.trim());
            }
        }
        if(pathName.equals("")) {
            pathName = "/";
        }
    }

    /** Returns an iterator over the components of the path.

        <p>
        The iterator cannot be used to modify the path object - the
        <code>remove</code> method is not supported.

        @return The iterator.
     */
    @Override
    public Iterator<String> iterator()
    {
    	List<String> components = new ArrayList<>();
    	myIterator myIterator;
        Arrays.stream(
                pathName.split("/")).
                filter(e -> !e.equals("")).
                forEach(e -> components.add(e.trim()));

        myIterator= new myIterator(components.iterator());
        return myIterator;

    }
    
    private class myIterator implements Iterator<String> {

		private Iterator<String> i;

		public myIterator(Iterator<String> i) {
			this.i = i;
		}

		@Override
		public boolean hasNext() {
			return this.i.hasNext();
		}

		@Override
		public String next() {
			return this.i.next();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

	}
/////////////////////////////////////////////////////////////////////
    
    /** Lists the paths of all files in a directory tree on the local
        filesystem.

        @param directory The root directory of the directory tree.
        @return An array of relative paths, one for each file in the directory
                tree.
        @throws FileNotFoundException If the root directory does not exist.
        @throws IllegalArgumentException If <code>directory</code> exists but
                                         does not refer to a directory.
     */
    public static Path[] list(File directory) throws FileNotFoundException
    {
    	  if(!directory.exists()) {
              throw new FileNotFoundException("Directory does not exist");
          }
          if(!directory.isDirectory()) {
              throw new IllegalArgumentException("Not a directory");
          }
          
          return getPaths(directory, new ArrayList<Path>(), directory.getAbsolutePath().length());
      
    }
    
    public static Path[] getPaths(File directory, List<Path> paths, int rootPathLength){

        for(File file : directory.listFiles()) {

            if(file.isDirectory()) {
                getPaths(file, paths, rootPathLength);
            } else {
                String filePath = file.getAbsolutePath().substring(rootPathLength);
                filePath = filePath.replaceAll("\\\\", "/");
                paths.add(new Path(filePath));
            }
        }

        return paths.toArray(new Path[0]);
    }
    

    /** Determines whether the path represents the root directory.

        @return <code>true</code> if the path does represent the root directory,
                and <code>false</code> if it does not.
     */
    public boolean isRoot()
    {
    	if (this.pathName.equals("/")) {
			return true;
		}
    	else {
		return false;
    	}
    }

    /** Returns the path to the parent of this path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no parent.
     */
    public Path parent()
    {
    	 if(this.pathName.equals(root)) {
             throw new IllegalArgumentException("Path is root and not a parent");
         }

         String parentPath = "";
         String[] components = this.pathName.split("/");
         for(int i = 0 ; i < components.length - 1 ; i++) {

             parentPath = parentPath + "/" + components[i].trim();
         }

         return new Path(parentPath);
    }

    /** Returns the last component in the path.

        @throws IllegalArgumentException If the path represents the root
                                         directory, and therefore has no last
                                         component.
     */
    public String last()
    {
    	 if(this.pathName.equals(root)) {
             throw new IllegalArgumentException("Path is root and not a parent");
         }
    	 
    	 String[] components = this.pathName.split("/");
    	 String lastComponent = components[components.length-1].trim();
         return lastComponent;
    }

    /** Determines if the given path is a subpath of this path.

        <p>
        The other path is a subpath of this path if is a prefix of this path.
        Note that by this definition, each path is a subpath of itself.

        @param other The path to be tested.
        @return <code>true</code> If and only if the other path is a subpath of
                this path.
     */
    public boolean isSubpath(Path other)
    {
    	if(this.pathName.contains(other.pathName)) {
    	return true;
    	}
    	else {
    		return false;
    	}
    }

    /** Converts the path to <code>File</code> object.

        @param root The resulting <code>File</code> object is created relative
                    to this directory.
        @return The <code>File</code> object.
     */
    public File toFile(File root)
    {
    	return new File(root.getPath());
    }

    /** Compares two paths for equality.

        <p>
        Two paths are equal if they share all the same components.

        @param other The other path.
        @return <code>true</code> if and only if the two paths are equal.
     */
    @Override
    public boolean equals(Object other)
    {
    	if(this.pathName.equals(((Path)other).pathName)) {
    		return true;
    	}
    	else {
    		return false;
    	}
    }

    /** Returns the hash code of the path. */
    @Override
    public int hashCode()
    {
    	return this.pathName.hashCode();
    }

    /** Converts the path to a string.

        <p>
        The string may later be used as an argument to the
        <code>Path(String)</code> constructor.

        @return The string representation of the path.
     */
    @Override
    public String toString()
    {
    	return pathName;
    }
}
