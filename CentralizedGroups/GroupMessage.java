/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import java.io.Serializable;

/**
 *
 * @author Silvia
 */
public class GroupMessage implements Serializable {
    public byte[] mensaje;
    public GroupMember emisor;
    
    public GroupMessage(byte[] mensaje, GroupMember emisor) {
        this.mensaje = mensaje;
        this.emisor = emisor;
    }
}
