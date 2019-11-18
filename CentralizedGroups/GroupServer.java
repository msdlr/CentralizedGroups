/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Miguel
 */
public class GroupServer extends UnicastRemoteObject implements GroupServerInterface {
    /* ATRIBUTOS */
    //Lista de grupos que maneja
    private LinkedList<ObjectGroup> groupList = new LinkedList();
    //Cerrojos para funciones de grupos
    Lock mutex;
    //Contador para generar identificadores de grupo y usuario
    private int groupCounter = 0;
    
    /* FUNCIONES */
    
    /* CONSTRUCTOR */
    public GroupServer() throws RemoteException {
        //Exportar sevidor
        super();
        
        //Estableer gestor de seguridad
        if (System.getSecurityManager() == null){
                System.setSecurityManager(new SecurityManager());  
        }

        //Inicializar listas
        this.groupList = new LinkedList();
        this.mutex = new ReentrantLock(true); //con true la cola es fifo
    }
    
    /* MAIN */
    public static void main(String args[]) throws RemoteException {
        //Fichero de política
        System.setProperty("java.security.policy", "/home/pwnage/NetBeansProjects/CentralizedGroups/src/CentralizedGroups/seguridad.txt");
        //System.setProperty("java.security.policy", "C:\\Users\\usuario\\Desktop\\CentralizedGroups\\src\\CentralizedGroups\\seguridad.txt");
        GroupServer server = new GroupServer();
       
        //Si no se ejecuta aquí da excepción
        if (System.getSecurityManager() == null){
                System.setSecurityManager(new SecurityManager());  
        }
        
        //Lanzar registro sobre el puerto 1099
        //Inscribir el servidor en el registro
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("GroupServer", server);
            System.out.println("Servidor lanzado correctamente");
        } catch (MalformedURLException e){
            //Si hay error al añadirlo al registro
            System.out.println("Error al hacer rebind");
        } catch (RemoteException ex){
            //Si el puerto ya está ocupado
            System.out.println("Error al lanzar el registro");
        }
    }
    
    /* FUNCIONES DE LA INTERFAZ */
    @Override
    public int createGroup(String galias, String oalias, String ohostname) throws RemoteException {
        /* ENTRADA EN SECCIÓN CRÍTICA */
        this.mutex.lock();
        
        try{
            // Buscar si el grupo solicitado ya existe y devolver error
            if(findGroup(galias) != -1){
                //Si el grupo ya existe se devuelve error
                return -1;
            }
            
            //Generamos un nuevo identificador de grupo y de miembro
            this.groupCounter++;
            //Creamos el nuevo grupo y le ponemos al usuario invocador como miembro
            //El nuevo propietario del grupo es el cliente invocador que se pasa por alias
            ObjectGroup nGroup = new ObjectGroup(galias,groupCounter, oalias, ohostname);
            //No hace falta añadir un nuevo GroupMember al grupo porque ya lo hace el constructor de ObjectGroup
            this.groupList.add(nGroup);
        }
        finally{
            /* SALIDA DE SECCIÓN CRÍTICA */
            this.mutex.unlock();
        }
        return 0;
    }

    @Override
    public int findGroup(String galias) throws RemoteException {
        //BLoqueo para acceso concurrente
        this.mutex.lock();
        
        try{
            //Buscamos el grupo con ese galias
            for(ObjectGroup OG : this.groupList){
                if(OG.galias.equals(galias)) return OG.gid;
            }
            //Si el grupo no existe devuelve -1
            return -1;
        }
        finally{
            this.mutex.unlock();
        }
        
    }

    @Override
    public String findGroup(int gid) throws RemoteException {
        this.mutex.lock();
        try{
            for(ObjectGroup OG : this.groupList){
                if (OG.gid == gid) return OG.galias;
            }
            return null;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public boolean removeGroup(String galias, String oalias) throws RemoteException {
        this.mutex.lock();
        try{
            //Buscamos grupo
            ObjectGroup grupo = null;
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)) grupo = OG;
            }
            //Si el grupo no existe devolvemos false
            if(grupo == null) return false;
            
            //Si no es el dueño
            if(!grupo.oalias.equals(oalias)) return false;
            //Si es el dueño
            else{
                this.groupList.remove(grupo); // surround with unlock/lock
                return true;
            }
            
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public GroupMember addMember(String galias, String alias, String hostname) throws RemoteException {
        this.mutex.lock();
        
        try{
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)){
                    //Comprobamos si el usuario está en el grupo
                    if( isMember(galias, alias) != null) return null;
                    
                    //Si no está, lo añadimos
                    this.mutex.unlock();
                    return OG.addMember(alias);
                }
            }
        }
        finally{
            try{
            this.mutex.unlock();  //was unlock
            } catch (IllegalMonitorStateException e) {}
        }
        return null;
    }

    @Override
    public boolean removeMember(String galias, String alias) throws RemoteException {
        this.mutex.lock();
        try{
            //Buscamos grupo
            ObjectGroup grupo = null;
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)) grupo = OG;
            }
            //Si el grupo no existe devolvemos false
            if(grupo == null) return false;
            
            //Si es el dueño
            if(grupo.oalias.equals(alias)) return false;
            //Si no es el dueño
            else{
                for(GroupMember GM : grupo.members){
                    if(GM.alias.equals(alias)){
                        this.mutex.unlock();
                        return grupo.removeMember(alias);
                        
                    }
                }
            }
            return false;
        }
        
        finally{
            try {
                this.mutex.unlock();
            } catch (IllegalMonitorStateException e) {}
        }
    }

    @Override
    public GroupMember isMember(String galias, String alias) throws RemoteException {
        this.mutex.lock();
        try{
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)){
                    return OG.isMember(alias);
                }
            }
        return null;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public boolean StopMembers(String galias) throws RemoteException {
        this.mutex.lock();
        try{
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)){
                    OG.StopMembers();
                    return true;
                }
            }
            return false;
        }
        finally{
            this.mutex.unlock();
        }
    }
    
    @Override
    public boolean AllowMembers(String galias) throws RemoteException {
        this.mutex.lock();
        try{
            for( ObjectGroup OG : this.groupList ){
                if(OG.galias.equals(galias)){
                    OG.AllowMembers();
                    return true;
                }
            }
        return false;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public LinkedList<String> ListMembers(String galias) throws RemoteException {
        this.mutex.lock();
        try{
            for(ObjectGroup OG : this.groupList){
                if(OG.galias.equals(galias)){
                    //recorremos la lista de miembros
                    //this.mutex.unlock();
                    return OG.ListMembers();
                }
            }
            return null;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public LinkedList<String> ListGroup() throws RemoteException {
        LinkedList grupos = new LinkedList<String>();
        
        for(ObjectGroup OG : this.groupList){
            grupos.add(OG.galias);
        }
        return grupos;
    }
}