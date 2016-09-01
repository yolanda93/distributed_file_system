// Clase de cliente que proporciona acceso al servicio DFS

package dfs;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

public class DFSCliente {
	//Obtenemos variables de entorno

	private String iPHost; 
	private String portHost;
    private DFSServicio servicio;
    int tamBloque;
    int tamCache;
    Map<String,Cache> almacenCache = new HashMap<String,Cache>();
    
    

	
	public DFSCliente(int tamBloque, int tamCache) {
		this.iPHost = System.getenv("SERVIDOR");
		this.portHost = System.getenv("PUERTO");
		this.tamBloque=tamBloque;
		this.tamCache=tamCache;
		
		
			try
			{
				servicio = (DFSServicio) Naming.lookup("//" + iPHost + ":" + portHost + "/DFS");
			}
			catch (RemoteException e) {
				System.err.println("Error de comunicacion: " + e.toString());
			}
			catch (Exception e) {
				System.err.println("Excepcion en DFSCliente:");
				e.printStackTrace();

			}
		
	}
	

	public DFSServicio getServicio(){
		return servicio;
		
	}


	public int getTamBloque() {
		return tamBloque;
	}


	public int getTamCache() {
		return tamCache;
	}


	public Cache getAlmacenCache(String fichero) {
		 Cache res;
		 if(almacenCache.containsKey(fichero)){// si lo contiene entonces obtenemos el valor asociado
			 res = almacenCache.get(fichero);
		 }else{ // Si no lo contiene creamos una cache y la insertamos.
			 res= new Cache (tamCache);
			 almacenCache.put(fichero,res);
		 }
		
		return res;		 
	}



	
	
	
	
}

