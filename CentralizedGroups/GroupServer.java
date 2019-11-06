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
    //Lista de miembros
    private LinkedList<GroupMember> memberList = new LinkedList();
    //Cerrojos para funciones de grupos
    Lock mutex = new ReentrantLock();
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
        this.memberList = new LinkedList();
    }
    
    /* MAIN */
    public static void main(String args[]) throws RemoteException {
        //Fichero de política
        System.setProperty("java.security.policy", "C:\\Users\\Usuario\\Desktop\\seguridad.txt");
        GroupServer server = new GroupServer();
       
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
            /* VARIABLES */
            //El campo oid del constructor de ServeGroup es el uid del usuario del que se pasa el hostname
            int nuevoOID = 0;
            int iMiembro;

            /*CÓDIGO*/
            // Buscar si el grupo solicitado ya existe y devolver error
            if(findGroup(galias) >= 0) return -1;
            
            //Encontramos el miembro con el alias que se pasa por parámetro
            iMiembro = mIndex(oalias);
            
            //Si no existe el miembro se devuelve -1
            if(iMiembro == -1){
                nuevoOID = this.memberList.get(iMiembro).uid;
            } else return -1;
            
            //Generamos un nuevo identificador de grupo
            groupCounter++;
            //Creamos el nuevo grupo
            this.groupList.add(new ObjectGroup(galias,groupCounter, oalias, nuevoOID));
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
                return -1;
            }
            else{
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
        boolean correcto=false;
        this.mutex.lock();
        try{
            int iGrupo, iMiembro;
            iGrupo = gIndex(galias);
            iMiembro = mIndex(oalias);
            if (iGrupo != -1 && iMiembro != -1){
                //Si existen el grupo y el miembro
                if( this.groupList.get(iGrupo).oid == this.memberList.get(iMiembro).uid )
                    //Devolvemos false si intentamos borrar al dueño
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
            else if (this.groupList.get(iGrupo).members.contains( this.memberList.get(mIndex(alias)) )){
                //Si el miembro con ese alias ya está en ese grupo
                return null;
            }
            //Si el grupo existe y el miembro no está en él ya
            this.memberCounter++;
            GroupMember nuevoMiembro = new GroupMember(alias, hostname,this.memberCounter,this.groupList.get(iGrupo).gid);
            this.memberList.add(nuevoMiembro);
            return nuevoMiembro;
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
            iMiembro = mIndex(alias);
            
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
                this.groupList.get(iGrupo).members.remove( this.memberList.get(iMiembro) );
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
            iMiembro = mIndex(alias);

            //Si el grupo no existe
            if(iGrupo == -1) return null;
            boolean member = this.groupList.get(iGrupo).members.contains(this.memberList.get(iMiembro));
            //Si es miembro se devuelve el GroupMember
            if (member) return this.memberList.get(iMiembro);
            //Si n se devuelve null
            else return null;
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
    public boolean AllowMembers(String gid) {
        this.mutex.lock();
        try{
            int iGrupo = gIndex(Integer.getInteger(gid));
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
            LinkedList lista = new LinkedList<String>();

            //Si el grupo no existe
            if(iGrupo == -1){
                return null;
            }
            else{
                //Si el grupo existe, recorremos su lista de miembros
                for(GroupMember GM : this.groupList.get(iGrupo).members){
                    lista.add(GM.hostname);
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
            if(OG.galias.equals(galias)) this.groupList.indexOf(OG);
        }
        return -1;
    }
    
    private int mIndex(String alias){
        for(GroupMember member : memberList){
            if(member.alias.equals(alias)) return this.memberList.indexOf(member);
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