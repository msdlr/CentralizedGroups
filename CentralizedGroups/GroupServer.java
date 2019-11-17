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
    private int memberCounter = 0;
    
    /* FUNCIONES */
    
    /* CONSTRUCTOR */
    public GroupServer() throws RemoteException {
        //Exportar sevidor
        super();
        
        //Estableer gestor de seguridad
        if (System.getSecurityManager() == null) {
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
        if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());  
        }
        
        //Lanzar registro sobre el puerto 1099
        //Inscribir el servidor en el registro
        try {
            LocateRegistry.createRegistry(1099);
            Naming.rebind("GroupServer", server);
            System.out.println("Servidor lanzado correctamente");
        } catch (MalformedURLException e) {
            //Si hay error al añadirlo al registro
            System.out.println("Error al hacer rebind");
        } catch (RemoteException ex){
            //Si el puerto ya está ocupado
            System.out.println("Error al lanzar el registro");
        }
    }
    
    /* FUNCIONES DE LA INTERFAZ */
    @Override
    public int createGroup(String galias, String oalias, String ohostname) {
        /* ENTRADA EN SECCIÓN CRÍTICA */
        this.mutex.lock();
        
        try{
            // Buscar si el grupo solicitado ya existe y devolver error
            if(findGroup(galias) != -1) {
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
            mutex.unlock();
        }
        return 0;
    }

    @Override
    public int findGroup(String galias) {
        //BLoqueo para acceso concurrente
        this.mutex.lock();
        
        try{
            int iGrupo = gIndex(galias);
            if(iGrupo == -1){
                //Si el grupo no exite se devuelve -1
                return -1;
            }
            else{
                //Si existe se crea
                return this.groupList.get(iGrupo).gid;
            }
        }
        finally{
            this.mutex.unlock();
        }
        
    }

    @Override
    public String findGroup(int gid) {
        this.mutex.lock();
        try{
            int iGrupo = GroupServer.this.gIndex(gid);
            if (iGrupo != -1){
                return this.groupList.get(iGrupo).galias;
            }
            return null;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public boolean removeGroup(String galias, String oalias) {
        this.mutex.lock();
        try{
            //Buscamos grupo y miembro en el grupo
            int iGrupo, iMiembro = 0;
            iGrupo = gIndex(galias);
            //Comprobamos si existe el miembro de ese grupo y buscamos el miembro
            if (iGrupo != -1) iMiembro = mIndex(oalias,galias);
            
            //Si existen el grupo y el miembro
            if (iMiembro != -1){
                //Devolvemos false si el oalias no es el del dueño del grupo
                if( !this.groupList.get(iGrupo).oalias.equals(oalias) )
                    return false;
                else {
                    //Lo borramos y devolvemos true
                    this.groupList.get(iGrupo).members.remove(iMiembro);
                    return true;
                }
            }
            else return false;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public GroupMember addMember(String galias, String alias, String hostname) {
        this.mutex.lock();
        
        try{
            //Buscamos si el grupo existe
            int iGrupo = gIndex(galias);
            
            //Si el grupo no está en la lista
            if(iGrupo == -1) return null;
            //Si el miembro con ese alias ya está en ese grupo
            else if ( this.mIndex(alias, galias) != -1 ){
                return null;
            }
            //Si el grupo existe y el miembro no está en él ya
            return this.groupList.get(iGrupo).addMember(alias);
        }
        finally{
            this.mutex.lock();
        }
    }

    @Override
    public boolean removeMember(String galias, String alias) {
        this.mutex.lock();
        try{
            //Buscamos el grupo y el usuario por alias
            int iGrupo, iMiembro;
            iGrupo = gIndex(galias);
            
            //Si el grupo no existe
            if(iGrupo == -1){
                return false;
            }
            
            //Si existe pero se intenta borrar el dueño
            else if (this.groupList.get(iGrupo).oalias.equals(alias)){
                return false;
            }
            //Si se intenta borrar cualquier otro miembro
            else{
                iMiembro = mIndex(alias,galias);
                if(iMiembro == -1) return false; //Si el usuario no está en este grupo
                boolean remove = this.groupList.get(iGrupo).members.remove( this.groupList.get(gIndex(galias)).members.get(iMiembro) );
                return true;
            }
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public GroupMember isMember(String galias, String alias) {
        this.mutex.lock();
        try{
            //return this.groupList.get(getGroup galias).memberList.contains(this.memberList.get(alias));
            int iGrupo, iMiembro;
            iGrupo = gIndex(galias);

            //Si el grupo no existe
            if(iGrupo == -1) return null;
            //Si el grupo existe buscamos el miembro
            iMiembro = mIndex(alias,galias);
             if(iMiembro == -1) return null;
             
            //Si es miembro se devuelve el GroupMember
            return this.groupList.get(iGrupo).members.get(iMiembro);
            //Si n se devuelve null
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public boolean StopMembers(String galias) {
        this.mutex.lock();
        try{
            int iGrupo = gIndex(galias);
            if(iGrupo == -1){
                //Si el grupo no existe
                return false;
            }
            else{
                //Si el grupo existe
                this.groupList.get(iGrupo).StopMembers();
                return true;
            }
        }
        finally{
            this.mutex.unlock();
        }
    }
    
    @Override
    public boolean AllowMembers(String galias) {
        this.mutex.lock();
        try{
            //Buscamos el grupo con el alias especificado
            int iGrupo = gIndex( galias );
            if(iGrupo == -1){
                //Si el grupo no existe
                return false;
            }
            else{
                //Si el grupo existe
                this.groupList.get(iGrupo).AllowMembers();
                return true;
            }
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public LinkedList<String> ListMembers(String galias) {
        this.mutex.lock();
        try{
            int iGrupo = gIndex(galias);
            LinkedList lista = new LinkedList();

            //Si el grupo no existe
            if(findGroup(galias) == -1){
                return null;
            }
            else{
                //Si el grupo existe, recorremos su lista de miembros
                for(GroupMember gp : this.groupList.get(iGrupo).members) {
                    lista.add(gp.alias);
                }
            }
            return lista;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public LinkedList<String> ListGroup() {
        LinkedList grupos = new LinkedList<String>();
        
        for(ObjectGroup OG : this.groupList){
            grupos.add(OG.galias);
        }
        return grupos;
    }
    
    /* FUNCIONES AUXILIARES */
    
    /*
        Estas funciones buscan grupos y miembros tanto por id como por alias
        Devuelven -1 si no se encuentra; si se encuentra devuelven el índice en las listas
    */
    private int gIndex(int gid){
        for(ObjectGroup OG : this.groupList){
            if(OG.gid == gid) return this.groupList.indexOf(OG);
        }
        return -1;
    }
    
    private int gIndex(String galias){
        for(ObjectGroup OG : this.groupList){
            if(OG.galias.equals(galias)) return this.groupList.indexOf(OG);
        }
        return -1;
    }
    
    private int mIndex(String alias, String galias){
        int groupI = gIndex(galias);
        //Si el grupo no existe
        if(groupI == -1) return -1;
        
        //Buscar miembro si el grupo existe
        for(GroupMember member : this.groupList.get(groupI).members){
            if( member.alias.equals(alias)) return this.groupList.get(groupI).members.indexOf(member);
        }
        return -1;
    }
    
//    private int getMember(int uid){
//        for(GroupMember member : memberList){
//            if(member.uid == uid) return this.memberList.indexOf(member);
//        }
//        return -1;
//    }
}