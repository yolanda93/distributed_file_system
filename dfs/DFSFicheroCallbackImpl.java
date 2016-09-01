// Clase de cliente que implementa el servicio de callback de DFS

package dfs;
import java.io.IOException;
import java.rmi.*;
import java.rmi.server.*;

public class DFSFicheroCallbackImpl extends UnicastRemoteObject implements DFSFicheroCallback {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 5367725684668572188L;
	private DFSFicheroCliente cliente;

	public DFSFicheroCallbackImpl(DFSFicheroCliente cliente)throws RemoteException {
		this.cliente = cliente;
    }
	
	@Override
	public void invalidarCache() throws IOException {
		cliente.invalidarCache();
	}
	
	
}
