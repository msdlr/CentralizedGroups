/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.util.LinkedList;

/**
 *
 * @author Miguel
 */
public interface GroupServerInterface {
    int createGroup (String galias, String oalias, String ohostname);
    int findGroup (String galias);
    String findGroup (int gid);
    boolean removeGroup (String galias, String oalias);
    GroupMember addMember (String galias, String alias, String hostname);
    boolean removeMember (String galias, String alias);
    GroupMember isMember (String galias, String alias);
    boolean StopMembers (String galias);
    boolean AloowMembers (String gid);
    LinkedList<String> ListMembers (String galias);
    LinkedList<String> ListGroup();
}
