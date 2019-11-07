/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupServerInterface;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.rmi.CORBA.Stub;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    Client() throws RemoteException {
        //Se exporta para para los callbacks de la práctica 4
        super();
    }

    public static void main(String[] args) {
        //Localizar el servidor en el registro
        String host = null;
        String url = "";
        //Registro donde buscar el servidor
        Registry registro = null;
        //Proxy para los métodos del servidor
        GroupServerInterface proxy = null;
    
        Scanner scanner = new Scanner(System.in);
        String command = "";
        boolean menu = true;

        //Objtener gestor de seguridad
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        //Asignar fichero de seguridad
        //System.setProperty("java.security.policy", "All Files	C:\\Users\\Miguel\\Desktop\\CentralizedGroups\\src\\Cliente\\seguridad.txt");
        
        try {
            //Registro para el callback de la Práctica 4
            Registry registry = LocateRegistry.getRegistry(1098);
            System.out.println("Registro lanzado correctamente");
        } catch (RemoteException ex) {
            System.out.println("Error al lanzar el registro");
        }
        
        try {
            //Objeto de la clase cliente
            Client c = new Client();
            System.out.println("Cliente creado correctamente");
        } catch (RemoteException ex) {
            System.out.println("Error iniciando el cliente");
        }
        
        try {
            //Conseguir hostname local
            System.out.println("Consiguiendo hostname local...");
            System.out.println("HOSTNAME: "+InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            System.out.println("No se pudo obtener el hostname local");
        }
        
        /* establecer conexion remota o local */
        System.out.println("Conexión remota o local? (l/<dir. ip>)");
        command = scanner.nextLine();
        if (command.equals("l")) {
            url="//127.0.0.1/GroupServer";
        } else {
            host = command;
            url="//"+host+"/GroupServer";
        }
        /* inicializar registro */
        try {
            registro = LocateRegistry.getRegistry(host);
            System.out.println("Registro obtenido correctamente");
        } catch (RemoteException e) {
            System.out.println("ERROR inicializando registro");
        }
        
        /* creacion de proxy */
        try {
            proxy = (GroupServerInterface) Naming.lookup("//127.0.0.1/GroupServer");
        } catch (RemoteException ex) {
            System.out.println("No se ha podido contactar con el registro");
            exit(-1);
        } catch (NotBoundException ex) {
            System.out.println("Name not bound");
            exit(-1);
        } catch (MalformedURLException ex) {
            System.out.println("Error en la dirección del server");
            exit(-1);
        }catch (java.security.AccessControlException ex){
            System.out.println("No se cumple la política de seguridad");
            exit(-1);
        }
        
        /* menu */
        if(menu)System.out.println("Opciones disponibles:");
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
                case "1":
                    createGroup(scanner, proxy);
                    break;
                case "2":
                    deleteGroup(scanner, proxy);
                    break;
                case "3":
                    modGroup(scanner, proxy);
                    break;
                case "4":
                    allowOrDeny(proxy);
                    break;
                case "5":
                    groupMembers(proxy);
                    break;
                case "6":
                    listGroups(proxy);
                    break;
                case "7":
                    exit(0);
                    break;
                default:
                    //Se vuelve a mostrar el menú
                    continue;
            }
        }
    }
    private static void allowOrDeny(GroupServerInterface proxy){
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
                    System.out.println("Bloqueadas las altas/bajas en el grupo " + st);
                    break;
                case "B":
                case "b":
                    proxy.StopMembers(st);
                    System.out.println("Bloqueadas las altas/bajas en el grupo " + st);
                    break;
                default:
                    System.out.println("ERROR");
            }
            
        }
    }
    
    public static void createGroup(Scanner scanner, GroupServerInterface proxy) {
        System.out.println("Creando grupo...");
        String galias, oalias, ohostname;
        System.out.println("Alias del grupo:");
        galias = scanner.nextLine();
        System.out.println("Alias del propietario:");
        oalias = scanner.nextLine();
        System.out.println("Hostname del propietario:");
        ohostname = scanner.nextLine();
        proxy.createGroup(galias, oalias, ohostname);
        System.out.println("Grupo " + galias + " creado");
    }
    
    public static void deleteGroup(Scanner scanner, GroupServerInterface proxy) {
        System.out.println("eliminando grupo...");
        String galias, oalias;
        System.out.println("Alias del grupo:");
        galias = scanner.nextLine();
        System.out.println("Alias del propietario:");
        oalias = scanner.nextLine();
        proxy.removeGroup(galias, oalias);
        System.out.println("Grupo " + galias + " eliminado");
    }
    
    public static void modGroup(Scanner scanner, GroupServerInterface proxy) {
        System.out.println("modificando miembros...");
        String option, galias, oalias;
        System.out.println("Alias del grupo:");
        galias = scanner.nextLine();
        System.out.println("Añadir o borrar miembro? (a/b)");
        option = scanner.nextLine();
        if (option.equals("a")) {
            // TODO
        } else if (option.equals("b")) {
            // TODO
        } else System.out.println("opción inválida");
    }
    
    private static void groupMembers(GroupServerInterface proxy) {
        /*
        mostrar miembros de un grupo
        */
        String st ="";
        Scanner scanner = new Scanner(System.in);
        LinkedList<String> namesList;
        
        System.out.println("Introduce el alias del grupo");
        st = scanner.nextLine();
        System.out.println("Buscando el grupo con alias \"" + st + "\"");
        namesList = proxy.ListMembers(st);
        
        if(namesList == null){
            System.out.println("El grupo" +st+ " no existe");
        }else{
            System.out.println(namesList.toString());
        }
        
    }

    private static void listGroups(GroupServerInterface proxy) {
        /*
        mostrar grupos actuales
        */
        LinkedList<String> list = proxy.ListGroup();
        if(list.isEmpty()){
            System.out.println("No hay grupos en este servidor");
        }
        else{
            System.out.println(list.toString());
        }
    }
}
