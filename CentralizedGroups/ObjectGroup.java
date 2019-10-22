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
    LinkedList<GroupMember> members;
    
    public ObjectGroup(String galias, int gid, String oalias, int oid) {
        this.galias = galias;
        this.oalias = oalias;
        this.gid = gid;
        this.oid = oid;
        addMember(oalias);
    }
    
    public GroupMember isMember(String userAlias) {
        
        return null;
    }
    
    public GroupMember addMember(String userAlias) {
        return null;
    }
    
    public boolean removeMember(String userAlias) {
        return false;
    }
    
    public void StopMembers() {}
    public void AllowMembers() {}
    
    public LinkedList<String> ListMembers() {
        return null;
    }
    
}
