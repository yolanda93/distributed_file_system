// Interfaz del servicio DFS

package dfs;
import java.rmi.*;

public interface DFSServicio extends Remote {
	FicheroInfo  crearReferencia(String nombre, String modo) throws RemoteException;
	void eliminarFichero (String fichero)throws RemoteException;
}       
