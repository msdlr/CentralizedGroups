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
    public String alias;
    public String hostname;
    public int uid;    /* user id  */
    public int gid;    /* group id */
    
    //Añadido en p4, puerto de la petición
    int port;
    
    public GroupMember(String alias, String hostname, int uid, int gid, int port) {
        this.alias    = alias;
        this.hostname = hostname;
        this.uid = uid;
        this.gid = gid;
        this.port = port;
    }
}
