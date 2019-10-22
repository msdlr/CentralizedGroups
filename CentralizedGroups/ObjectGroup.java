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
    String alias;
    String ownerAlias;
    int gid;    /* group id */
    int oid;    /* owner id */
    
    public ObjectGroup(String groupAlias, int gid, String userAlias, int uid) {
        
    }
    
    public GroupMember isMember() {
        return null;
    }
    
    public GroupMember addMember() {
        return null;
    }
    
    public boolean removeMember() {
        return false;
    }
    
    public void StopMembers() {}
    public void AllowMembers() {}
    
    public LinkedList<String> ListMembers() {
        return null;
    }
    
}
