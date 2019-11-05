/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupServerInterface;
import static java.lang.System.exit;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.rmi.CORBA.Stub;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    Client() throws RemoteException {}

    public void main(String[] args) {
        //Localizar el servidor en el registro
        String host = null;
        //Registro donde buscar el servidor
        Registry registro = null;
        //Proxy para los métodos del servidor
        GroupServerInterface proxy;
    
        Scanner scanner = new Scanner(System.in);
        String command = "";
        boolean menu = true;

        /* establecer conexion remota o local */
        System.out.println("Conexión remota o local? (l/<dir. ip>)");
        command = scanner.nextLine();
        if (command.equals("l")) {
            host = "127.0.0.1";
        } else {
            host = command;
        }
        /* inicializar registro */
        try {
            registro = LocateRegistry.getRegistry(host);
        } catch (RemoteException e) {
            System.out.println("ERROR inicializando registro");
        }
        /* creacion de proxy */
        try {
            proxy = (GroupServerInterface) registro.lookup("GroupServer");
        } catch (RemoteException ex) {
            System.out.println("ERROR en creacion de proxy (RemoteException)");
        } catch (NotBoundException ex) {
            System.out.println("ERROR en creacion de proxy (NotBoundException)");;
        }

        /* menu */
        System.out.println("Opciones disponibles:");
        while (menu) {
            System.out.println("1: crear grupo\n"
                    + "2: eliminar grupo\n"
                    + "3: modificar miembros de grupo\n"
                    + "4: bloquear/desbloquear altas y bajas\n"
                    + "5: mostrar miembros de un grupo\n"
                    + "6: mostrar grupos actuales\n"
                    + "7: terminar ejecución");
            command = scanner.nextLine();
            switch (command) {
                case "":
                    
                case "4":
                    func4(host, registro, proxy);

                default:
                    System.out.println("Error, saliendo");
                    break;
            }
        }
    }
    private void func4(String host, Registry registro, GroupServerInterface proxy){
        //Variables
        String st = "";
        Scanner s = new Scanner(System.in);
        int gid = 0;
        String galias = null;
        
        //Necesitamos el gid del grupo y si es alta o baja
        //Buscar grupo
        System.out.println("Introduce el alias del grupo");
        st = s.nextLine();
        System.out.println("Buscando grupo"+st);
        
        gid = proxy.findGroup(galias);
        if(gid == -1){
            //Si el grupo no se ha encontrado
            System.out.println("Grupo " + galias + " no encontrado");
        }
        else{
            //Si el grupo se ha encontrado
            System.out.println("GRUPO:" + st + " con  GID" + gid);
            
            System.out.println("[B]loquear altas/bajas \n [D]esbloquear altas/bajas ");
            
            st = s.nextLine();
            switch(st){
                case "D":
                case "d":
                    proxy.AllowMembers(st);
                    System.out.println("Bloqueadas las altas/bajas en el grupo");
                    break;
                case "B":
                case "b":
                    proxy.StopMembers(st);
                    System.out.println("Bloqueadas las altas/bajas en el grupo");
                    break;
                default:
                    System.out.println("ERROR");
            }
            
        }
    }
}
