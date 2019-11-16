/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupServer;
import java.rmi.RemoteException;
import java.security.AccessControlException;
import static javafx.application.Platform.exit;

/**
 *
 * @author Miguel
 */
public class testClient {
    
    public static void main(String[] args) throws RemoteException{
        GroupServer gs = new GroupServer();
        String galias = "GRUPO_TEST";
        String ualias ="MSI";
        String uhostname="localhost";
        
        
        //Crear grupo
        if (gs.createGroup(galias, ualias, uhostname) !=-1){
            System.out.println("grupo "+galias+" creado correctamente");
        }
        else{
            System.out.println("Error creando el grupo");
        }
        
        //Listar grupos
        System.out.println("Lista de grupos");
        for(String st : gs.ListGroup()){
            System.out.println(st);
        }
        
        //Listar usuarios del grupo grupo
        System.out.println("Lista de usuarios de "+galias);
        for(String st : gs.ListMembers(galias)){
            System.out.println(st);
        }
        
        
        //Borrar grupo
        
        if(gs.removeGroup(galias, ualias)){
            System.out.println("Grupo " + galias+ " borrado");
        }
        else{
            System.out.println("Error borrando " + galias);
        }
        
        //Unirse a un grupo
        
        
        //Crear grupo con otro alias
        if (gs.createGroup(galias+"2", ualias+"2", uhostname+"2") !=-1){
            System.out.println("grupo "+galias+"2"+" creado correctamente");
        }
        else{
            System.out.println("Error creando el grupo " +galias+"2");
        }
        
        //Unir a un usuario a este grupo
        if(gs.addMember(galias+"2", ualias, uhostname+"2") == null){
            System.out.println("Error uniendose a" + galias+"2");
        }
        
        if(gs.removeMember(galias+"2", ualias)){
            System.out.println("Saliendo de " +galias+"2");
        }
        
        //Bloquear/permitir altas/bajas
        
        try{
            int gid = gs.findGroup(galias+"2");
            if (gs.AllowMembers( Integer.toString(gid) )){
                System.out.println("Bajas en "+galias+"2"+" permitidas");
            }

            if (gs.StopMembers( galias+"2" )){
                System.out.println("Altas en "+galias+"2"+" permitidas");
            }
        }
        catch(AccessControlException e){
            System.out.println("Error con los permisos de " + galias+"2");
        }
        
        
        exit();
    }
}
