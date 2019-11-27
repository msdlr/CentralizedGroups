/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Silvia
 */
public class ObjectGroup {

    String galias;
    String oalias;
    int gid;    /* group id */
    int oid;    /* owner id */
    private int counter;    /* == n. de miembros */
    int pendingMsgs;        /* n. de envios pendientes */
    LinkedList<GroupMember> members;

    /* estructuras para bloqueo */
    int waiting = 0;
    boolean locked = false;
    ReentrantLock l = new ReentrantLock(true);
    Condition allowMod = l.newCondition();

    //Constructor actualizado para p4
    public ObjectGroup(String galias, int gid, String oalias, String ohostname, int port) {
        this.galias = galias;
        this.oalias = oalias;
        this.gid = gid;
        this.members = new LinkedList();
        counter = 0;
        pendingMsgs = 0;
        //addMember(oalias);
        GroupMember member = new GroupMember(oalias, ohostname, counter+1, gid, port);
        members.add(member);
        counter++;
    }

    public GroupMember isMember(String alias) {
        this.l.lock();
        try{
            /* buscar miembro en la lista */
        for (GroupMember member : members) {
            if (member.alias.equals(alias)) {
                return member;
            }
        }
        return null;
        }
        finally {
            this.l.unlock();
        }
    }

    public GroupMember addMember(String alias, int port) {
        /* TODO: control de bloqueo */
        l.lock();
        try {
    //        /* si las altas y bajas estan bloqueadas, esperar a que se
    //           desbloqueen                                             */
    //        while (locked) {
    //            allowMod.await();
    //        }
            /* salir de la función si las altas y bajas estan bloqueadas */
            if (locked) {
                System.out.println("Las altas y bajas de miembros se encuentran "
                        + "bloqueadas en este momento.\n");
                return null;
            }
            /* si ya existe un miembro con el mismo alias, devolver null */
            if (isMember(alias) != null) {
                return null;
            }
            /* si no, añadir miembro nuevo */
            members.add(new GroupMember(alias, oalias, counter, gid, port));
            /* una vez añadido, incrementar contador y devolver miembro */
            counter++;
            return isMember(alias);    /* si la adición ha fallado, devuelve null */
    //    } catch (InterruptedException e) {
    //        System.out.println("ERROR esperando a desbloqueo");
    //        return null;
    //    }
        } finally {
            l.unlock();
        }
    }

    public boolean removeMember(String alias) {
        /* TODO: control de bloqueo */
        l.lock();
        try {
            /* si las altas y bajas estan bloqueadas, esperar a que se 
               desbloqueen                                             */
            while (locked) {
                allowMod.await();
            }
            /* si no existe el miembro o es el propietario, devolver false */
            if (isMember(alias) == null || oalias.equals(alias)) {
                return false;
            }
            /* si no hay fallos quitandolo de la lista, devuelve true */
            return members.remove(isMember(alias));
        } catch (InterruptedException e) {
            System.out.println("ERROR esperando a desbloqueo");
            return false;
        } finally {
            l.unlock();
        }
    }

/*  public void StopMembers() {
        l.lock();
        try {
            locked = true;
        } finally {
            l.unlock();
        }
    }

    public void AllowMembers() {
        l.lock();
        try {
            locked = false;
            allowMod.signalAll();    // desbloquear clientes bloqueados
        } finally {
            l.unlock();
        }
    }  */

    public LinkedList<String> ListMembers() {
        l.lock();
        try{
        LinkedList<String> nombres = new LinkedList();
        for (GroupMember member : members) {
            nombres.add(member.alias);
        }
        //this.l.unlock();
        return nombres;
        }
        finally{
            l.unlock();
        }
    }
    
    void Sending() {
        l.lock();
        try {
            pendingMsgs++;
            if (pendingMsgs > 0) {
                locked = true;
            }
        } finally {
            l.unlock();
        }
    }
    
    void EndSending() {
        l.lock();
        try {
            pendingMsgs--;
            if (pendingMsgs == 0) {
                locked = false;
                allowMod.signalAll();
            }
        } finally {
            l.unlock();
        }
    }
    
    boolean sendGroupMessage(GroupMember gm, byte msg[]) {
        // TODO: implement this
        l.lock();
        try {
            Sending();
            
            // TODO: run SendingMessage thread
            for (GroupMember target : members) {
                if (!target.equals(gm)) {
                    SendingMessage m = new SendingMessage(this, new GroupMessage(msg, gm), target);
                    m.start();
                }
            }
            
            EndSending();
            return true;
        } finally {
            l.unlock();
        }
        //return false;
    }

}
