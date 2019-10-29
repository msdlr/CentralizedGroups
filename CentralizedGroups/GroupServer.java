/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    //Contador para generar identificadores de grupo
    int contadorID = 0;
    
    /* FUNCIONES */
    
    /* CONSTRUCTOR */
    public GroupServer() throws RemoteException {
        this.groupList = new LinkedList();
    }

    @Override
    public int createGroup(String galias, String oalias, String ohostname) {
        /* ENTRADA EN SECCIÓN CRÍTICA */
        this.mutex.lock();
        
        /* VARIABLES */
        //Objeto ObjectGroup a crear
        ObjectGroup tmp;
        //El campo oid del constructor de ServeGroup es el uid del usuario del que se pasa el hostname
        int nuevoOID = 0;
        
        /*CÓDIGO*/
        // Buscar si el grupo solicitado ya existe y devolver error
        for(ObjectGroup group : groupList){
            if(group.galias.equals(galias)) return -1;
        }
        try{
            //Constructor del grupo:
            //public ObjectGroup(String galias, int gid, String oalias, int oid)
            
            //Encontramos el miembro con el hostname que se pasa por parámetro
            for(GroupMember member : memberList){
                if( member.hostname == ohostname ) nuevoOID = member.uid;
            }
            //Generamos un nuevo identificador de grupo
            contadorID++;
            //Creamos el nuevo grupo
            this.groupList.add(new ObjectGroup(galias,contadorID, oalias, nuevoOID));
        }
        finally{
            /* SALIDA DE SECCIÓN CRÍTICA */
            mutex.unlock();
        }
        return 0;
    }

    @Override
    public int findGroup(String galias) {
        for(ObjectGroup group : this.groupList){
            //Si lo encuentra devuelve el id del grupo
            if(group.galias.equals(galias)) return group.gid;
        }
        //Si no, devuelve -1
        return -1;
    }

    @Override
    public String findGroup(int gid) {
        for(ObjectGroup OG : groupList){
            if( OG.gid == gid ) return OG.galias;
        }
        return null;
    }

    @Override
    public boolean removeGroup(String galias, String oalias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GroupMember addMember(String galias, String alias, String hostname) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean removeMember(String galias, String alias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GroupMember isMember(String galias, String alias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LinkedList<String> ListGroup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
