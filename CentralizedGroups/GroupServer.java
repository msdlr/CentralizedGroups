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
    long contadorID = 0;
    
    /* FUNCIONES */
    
    /* CONSTRUCTOR */
    public GroupServer() throws RemoteException {
        this.groupList = new LinkedList();
    }

    @Override
    public int createGroup(String galias, String oalias, String ohostname) {
        /* VARIABLES */
        //Cerrojo para la función
        Condition Creating =  this.mutex.newCondition();
        //Objeto ObjectGroup a crear
        ObjectGroup tmp;
        
        /*CÓDIGO*/
        // Buscar si el grupo solicitado ya existe y devolver error
        for(ObjectGroup OG : groupList){
            if(OG.galias.equals(galias)) return -1;
        }
        // Crear el grupo bajo mutex
        mutex.lock();
        try{
        //Intentamos esperar en la cola de espera para esta función
            try {
                Creating.await();
            } catch (InterruptedException ex) {
                Logger.getLogger(GroupServer.class.getName()).log(Level.SEVERE, null, ex);
            }
            //Constructor del grupo:
            //public ObjectGroup(String galias, int gid, String oalias, int oid)
            
        }
        finally{
            mutex.unlock();
        }
        return 0;
    }

    @Override
    public int findGroup(String galias) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
    public boolean AloowMembers(String gid) {
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
