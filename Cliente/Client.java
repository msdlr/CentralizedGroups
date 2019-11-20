/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupServerInterface;
import java.io.IOException;
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

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    /* Atributos para la clase */

 /* CONSTRUCTOR */
    Client() throws RemoteException {
        //Se exporta para que pueda atender peticiones de Callback (p4)
        super();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        //Asignar fichero de política de seguridad
        System.setProperty("java.security.policy", "D:\\DOC\\NetBeansProjects\\CentralizedGroups\\src\\Cliente\\seguridad.txt");

        //Crear gestor de seguridad si no hay ninguno
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        //Lanzar el registro en el puerto 1099
        try {
            Registry registro = LocateRegistry.getRegistry(1099);
            System.out.println("Registro (local) lanzado correctamente");
        } catch (RemoteException e) {
            System.out.println("Error lanzando el registro");
            System.exit(-1);
        }

        //Creamos el objeto de tipo Cliente
        Client c;
        c = new Client();

        //Objeto del tipo de la interfaz -> proxy
        GroupServerInterface proxy = null;

        //Conexión local / remota
        System.out.println("Local/IP");

        Scanner s = new Scanner(System.in);
        String ip = s.nextLine();
        String url = null;

        //Si no introducimos IP conectamos con el servidor local
        if ("".equals(ip)) {
            ip = "127.0.0.1";
        }

        url = "rmi://" + ip + "/GroupServer";
        System.out.println("Buscando servidor: " + url);

        try {
            proxy = (GroupServerInterface) Naming.lookup(url);
            System.out.println("PROXY OBTENIDO CORRECTAMENTE");
        } catch (NotBoundException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Name not bound");
            System.exit(-2);
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error en la direccion");
            System.exit(-3);
        } catch (RemoteException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se ha podido contactar con el registro");
            System.exit(-4);
        } catch (java.security.AccessControlException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error de seguridad (revisar pathname del fichero de políticas)");
            System.exit(-5);
        }

        System.out.println("Introduce tu alias!");
        String alias = s.nextLine();
        String localhost = InetAddress.getLocalHost().getHostName();

        while (true) {
            String opcion = "";

            System.out.println("\n"+alias + "@" + localhost + 
                    "1: crear grupo\n"
                    + "2: eliminar grupo\n"
                    + "3: entrar/salir de un grupo\n"
                    + "4: bloquear/desbloquear altas y bajas\n"
                    + "5: mostrar miembros de un grupo\n"
                    + "6: mostrar grupos actuales\n"
                    + "7: terminar ejecución");

            //Leer opción
            opcion = s.nextLine();

            switch (Integer.parseInt(opcion)) {
                case 1: //Crear un grupo nuevo
                    // supone que alias y hostname se obtienen al principio del main
                    System.out.println("Creando grupo...");
                    System.out.println("Alias del grupo:");
                    String nuevoGalias = s.nextLine();
                    try {
                        if (proxy.createGroup(nuevoGalias, alias, localhost) == -1) {
                            System.out.println("ERROR al crear el grupo");
                            return;
                        }
                    } catch (RemoteException ex) {
                        System.out.println("createGroup(): ERROR de acceso remoto creando grupo");
                    }
                    System.out.println("Grupo " + nuevoGalias + " creado");
                    break;

                case 2: //Borrar grupo
                    System.out.println("eliminando grupo...");
                    String galias;
                    System.out.println("Alias del grupo:");
                    galias = s.nextLine();
                    try {
                        if (!proxy.ListMembers(galias).getFirst().equals(alias)) {
                            System.out.println("ERROR verificando que se es propietario");
                            return;
                        }
                    } catch (RemoteException ex) {
                        System.out.println("deleteGroup(): ERROR de acceso remoto verificando propietario");
                    } catch (NullPointerException ex) {
                        System.out.println("deleteGroup(): ERROR, el grupo no existe");
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

                    break;

                case 3: //Unirse / salir de un grupo
                    String entrarSalir,
                     galiasEntrarSalir;
                    System.out.println("Alias del grupo:");
                    galiasEntrarSalir = s.nextLine();
                    try {
                        if (proxy.isMember(galiasEntrarSalir, alias) != null) {
                            System.out.println("Ya estás en el grupo " + galiasEntrarSalir + ", salir de él? (s/n)");
                            entrarSalir = s.nextLine();
                            if (entrarSalir.equals("s")) {
                                if (!proxy.removeMember(galiasEntrarSalir, alias)) {
                                    System.out.println("ERROR al salir de grupo");
                                } else {
                                    System.out.println("Se ha salido del grupo con éxito");
                                }
                            } else if (entrarSalir.equals("n")) {
                                System.out.println("Operación cancelada");
                            }
                        } else {
                            System.out.println("Unirte al grupo " + galiasEntrarSalir + "? (s/n)");
                            entrarSalir = s.nextLine();
                            if (entrarSalir.equals("s")) {
                                if (proxy.addMember(galiasEntrarSalir, alias, localhost) == null) {
                                    System.out.println("ERROR al unirse a grupo; las altas pueden estar bloqueadas");
                                } else {
                                    System.out.println("Se ha unido al grupo con éxito");
                                }
                            } else if (entrarSalir.equals("n")) {
                                System.out.println("Operación cancelada");
                            }
                        }
                    } catch (RemoteException ex) {
                        System.out.println("modGroup(): ERROR de acceso remoto");
                    }

                    break;

                case 4: //Bloquear/desbloquear altas/bajas

                    //Variables
                    String B_D = "";
                    int gid = 0;
                    String busqueda = null;

                    //Necesitamos el gid del grupo y si es alta o baja
                    //Buscar grupo
                    System.out.println("Introduce el alias del grupo");
                    busqueda = s.nextLine();
                    System.out.println("Buscando grupo" + busqueda);

                    try {
                        gid = proxy.findGroup(busqueda);
                    } catch (RemoteException ex) {
                        System.out.println("allowOrDeny(): ERROR obteniendo gid");
                        break;
                    }
                    if (gid == -1) {
                        //Si el grupo no se ha encontrado
                        System.out.println("Grupo " + busqueda + " no encontrado");
                    } else {
                        //Si el grupo se ha encontrado
                        System.out.println("GRUPO:" + busqueda + " con  GID" + gid);

                        System.out.println("[B]loquear altas/bajas \n [D]esbloquear altas/bajas ");

                        B_D = s.nextLine();
                        switch (B_D) {
                            case "D":
                            case "d":
                                try {
                                    proxy.AllowMembers(busqueda);
                                } catch (RemoteException ex) {
                                    System.out.println("allowOrDeny(): ERROR desbloqueando altas/bajas");
                                }
                                System.out.println("DesbCloqueadas las altas/bajas en el grupo " + B_D);
                                break;
                            case "B":
                            case "b":
                                try {
                                    proxy.StopMembers(busqueda);
                                } catch (RemoteException ex) {
                                    System.out.println("allowOrDeny(): ERROR bloqueando altas/bajas");
                                }
                                System.out.println("Bloqueadas las altas/bajas en el grupo " + busqueda);
                                break;
                            default:
                                System.out.println("ERROR");
                        }
                    }

                case 5: //Mostrar miembros de un grupo en específico
                    String nombreGrupo = "";
                    LinkedList<String> namesList;

                    System.out.println("Introduce el alias del grupo");
                    nombreGrupo = s.nextLine();
                    System.out.println("Buscando el grupo con alias \"" + nombreGrupo + "\"");
                    try {
                        namesList = proxy.ListMembers(nombreGrupo);
                        if (namesList == null) {
                            System.out.println("El grupo " + nombreGrupo + " no existe");
                        } else {
                            System.out.println(namesList.toString());
                        }
                    } catch (RemoteException ex) {
                        System.out.println("groupMembers(): ERROR de acceso remoto obteniendo listado de miembros");
                    }
                    break;

                case 6: //Mostrar grupos actuales
                    LinkedList<String> l = new LinkedList();
                    try {
                        l = proxy.ListGroup();
                        if (l.isEmpty()) {
                            System.out.println("No hay grupos en este servidor");
                        } else {
                            System.out.println(l.toString());
                        }
                    } catch (RemoteException ex) {
                        System.out.println("listGroups(): ERROR de acceso remoto obteniendo listado de grupos");
                    }
                    break;

                case 7: //Salir
                    System.exit(0);

                default: //Intro / opción no válida 
                    System.out.println("Accion incorrecta");
            }
        }

    }

}
