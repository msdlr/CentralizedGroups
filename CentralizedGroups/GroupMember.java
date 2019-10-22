package CentralizedGroups;

import java.io.Serializable;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Silvia
 */
public class GroupMember implements Serializable{
    String alias;
    String hostname;
    int uid;    /* user id  */
    int gid;    /* group id */
    
    public GroupMember(String alias, String hostname, int uid, int gid) {
        this.alias    = alias;
        this.hostname = hostname;
        this.uid = uid;
        this.gid = gid;
    }
}
