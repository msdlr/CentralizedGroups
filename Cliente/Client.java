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

    // Propiedades de usuario
    static String alias;
    static String hostname;

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
        
        //Asignar fichero de seguridad
        System.setProperty("java.security.policy", "C:\\Users\\usuario\\Desktop\\CentralizedGroups\\src\\Cliente\\seguridad.txt");
        //System.setProperty("java.security.policy", "/home/pwnage/NetBeansProjects/CentralisedGroups/src/Cliente/seguridad.txt");
        
        //Objtener gestor de seguridad
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

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
            System.out.println("HOSTNAME: " + InetAddress.getLocalHost().getHostName());
        } catch (UnknownHostException ex) {
            System.out.println("No se pudo obtener el hostname local");
        }

        /* establecer conexion remota o local */
        System.out.println("Conexión remota o local? (l/<dir. ip>)");
        command = scanner.nextLine();
        if (command.equals("l")) {
            host="localhost";
            url = "//localhost/GroupServer";
        } else {
            host = command;
            url = "//" + host + "/GroupServer";
        }
        /* inicializar registro */
        try {
            registro = LocateRegistry.getRegistry(host);
            System.out.println("Registro de " + host +" obtenido correctamente");
        } catch (RemoteException e) {
            System.out.println("ERROR inicializando registro");
        }

        /* creacion de proxy */
        try {
            proxy = (GroupServerInterface) Naming.lookup(url);
        } catch (RemoteException ex) {
            System.out.println("No se ha podido contactar con el registro");
            exit(-1);
        } catch (NotBoundException ex) {
            System.out.println("Name not bound");
            exit(-1);
        } catch (MalformedURLException ex) {
            System.out.println("Error en la dirección del server");
            exit(-1);
        } catch (java.security.AccessControlException ex) {
            System.out.println("No se cumple la política de seguridad");
            exit(-1);
        }

        /* obtencion de alias y hostname */
        System.out.println("Introduzca su alias:");
        alias = scanner.nextLine();
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ex) {
            System.out.println("ERROR obteniendo hostname");
        }

        /* menu */
        if (menu) {
            System.out.println("Opciones disponibles:");
        }
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

    private static void allowOrDeny(GroupServerInterface proxy) {
        //Variables
        String st = "";
        Scanner s = new Scanner(System.in);
        int gid = 0;
        String galias = null;

        //Necesitamos el gid del grupo y si es alta o baja
        //Buscar grupo
        System.out.println("Introduce el alias del grupo");
        st = s.nextLine();
        System.out.println("Buscando grupo" + st);

        try {
            gid = proxy.findGroup(galias);
        } catch (RemoteException ex) {
            System.out.println("allowOrDeny(): ERROR obteniendo gid");
        }
        if (gid == -1) {
            //Si el grupo no se ha encontrado
            System.out.println("Grupo " + galias + " no encontrado");
        } else {
            //Si el grupo se ha encontrado
            System.out.println("GRUPO:" + st + " con  GID" + gid);

            System.out.println("[B]loquear altas/bajas \n [D]esbloquear altas/bajas ");

            st = s.nextLine();
            switch (st) {
                case "D":
                case "d":
                    try {
                        proxy.AllowMembers(st);
                    } catch (RemoteException ex) {
                        System.out.println("allowOrDeny(): ERROR desbloqueando altas/bajas");
                    }
                    System.out.println("Bloqueadas las altas/bajas en el grupo " + st);
                    break;
                case "B":
                case "b":
                    try {
                        proxy.StopMembers(st);
                    } catch (RemoteException ex) {
                        System.out.println("allowOrDeny(): ERROR bloqueando altas/bajas");
                    }
                    System.out.println("Bloqueadas las altas/bajas en el grupo " + st);
                    break;
                default:
                    System.out.println("ERROR");
            }

        }
    }

    public static void createGroup(Scanner scanner, GroupServerInterface proxy) {
        // supone que alias y hostname se obtienen al principio del main
        System.out.println("Creando grupo...");
        System.out.println("Alias del grupo:");
        String galias = scanner.nextLine();
        try {
            if (proxy.createGroup(galias, alias, hostname) < 1) {
                System.out.println("ERROR al crear el grupo");
                return;
            }
        } catch (RemoteException ex) {
            System.out.println("createGroup(): ERROR de acceso remoto creando grupo");
        }
        System.out.println("Grupo " + galias + " creado");
    }

    public static void deleteGroup(Scanner scanner, GroupServerInterface proxy) {
        System.out.println("eliminando grupo...");
        String galias;
        System.out.println("Alias del grupo:");
        galias = scanner.nextLine();
        try {
            if (!proxy.ListMembers(galias).getFirst().equals(alias)) {
                System.out.println("ERROR verificando que se es propietario");
                return;
            }
        } catch (RemoteException ex) {
            System.out.println("deleteGroup(): ERROR de acceso remoto verificando propietario");
        }
        try {
            if (!proxy.removeGroup(galias, alias)) {
                System.out.println("ERROR al borrar grupo");
            } else {
                System.out.println("Grupo " + galias + " eliminado");
            }
        } catch (RemoteException ex) {
            System.out.println("deleteGroup(): ERROR de acceso remoto borrando grupo");
        }
    }

    public static void modGroup(Scanner scanner, GroupServerInterface proxy) {
        // supone que alias se obtiene al principio del main
        String option, galias;
        System.out.println("Alias del grupo:");
        galias = scanner.nextLine();
        try {
            if (proxy.isMember(galias, alias) != null) {
                System.out.println("Ya estás en el grupo " + galias + ", salir de él? (s/n)");
                option = scanner.nextLine();
                if (option.equals("s")) {
                    if (!proxy.removeMember(galias, alias)) {
                        System.out.println("ERROR al salir de grupo");
                    } else {
                        System.out.println("Se ha salido del grupo con éxito");
                    }
                } else if (option.equals("n")) {
                    System.out.println("Operación cancelada");
                }
            } else {
                System.out.println("Unirte al grupo " + galias + "? (s/n)");
                option = scanner.nextLine();
                if (option.equals("s")) {
                    if (proxy.addMember(galias, alias, hostname) == null) {
                        System.out.println("ERROR al unirse a grupo");
                    } else {
                        System.out.println("Se ha unido al grupo con éxito");
                    }
                } else if (option.equals("n")) {
                    System.out.println("Operación cancelada");
                }
            }
        } catch (RemoteException ex) {
            System.out.println("modGroup(): ERROR de acceso remoto");
        }
    }

    private static void groupMembers(GroupServerInterface proxy) {
        /*
        mostrar miembros de un grupo
         */
        String st = "";
        Scanner scanner = new Scanner(System.in);
        LinkedList<String> namesList;

        System.out.println("Introduce el alias del grupo");
        st = scanner.nextLine();
        System.out.println("Buscando el grupo con alias \"" + st + "\"");
        try {
            namesList = proxy.ListMembers(st);
            if (namesList == null) {
                System.out.println("El grupo" + st + " no existe");
            } else {
                System.out.println(namesList.toString());
            }
        } catch (RemoteException ex) {
            System.out.println("groupMembers(): ERROR de acceso remoto obteniendo listado de miembros");
        }

    }

    private static void listGroups(GroupServerInterface proxy) {
        /*
        mostrar grupos actuales
         */
        LinkedList<String> list;
        try {
            list = proxy.ListGroup();
            if (list.isEmpty()) {
                System.out.println("No hay grupos en este servidor");
            } else {
                System.out.println(list.toString());
            }
        } catch (RemoteException ex) {
            System.out.println("listGroups(): ERROR de acceso remoto obteniendo listado de grupos");
        }
    }
}
