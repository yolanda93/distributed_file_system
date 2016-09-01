// Clase de servidor que implementa el servicio DFS

package dfs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Map;

public class DFSServicioImpl extends UnicastRemoteObject implements DFSServicio {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DFSDir = "DFSDir/";
	//Contendor que relaciona la referencia al servicio con el fichero.
    // esa referencia es la que le damos al cliente.
	private Map<String, DFSFicheroServImpl> almacenReferencias = new HashMap<String,DFSFicheroServImpl>();
	
	public DFSServicioImpl() throws RemoteException {
    }

	@Override
	public synchronized FicheroInfo crearReferencia(String nombre, String modo) throws RemoteException {
		DFSFicheroServImpl referencia = null;
		String location = DFSDir + nombre;
		if (almacenReferencias.containsKey(nombre)) { // entonces le damos la referencia del fichero asociada.
			referencia = almacenReferencias.get(nombre);
		} else {
			try {
				referencia = new DFSFicheroServImpl(nombre, modo,this);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			almacenReferencias.put(nombre, referencia);
		}
		
		File ficheroAux = new File (location);
	      if(!modo.contains("w")&&!ficheroAux.exists())
			try {
				throw new IOException();
			} catch (IOException e) {
				e.printStackTrace();
			}
	      
		long modificacion = ficheroAux.lastModified();
		boolean disponibleCache=false;
		try {
			if(!referencia.hayEscritores()&&!modo.contains("w"))
				disponibleCache = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FicheroInfo respuesta = new FicheroInfo(modificacion, referencia, disponibleCache);
        return respuesta;
	}
	
	
	@Override
	public synchronized void eliminarFichero (String fichero)throws RemoteException{
		
		almacenReferencias.remove(fichero);
		
	}
	
	
	
	
}
