// Interfaz del servicio de callback de DFS

package dfs;
import java.io.IOException;
import java.rmi.*;

public interface DFSFicheroCallback extends Remote  {

	



	void invalidarCache() throws IOException;
}
