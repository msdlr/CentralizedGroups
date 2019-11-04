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
    int gid;
    /* group id */
    int oid;
    /* owner id */
    private int counter;
    LinkedList<GroupMember> members;

    /* estructuras para bloqueo */
    ReentrantLock l = new ReentrantLock(true);
    Condition allowInsert = l.newCondition();
    Condition allowDelete = l.newCondition();

    public ObjectGroup(String galias, int gid, String oalias, int oid) {
        this.galias = galias;
        this.oalias = oalias;
        this.gid = gid;
        this.oid = oid;
        this.counter = 1;
        addMember(oalias);
    }

    public GroupMember isMember(String alias) {
        /* buscar miembro en la lista */
        for (GroupMember member : members) {
            if (member.alias.equals(alias)) {
                return member;
            }
        }
        return null;
    }

    public GroupMember addMember(String alias) {
        /* TODO: control de bloqueo */
        try {
            while (l.isLocked()) {
                allowInsert.await();
            }

            /* si ya existe un miembro con el mismo alias, devolver null */
            if (isMember(alias) != null) {
                return null;
            }
            /* si no, añadir miembro nuevo */
            members.add(new GroupMember(alias, oalias, counter, gid));
            /* una vez añadido, incrementar contador y devolver miembro */
            counter++;
            return members.getLast();

        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        }
        return null;
    }

    public boolean removeMember(String ualias) {
        /* TODO: control de bloqueo */
        try {
            while (l.isLocked()) {
                allowDelete.await();
            }

            /* si no existe el miembro o es el propietario, devolver null */
            if (isMember(ualias) == null || oalias.equals(ualias)) {
                return false;
            }
            members.remove(isMember(ualias));
            return true;

        } catch (InterruptedException e) {
            System.out.println(e.getStackTrace());
        }
        return false;
    }

    public void StopMembers() {
        l.lock();
    }

    public void AllowMembers() {
        allowInsert.signalAll();
        allowDelete.signalAll();
        l.unlock();
    }

    public LinkedList<String> ListMembers() {
        LinkedList<String> nombres = null;
        for (GroupMember member : members) {
            nombres.add(member.alias);
        }
        return nombres;
    }

}
