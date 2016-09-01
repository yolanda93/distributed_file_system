// Clase de servidor que implementa el API de acceso remoto a un fichero

package dfs;
import java.rmi.*;
import java.rmi.server.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.*;

public class DFSFicheroServImpl extends UnicastRemoteObject implements DFSFicheroServ {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String DFSDir = "DFSDir/";
    
    private RandomAccessFile fichero;
    private String location;
    private String accessMode;
    DFSServicio servicio;
    private String nombre;
    // listas de los clientes read y write.
	List<Integer> lectores = new ArrayList<Integer>();
	List<Integer> escritores = new ArrayList<Integer>();
	//lista de clientes que tienen abierto el fichero y estan usando cache sobre el mismo. 
	List<DFSFicheroCallback> usandoCache = new ArrayList<DFSFicheroCallback>();
	
    public DFSFicheroServImpl(String location, String accessMode,DFSServicio servicio) throws RemoteException, FileNotFoundException {
        this.nombre=location;
        this.servicio=servicio;
    	this.location = DFSDir + location;
    	this.accessMode = accessMode;
    	this.fichero = new RandomAccessFile(this.location, this.accessMode); //Primer campo direccion segundo campo permisos

        	
    }
    
    @Override
    public synchronized boolean hayEscritores() throws IOException{
    	return !escritores.isEmpty();
    }
    
    
    

    @Override
    public synchronized void anadirUsuario( Integer usuarioId,String operacion) throws IOException{
    	if(operacion.contains("w") && !escritores.contains(usuarioId))
    		escritores.add(usuarioId);
    	if(operacion.contains("r")&& !lectores.contains(usuarioId))
    		lectores.add(usuarioId);
    }
    
    @Override
    public synchronized void validarCache( Integer usuarioId,String operacion,DFSFicheroCallback cliente) throws IOException{
    	if(operacion.contains("w")){
    		if(escritores.isEmpty()&&lectores.isEmpty())
    			usandoCache.add(cliente);
    		//hay conflicto
    		else{ //le decimos a los clientes de ese fichero que invaliden la cache. Llega un escritor y solo hay lectores

    			for(int i=0;i< usandoCache.size();i++){
    					usandoCache.get(i).invalidarCache();
    			}
    			cliente.invalidarCache(); // se le notifica al cliente que no puede usar cache y la invalida
    			usandoCache.clear();
    		}	   		
    	}else{ // operacion read
    		if(escritores.isEmpty()){
    			usandoCache.add(cliente);
    		}else{
    			for(int i=0;i< usandoCache.size();i++){
					usandoCache.get(i).invalidarCache();
			}
			cliente.invalidarCache(); // se le notifica al cliente que no puede usar cache y la invalida
			usandoCache.clear();
    		}
    	
    	}
    	this.anadirUsuario(usuarioId, operacion);

    }

	@Override
	public synchronized byte[] read(byte [] b) throws IOException{
    // Con cache: Solo le llegan las que conlleven fallo de cache
	
		int i = 0;
		try{ 
		for(i = 0; i < b.length ;i++) 
		 b[i] = fichero.readByte();		 
		}
		catch(EOFException e)
		{
		  
		  if(i == 0)
 		  b = null;
		  else
		  {
		  byte[] a = new byte[i];
		  for(int j = 0; j < i;j++) 
				 a[j] = b[j];
		  b = a;
		  }
		}

		return b;
	}

	@Override
	public synchronized void write(byte [] b) throws IOException{

			fichero.write(b);
	}

	@Override
	public synchronized void seek(long p) throws IOException{

			fichero.seek(p);
	}
	
	@Override
	public synchronized boolean close(Integer usuarioId) throws IOException{

		if(lectores.contains(usuarioId))
			lectores.remove(usuarioId);
		else
		   escritores.remove(usuarioId);

		if(escritores.isEmpty()&&lectores.isEmpty()){
		fichero.close();		
	    servicio.eliminarFichero(nombre);
		
		return false;
		}
		return true;
	}
	

	@Override
	public synchronized long modificacion() throws IOException{
	
		File ficheroAux = new File (this.location);
		long modificacion = ficheroAux.lastModified();
		return modificacion;
	}
	
	

}
