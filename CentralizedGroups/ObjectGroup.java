/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.util.LinkedList;

/**
 *
 * @author Silvia
 */
public class ObjectGroup {
    String galias;
    String oalias;
    int gid;    /* group id */
    int oid;    /* owner id */
    private int counter;
    LinkedList<GroupMember> members;
    
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
            if (member.alias.equals(alias)) return member;
        }
        return null;
    }
    
    public GroupMember addMember(String alias) {
        /* si ya existe un miembro con el mismo alias, devolver null */
        if (isMember(alias) == null) return null;
        /* si no, añadir miembro nuevo */
        members.add(new GroupMember(alias, this.oalias, counter, this.gid));
        /*                                 ^ nombre del propietario??     */
        counter++;
        /* TODO: figure this return out */
        return null;
    }
    
    public boolean removeMember(String ualias) {
        return false;
    }
    
    public void StopMembers() {}
    public void AllowMembers() {}
    
    public LinkedList<String> ListMembers() {
        return null;
    }
    
}
