// Interfaz del API de acceso remoto a un fichero

package dfs;
import java.io.IOException;
import java.rmi.*;

public interface DFSFicheroServ extends Remote  {

abstract public byte[] read (byte[] b) throws IOException;

abstract public void write (byte[] b) throws IOException;

abstract public void seek (long p) throws IOException;

abstract public boolean close (Integer usuarioId) throws IOException;

abstract public void anadirUsuario(Integer usuarioId, String operacion) throws IOException;

abstract public long modificacion() throws IOException;

abstract public boolean hayEscritores()throws IOException;

abstract public void validarCache(Integer usuarioId, String operacion,
		DFSFicheroCallback callback) throws IOException;



}
