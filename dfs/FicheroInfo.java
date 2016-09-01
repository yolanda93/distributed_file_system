// Esta clase representa información de un fichero.
// El enunciado explica más en detalle el posible uso de esta clase.
// Al ser serializable, puede usarse en las transferencias entre cliente
// y servidor.

package dfs;
import java.io.*;

public class FicheroInfo implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4255650063855182381L;
	long modificacion;
	DFSFicheroServ fichero;
	boolean  disponibleCache;
	public FicheroInfo(long modificacion, DFSFicheroServ fichero, boolean disponibleCache){
		this.modificacion=modificacion;
		this.fichero=fichero;
		this.disponibleCache= disponibleCache;
	}
	public long getModificacion() {
		return modificacion;
	}
	public DFSFicheroServ getFichero() {
		return fichero;
	}
	public boolean getDisponibilidadCache() {
		return disponibleCache;
	}
}
