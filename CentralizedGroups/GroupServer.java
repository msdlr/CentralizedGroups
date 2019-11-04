/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.rmi.RemoteException;
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
    private LinkedList<ObjectGroup> groupList;
    //Lista de miembros
    private LinkedList<GroupMember> memberList;
    //Cerrojos para funciones de grupos
    Lock mutex = new ReentrantLock();
    //Contador para generar identificadores de grupo y usuario
    int groupCounter = 0;
    int memberCounter = 0;
    
    /* FUNCIONES */
    
    /* CONSTRUCTOR */
    public GroupServer() throws RemoteException {
        this.groupList = new LinkedList();
    }

    @Override
    public int createGroup(String galias, String oalias, String ohostname) {
        /* ENTRADA EN SECCIÓN CRÍTICA */
        this.mutex.lock();
        
        try{
            /* VARIABLES */
            //El campo oid del constructor de ServeGroup es el uid del usuario del que se pasa el hostname
            int nuevoOID = 0;
            int buscarMiembro;

            /*CÓDIGO*/
            // Buscar si el grupo solicitado ya existe y devolver error
            if(findGroup(galias) >= 0) return -1;
            
            //Encontramos el miembro con el alias que se pasa por parámetro
            buscarMiembro = getMember(oalias);
            
            //Si no existe el miembro se devuelve -1
            if(buscarMiembro == -1){
                nuevoOID = this.memberList.get(buscarMiembro).uid;
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
            int buscarGrupo = getGroup(galias);
            if(buscarGrupo == -1){
                return -1;
            }
            else{
                return this.groupList.get(buscarGrupo).gid;
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
            int buscarGrupo = getGroup(gid);
            if (buscarGrupo != -1){
                return this.groupList.get(buscarGrupo).galias;
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
            int buscarGrupo, buscarMiembro;
            buscarGrupo = getGroup(galias);
            buscarMiembro = getMember(oalias);
            if (buscarGrupo != -1 && buscarMiembro != -1){
                //Si existen el grupo y el miembro
                if( this.groupList.get(buscarGrupo).oid == this.memberList.get(buscarMiembro).uid )
                    //Devolvemos false si intentamos borrar al dueño
                    return false;
                else {
                    //Lo borramos y devolvemos true
                    this.groupList.get(buscarGrupo).members.remove(buscarMiembro);
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
            int buscarGrupo = getGroup(galias);
            
            //Si el grupo no está en la lista
            if(buscarGrupo == -1) return null;
            else if (this.groupList.get(buscarGrupo).members.contains( this.memberList.get(getMember(alias)) )){
                //Si el miembro con ese alias ya está en ese grupo
                return null;
            }
            //Si el grupo existe y el miembro no está en él ya
            this.memberCounter++;
            GroupMember nuevoMiembro = new GroupMember(alias, hostname,this.memberCounter,this.groupList.get(buscarGrupo).gid);
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
            int buscarGrupo, buscarMiembro;
            buscarGrupo = getGroup(galias);
            buscarMiembro = getMember(alias);
            
            //Si el grupo no existe
            if(buscarGrupo == -1){
                return false;
            }
            //Si existe pero se intenta borrar el dueño
            else if (this.groupList.get(buscarGrupo).oalias.equals(alias)){
                return false;
            }
            //Si se intenta borrar cualquier otro miembro
            else{
                this.groupList.get(buscarGrupo).members.remove( this.memberList.get(buscarMiembro) );
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
            int buscarGrupo, buscarMiembro;
            buscarGrupo = getGroup(galias);
            buscarMiembro = getMember(alias);

            //Si el grupo no existe
            if(buscarGrupo == -1) return null;
            boolean member = this.groupList.get(buscarGrupo).members.contains(this.memberList.get(buscarMiembro));
            //Si es miembro se devuelve el GroupMember
            if (member) return this.memberList.get(buscarMiembro);
            //Si n se devuelve null
            else return null;
        }
        finally{
            this.mutex.unlock();
        }
    }

    @Override
    public boolean StopMembers(String galias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean AllowMembers(String gid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedList<String> ListMembers(String galias) {
        this.mutex.lock();
        try{
            int buscarGrupo = getGroup(galias);
            LinkedList lista = new LinkedList<String>();

            //Si el grupo no existe
            if(buscarGrupo == -1){
                return null;
            }
            else{
                //Si el grupo existe, recorremos su lista de miembros
                for(GroupMember GM : this.groupList.get(buscarGrupo).members){
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
    private int getGroup(int gid){
        for(ObjectGroup OG : this.groupList){
            if(OG.gid == gid) return this.groupList.indexOf(OG);
        }
        return -1;
    }
    
    private int getGroup(String galias){
        for(ObjectGroup OG : this.groupList){
            if(OG.galias.equals(galias)) this.groupList.indexOf(OG);
        }
        return -1;
    }
    
    private int getMember(String alias){
        for(GroupMember member : memberList){
            if(member.alias.equals(alias)) return this.memberList.indexOf(member);
        }
        return -1;
    }
    
    private int getMember(int uid){
        for(GroupMember member : memberList){
            if(member.uid == uid) return this.memberList.indexOf(member);
        }
        return -1;
    }
}
