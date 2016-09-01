# distributed_file_system
This repository contains the implementation of a distributed file system in Java

The repository is organized in 3 directories:  cliente, servidor y dfs.
The operations allowed are read, write and seek.


* DFSCliente: It provides access to the service. 
* DFSFicheroCliente: API of the DFS service. Each instance of this class represents a remote file accessed by the application.
* DFSServicio y DFSServicioImpl: Remote interface and implementation, offers in the server the DFS service.
* DFSFicheroServ y DFSFicheroServImpl:  Remote interface and implementation,  it offers in the server the API to access remote files.
* Bloque: It represents the fragment of a file.
* Cache: It implements a LRU caché. 
* FicheroInfo: It contains the information of file
* DFSFicheroCallback y DFSFicheroCallbackImpl: Remote interface and implementation, it offers in the client the callback service necessary to implement the coherence protocol.
