/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupMember;
import CentralizedGroups.GroupMessage;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface {

    /* Atributos para la clase */
    private static Client c;
    
    private int cPort;
    private String alias="";
    private Queue<GroupMessage> msgQueue;
    private GroupServerInterface proxy;
    
    private ReentrantLock mutex = new ReentrantLock(true);
    private Condition waiting = mutex.newCondition();

    /* CONSTRUCTOR */
    Client(int p) throws RemoteException {
        //Se exporta para que pueda atender peticiones de Callback (p4)
        super();

        //Crear gestor de seguridad si no hay ninguno
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        
        //Inicialización de los campos
        cPort = p;
        mutex = new ReentrantLock(true);
        waiting = mutex.newCondition();
        msgQueue = new LinkedList<GroupMessage>();
        alias=new String();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        //Asignar fichero de política de seguridad
        System.setProperty("java.security.policy", "src/Cliente/seguridad.txt");
        System.setProperty("java.rmi.server.hostname", InetAddress.getLocalHost().getHostAddress());
        
        //Crear gestor de seguridad si no hay ninguno
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        //Creamos el objeto de tipo Cliente
        //Preguntar puerto
        Scanner s = new Scanner(System.in);
        System.out.println("Introduce puerto del registro local");
        String puerto = s.nextLine();

        int nPuerto = 1099;
        try {
            nPuerto = Integer.parseInt(puerto);
        } catch (NumberFormatException e) {
            System.out.println("Puerto no válido, asignando 1099");
        }
        
        //Conseguir hostname local
        String localhost = InetAddress.getLocalHost().getHostAddress();

       
        
        //Creación del objeto cliente
        try{
            c = new Client(nPuerto);
        } catch (RemoteException e){
            System.out.println("Error remoto creando el servidor");
        } catch(Exception e) {
            System.out.println("Error desconocido");
        }
        
         System.out.println("Introduce tu alias");
         c.alias = s.nextLine();
        
        //Lanzar el registro en el puerto 1099
        try {
            Registry registro = LocateRegistry.createRegistry(c.cPort);
            Naming.rebind("//"+localhost+"/"+c.alias, c);
            System.out.println("Registro (local) lanzado correctamente");
        } catch (RemoteException e) {
            System.out.println("Error lanzando el registro (puerto ocupado)");
            System.exit(0);
        }


        //Objeto del tipo de la interfaz -> proxy
        c.proxy = null;

        //Conexión local / remota
        System.out.println("Local/IP");

        String ip = s.nextLine();
        String url = null;

        //Si no introducimos IP conectamos con el servidor local
        if ("".equals(ip)) {
            ip = "127.0.0.1";
        }

        url = "rmi://" + ip + "/GroupServer";
        System.out.println("Buscando servidor: " + url);

        try {
            c.proxy = (GroupServerInterface) Naming.lookup(url);
            System.out.println("PROXY OBTENIDO CORRECTAMENTE");
        } catch (NotBoundException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Name not bound");
            System.exit(2);
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error en la direccion");
            System.exit(3);
        } catch (RemoteException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se ha podido contactar con el registro");
            System.exit(4);
        } catch (java.security.AccessControlException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error de seguridad (revisar pathname del fichero de políticas)");
            System.exit(5);
        }

        
        
        while (true) {
            String opcion = "";

            System.out.println("\n" + c.alias + "@" + localhost + "\n"
                    + "1: crear grupo\n"
                    + "2: eliminar grupo\n"
                    + "3: entrar/salir de un grupo\n"
                    + "4: enviar mensaje\n"
                    + "5: recoger mensaje\n"
                    + "6: mostrar miembros de un grupo\n"
                    + "7: mostrar grupos actuales\n"
                    + "8: terminar ejecución");

            //Leer opción
            opcion = s.nextLine();

            switch (opcion) {
                case "1": //Crear un grupo nuevo
                    // supone que alias y hostname se obtienen al principio del main
                    System.out.println("Creando grupo...");
                    System.out.println("Alias del grupo:");
                    String nuevoGalias = s.nextLine();
                    try {
                        if (c.proxy.createGroup(nuevoGalias, c.alias, localhost, c.cPort) == -1) {
                            System.out.println("ERROR al crear el grupo");
                            continue;
                        }
                    } catch (RemoteException ex) {
                        System.out.println("createGroup(): ERROR de acceso remoto creando grupo");
                    }
                    System.out.println("Grupo " + nuevoGalias + " creado");
                    break;

                case "2": //Borrar grupo
                    System.out.println("eliminando grupo...");
                    String galias;
                    System.out.println("Alias del grupo:");
                    galias = s.nextLine();
                    try {
                        if (!c.proxy.ListMembers(galias).getFirst().equals(c.alias)) {
                            System.out.println("ERROR verificando que se es propietario");
                            continue;
                        }
                    } catch (RemoteException ex) {
                        System.out.println("deleteGroup(): ERROR de acceso remoto verificando propietario");
                    } catch (NullPointerException ex) {
                        System.out.println("deleteGroup(): ERROR, el grupo no existe");
                    }
                    try {
                        if (!c.proxy.removeGroup(galias, c.alias)) {
                            System.out.println("ERROR al borrar grupo");
                        } else {
                            System.out.println("Grupo " + galias + " eliminado");
                        }
                    } catch (RemoteException ex) {
                        System.out.println("deleteGroup(): ERROR de acceso remoto borrando grupo");
                    }

                    break;

                case "3": //Unirse / salir de un grupo
                    String entrarSalir,
                     galiasEntrarSalir;
                    System.out.println("Alias del grupo:");
                    galiasEntrarSalir = s.nextLine();
                    try {
                        if (c.proxy.isMember(galiasEntrarSalir, c.alias) != null) {
                            System.out.println("Ya estás en el grupo " + galiasEntrarSalir + ", salir de él? (s/n)");
                            entrarSalir = s.nextLine();
                            if (entrarSalir.equals("s")) {
                                if (!c.proxy.removeMember(galiasEntrarSalir, c.alias)) {
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
                                if (c.proxy.addMember(galiasEntrarSalir, c.alias, localhost, c.cPort) == null) {
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

                case "4":
                    /* Enviar mensaje*/
                    //Necesitamos saber el nombre del grupo
                    System.out.println("Escribe el nombre del grupo");
                    String gEnviar = s.nextLine();

                    //Comprobar si existe el grupo
                    try {
                        if (c.proxy.findGroup(gEnviar) == -1) {
                            //Si el grupo no se encuentra
                            System.out.println("Grupo " + gEnviar + " no encontrado");
                        } else {
                            //Si el grupo existe, comprobamos si se es miembro
                            GroupMember m = c.proxy.isMember(gEnviar, c.alias);
                            if (m == null) {
                                System.out.println("No eres miembro del grupo " + gEnviar);
                            } else {
                                String msg = s.nextLine();

                                boolean ok = c.proxy.sendGroupMessage(m, msg.getBytes());
                                if (ok) {
                                    System.out.println("Mensaje enviado con éxito");
                                } else {
                                    System.out.println("Error (no remoto) enviando el mensaje");
                                }
                            }
                        }
                    } catch (RemoteException e) {
                        System.out.println("Error remoto");
                    }

                    break;

                case "5":
                    /* Recoger mensaje */
                    String gRecv = "";
                    System.out.println("Introduce el alias del grupo");
                    gRecv = s.nextLine();
                    c.receiveGroupMessage(gRecv);
                    break;

                case "6": //Mostrar miembros de un grupo en específico
                    String nombreGrupo = "";
                    LinkedList<String> namesList;

                    System.out.println("Introduce el alias del grupo");
                    nombreGrupo = s.nextLine();
                    System.out.println("Buscando el grupo con alias \"" + nombreGrupo + "\"");
                    try {
                        namesList = c.proxy.ListMembers(nombreGrupo);
                        if (namesList == null) {
                            System.out.println("El grupo " + nombreGrupo + " no existe");
                        } else {
                            System.out.println(namesList.toString());
                        }
                    } catch (RemoteException ex) {
                        System.out.println("groupMembers(): ERROR de acceso remoto obteniendo listado de miembros");
                    }
                    break;

                case "7": //Mostrar grupos actuales
                    LinkedList<String> l = new LinkedList();
                    try {
                        l = c.proxy.ListGroup();
                        if (l.isEmpty()) {
                            System.out.println("No hay grupos en este servidor");
                        } else {
                            System.out.println(l.toString());
                        }
                    } catch (RemoteException ex) {
                        System.out.println("listGroups(): ERROR de acceso remoto obteniendo listado de grupos");
                    }
                    break;

                case "8": //Salir
                    System.exit(0);

                default: //Intro / opción no válida 
                    System.out.println("Accion incorrecta");
            }
        }

    }

    @Override
    public void DepositMessage(GroupMessage m) throws RemoteException {
        mutex.lock();
        try {
            /* añadir message a cola y despertar procesos bloqueados */
            msgQueue.add(m);
            waiting.signalAll();
        } finally {
            mutex.unlock();
        }
    }

    @Override
    public byte[] receiveGroupMessage(String galias) throws RemoteException {
        GroupMessage msg = null;
        mutex.lock();
        try {
            /* verificar usuario y grupo */
            if (c.proxy.isMember(galias, c.alias) == null) {
                return null;
            }
            /* mientras no haya mensaje que devolver */
            while (msg == null) {
                /* buscar mensajes para grupo galias en la cola */
                for (GroupMessage m : msgQueue) {
                    // si no funciona, comprobar que msg == null
                    if ( msg == null && m.emisor.gid == proxy.findGroup(galias)) {
                        msg = m;
                        System.out.println(">Mensaje ("+ msg.emisor.alias +"): "+ msg.mensaje.toString());
                        msgQueue.remove(m);
                    }
                }
//                if (msg == null) {
//                    try {
//                        waiting.await();
//                    } catch (InterruptedException ex) {
//                        System.out.println("ERROR de concurrencia esperando mensaje");
//                    }
//                }
                System.out.println("");
            }
        } finally {
            mutex.unlock();
        }
        return msg.mensaje;
    }

}
