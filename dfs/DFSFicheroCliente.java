// Clase de cliente que proporciona el API del servicio DFS

package dfs;

import java.io.*;
import java.rmi.*;
import java.util.List;


public class DFSFicheroCliente  {

	private DFSFicheroServ fichero;
	private FicheroInfo ficheroAux;
	private DFSCliente cliente;
	private String modo;
	private long posicion;
	private Cache cache;
	private String nombre;
	private long ultimaModificacion;
	private Integer usuarioId;
	private boolean estaAbierto;
	private DFSServicio servicio;
	private boolean disponibilidadCache;
    private DFSFicheroCallback callback;

	public DFSFicheroCliente(DFSCliente dfs, String nom, String modo) 
			throws RemoteException, IOException, FileNotFoundException {

	    this.estaAbierto=true;
	        this.usuarioId=this.hashCode();
		this.cliente = dfs;
		this.modo = modo;
		this.nombre=nom;
		this.servicio=cliente.getServicio();
		this.ficheroAux = servicio.crearReferencia(nom, modo);
		this.fichero = this.ficheroAux.getFichero();
		if(this.fichero==null)
			throw new FileNotFoundException("La fabrica del cliente nos devolvio una referencia nula");
		this.disponibilidadCache=this.ficheroAux.getDisponibilidadCache();
	     
		this.ultimaModificacion = this.ficheroAux.getModificacion();
        this.callback= new DFSFicheroCallbackImpl(this);
		fichero.validarCache(usuarioId, modo,callback);
		
		// le decimos al cliente que puede usar la cache
		if(disponibilidadCache){
			this.cache = this.cliente.getAlmacenCache(nombre); 	
			if(cache.obtenerFecha() < this.ultimaModificacion)
				cache.vaciar(); // no es valida
		}

		posicion = 0;

	  }
	
	


	

	public  synchronized int read(byte[] b) throws RemoteException, IOException {
		if(estaAbierto){
			if(disponibilidadCache){ // preguntamos por cache
				int noBloques=b.length/ cliente.getTamBloque();
				long puntBloque= posicion/cliente.getTamBloque();
				int noBytes=0;
				Bloque bloque;
				byte[] blqLeer= new byte[cliente.getTamBloque()]; // es la estructura con el tamano del bloque que queremos leer.
				for(int i =0; i <noBloques; i++){
					noBytes=i*cliente.getTamBloque(); // noBytes que llevamos leidos hasta el momento.
					if((bloque=cache.getBloque(puntBloque))==null){ 	   // Fallo en cache: Leemos del servicio si tenemos fallo en cache.

						fichero.seek(posicion+noBytes); // movemos el puntero de lectura del servicio.
						byte[] resultado = fichero.read(blqLeer);

						if(resultado == null) // Si hemos llegado a fin de fichero devolvemos -1.
							return -1;
						// Compiamos el bloque en cache 
						bloque = new Bloque(puntBloque,resultado);
						Bloque exp= cache.putBloque(bloque);
						if(exp!=null && cache.preguntarYDesactivarMod(exp)){
							fichero.seek(exp.obtenerId()*cliente.getTamBloque());
							fichero.write(exp.obtenerContenido());
						}
						System.arraycopy(resultado,0, b, noBytes, resultado.length);
						puntBloque++;
					} else{// Acierto en cache.
						System.arraycopy(bloque.obtenerContenido(),0, b, noBytes,bloque.obtenerContenido().length);		
					}

				}
				posicion=posicion+b.length;
			}else{ // no podemos usar cache por tanto hacemos un read normal.
				fichero.seek(posicion);
				byte[] respuesta = fichero.read(b);
				if(respuesta == null)
					return -1;
				System.arraycopy(respuesta, 0, b, 0, respuesta.length);
			}
			return b.length;
		}
		else
		throw new IOException("Tiene que abrir el fichero antes");

	}
	
	public  synchronized void write(byte[] b) throws RemoteException, IOException {
		// Con cache: Solo le llegaran al servidor las correspondientes a la expulsion de la cache de un bloque modificado, asi como los volcados de bloques modificados al cerrar el fichero.
		if(estaAbierto){
			if(disponibilidadCache){ // preguntamos por cache
				int noBloques= b.length/ cliente.getTamBloque();
				int puntBloque= (int)posicion/cliente.getTamBloque();

				if(modo.equals("r"))
					throw new IOException("No se permite escribir en modo readonly");

				for(int i=0; i <  noBloques;i++){

					byte[] bloqueEscr = new byte[cliente.getTamBloque()];
					System.arraycopy(b, i*cliente.getTamBloque(), bloqueEscr, 0, cliente.getTamBloque());
					posicion+=cliente.getTamBloque();
					Bloque bloque= new Bloque(puntBloque,bloqueEscr);
					cache.activarMod(bloque);
					Bloque exp = cache.putBloque(bloque);

					if(exp!=null && cache.preguntarYDesactivarMod(exp)){
						fichero.seek( exp.obtenerId() * cliente.getTamBloque()); // Actualizamos el puntero del servicio
						fichero.write(exp.obtenerContenido());

					}
					puntBloque++;
				}
			}else { // no podemos usar cache por tanto hacemos un read normal.
				fichero.seek(posicion);
				fichero.write(b);
			}
		}
		else
		throw new IOException("Tiene que abrir el fichero antes");

	}

	public  synchronized void seek(long p) throws RemoteException, IOException {
		if(estaAbierto){
			fichero.seek(p);
			posicion = p;
		}
		else
		throw new IOException("Tiene que abrir el fichero antes");

	}

	public  synchronized void close() throws RemoteException, IOException {

	    	if(estaAbierto){
			if(disponibilidadCache)//usamos cache
			{
				long modificacion=0;
				List<Bloque> listaBloques = cache.listaMod();
				for(int i=0;i< listaBloques.size();i++){
					fichero.seek(listaBloques.get(i).obtenerId()* cliente.getTamBloque()); //actualizamos el puntero del servicio.
					fichero.write(listaBloques.get(i).obtenerContenido());
					cache.desactivarMod(listaBloques.get(i));	 
				}
				modificacion=fichero.modificacion();
				cache.fijarFecha(modificacion); //fijamos la fecha de modificacion de la cache.	 
			}
			estaAbierto=fichero.close(usuarioId);
                        estaAbierto=false;
			//if(!estaAbierto) //eliminamos referencia del almacen del servicio
			//	servicio.eliminarFichero(nombre);

			posicion = 0;

				}
		else
		throw new IOException("Tiene que abrir el fichero antes");
	}

	public  synchronized long getPosicion(){
		return posicion;
	}






	public  synchronized void invalidarCache() throws IOException {
		// TODO Auto-generated method stub
		if(cache!=null){
			this.disponibilidadCache=false;
			List<Bloque> listaBloques = cache.listaMod();
			for(int i=0;i< listaBloques.size();i++){
				fichero.seek(listaBloques.get(i).obtenerId()* cliente.getTamBloque()); //actualizamos el puntero del servicio.
				fichero.write(listaBloques.get(i).obtenerContenido());
				cache.desactivarMod(listaBloques.get(i));	 
			}
		}

	}

	

}

