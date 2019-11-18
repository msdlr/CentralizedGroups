package CentralizedGroups;

import CentralizedGroups.GroupServerInterface;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Client extends UnicastRemoteObject{

    public Client() throws RemoteException {
        // Se exporta: puede atender peticiones //Se exporta como servidor (para callbacks de practicas posteriores)
        super(); 
    }

    
    public static void main(String[] args) throws UnknownHostException, IOException{
        //Se asigna el fichero con las politicas de segurida
        System.setProperty("java.security.policy", "/home/pwnage/NetBeansProjects/CentralizedGroups/src/Cliente/seguridad.txt");
        
        //En caso de no existir ningun gestor de seguridad creamos uno.
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        
        //Crea un registro para el puerto establecido,
        //en este caso, para el puerto 1099.
        Registry registry = LocateRegistry.getRegistry(1099); //Creo que esto es necesario para P4
        
        try {
            //Creamos un objeto de la clase cliente.
            Client cliente = new Client();
                        
            //DEBE LOCALIZARSE EL REGISTRO Y
            // OBTENER LA REFERENCIA AL SERVIDOR.
            //INVOCA AL SERVIDOR
            
            //Buscamos el servidor en el registro, para ello le pasamos la Ip del Servidor.
            //La ip es de la máquina a la que se quiere acceder, es decir, quien va a hacer de Servidor.
            //LOCALIZAMOS EL SERVIDOR EN EL REGISTRO PARA INVOCAR AL SERVIDOR
            GroupServerInterface interfaz = (GroupServerInterface) Naming.lookup("//127.0.0.1/GroupServer");
                                // ¿Por qué no se puede con (sin embargo al hacerlo en local si funcioona, por qué?
             //                   GroupServerInterface interfaz = (GroupServerInterface) registry.lookup("GroupServer");//??

            
            //GroupServerInterface remoto = (GroupServerInterface) Naming.lookup("GroupServer");
            
            int borrarop=0;
            int op=1,gid;
            String alias_propio, alias_miembro,galias;
            String hostname;
            
            Scanner scanner = new Scanner (System.in);
            Scanner sc_galias = new Scanner (System.in);             
            Scanner sc_opcion = new Scanner (System.in);
            Scanner sc_grupo = new Scanner (System.in);
            Scanner sc_hostname = new Scanner (System.in);
            Scanner sc_gid = new Scanner (System.in);
            
            //Nos solicita la introduccíón de un alias como cliente.
            System.out.println("Introduce tu alias: ");
            alias_propio = scanner.next();
            //recoge el hostname automaticamente
            hostname=InetAddress.getLocalHost().getHostName();
            System.out.println("Tu hostname es: "+hostname+"\n");
            
            //Mostramos el menú que contendrá opciones como añadir grupo o miembro de grupo,
            //eliminar grupo o miembro de grupo, bloquear o desbloquear altas y bajas.
            while(op != 9){
                System.out.println("\n----MENU----\n"+
                        "1.- Crear grupo\n" +
                        "2.- Eliminar grupo\n" +
                        "3.- Añadir miembro a grupo\n" +
                        "4.- Eliminar miembro de grupo\n" +
                        "5.- Bloquear altas y bajas\n" +
                        "6.- Desbloquear altas y bajas\n" +
                        "7.- Mostrar grupos\n" +
                        "8.- Mostrar miembros de un grupo\n" +
                        "9.- Terminar\n" +
                        "\n Elige una opción:");
                try{
                op = Integer.parseInt(sc_opcion.nextLine());
                }catch(NumberFormatException e){
                    op=0;
                }
                
                
                switch(op){
                    
                    case 1:
                        //Seleccionando esta opción, podremos crear un nuevo grupo.
                        System.out.println("\nIntroduce el nombre que deseas poner al grupo: ");
                        galias = sc_grupo.next();                        
                        if(interfaz.createGroup(galias, alias_propio, java.net.Inet4Address.getLocalHost().getHostName())>=0){
                            gid = interfaz.createGroup(galias, alias_propio, hostname);
                            if (gid == -2){
                                System.out.println("No se puede crear grupo: ya existe otro con el mismo nombre.\n");
                            }else {
                                System.out.println("Grupo creado. Id: " +gid+"");
                                System.out.println("Propietario: " + alias_propio +".\n");
                            }
                           // System.out.println("\nEl grupo se ha creado correctamente con id = " + gid + "\n");
                        }else{
                            System.out.println("\nSe ha producido un error al crear el grupo\n");
                        }
                        break;
                        
                    case 2:
                        System.out.println("\nQuieres eliminar por: \n 1-Nombre \n 2-id\n");
                        borrarop = Integer.parseInt(sc_opcion.nextLine());
                        
                        switch (borrarop) {
                            case 1:
                                System.out.println("\nIntroduce el nombre del grupo para eliminarlo");
                                galias = sc_galias.nextLine();
                                if(interfaz.removeGroup(galias, alias_propio)){
                                    System.out.println("\nEl grupo se ha eliminado correctamente \n");
                                }else{
                                    System.out.println("\nSe ha producido un error al eliminar el grupo \n");
                                }
                                break;                           
                            case 2:    
                                String galiass;
                                System.out.println("\nIntroduce el id del grupo para eliminarlo: ");
                                gid = sc_gid.nextInt();                               
                                galiass = interfaz.findGroup(gid);
                                
                                if(interfaz.removeGroup(galiass, alias_propio)){
                                    System.out.println("\nEl grupo se ha eliminado correctamente. \n");
                                } else {
                                    System.out.println("\n Se ha producido un error al eliminar el grupo. \n");
                                }
                                break;
                        }
                        break;
                        
                        //Seleccionando esta opción, podremos eliminar un grupo ya existente.
                        
                        
                    case 3:                      
                        //Seleccionando esta opción, podremos añadir un nuevo miembro
                        //a un grupo ya existente.
                    //    String hostname;
                        System.out.println("\nIntroduce el alias del grupo");
                        galias = sc_galias.nextLine();              
                        gid = interfaz.findGroup(galias);
                        System.out.println("Introduce el hostname del miembro");
                        hostname = sc_hostname.nextLine();
                        System.out.println("Introduce el alias del miembro a añadir");
                        alias_miembro = scanner.next();
                        
                        if(interfaz.addMember(galias, alias_miembro, hostname)!=null){ //&& (interfaz.AllowMembers(galias))){
                            System.out.println("\nEl miembro se ha añadido correctamente \n");
                        
                        }else{
                            System.out.println("\nSe ha producido un error al añadir el miembro \n");
                        }
                        break;
                        
                        
                        
                        
                        
                        
                    case 4:
                        //Seleccionando esta opción, podremos eliminar un miembro existente
                        //dentro de un grupo ya existente.
                        System.out.println("\nIntroduce el alias del grupo");
                        galias = sc_galias.nextLine();              
                        gid = interfaz.findGroup(galias);
                        System.out.println("\nIntroduce el alias del miembro a eliminar");
                        alias_miembro = scanner.next();
                                               
                       
                        if(!alias_miembro.equals(alias_propio)){
                            if(interfaz.removeMember(galias, alias_miembro)){
                                System.out.println("\n El miembro se ha borrado correctamente \n");
                            }else{
                                System.out.println("\nSe ha producido un error al borrar el miembro \n");
                            }
                        } else System.out.println("No se puede eliminar al propietario.");
                            break;
                       
                            
                            
                    case 5:
                        //Seleccionando esta opción, bloquearemos la posibilidad de añadir miembros
                        //o eliminar miembros dentro del grupo existente que se ha indicado.
                        System.out.println("Introduce el alias del grupo para bloquear miembros");
                        galias = sc_galias.nextLine();              
                        gid = interfaz.findGroup(galias);
                        if(interfaz.StopMembers(galias)!=false && interfaz.isMember(galias, alias_propio)!=null){
                            System.out.println("\n Miembros bloqueados correctamente \n");
                        }else{
                            System.out.println("\nSe ha producido un error al bloquear los miembros \n");
                        }
                        break;
                        
                    case 6:
                        //Seleccionando esta opción, permitiremos de nuevo la posibilidad de añadir miembros
                        //o eliminar miembros dentro del grupo existente que se ha indicado.
                        System.out.println("Introduce el alias del grupo para desbloquear miembros");
                        galias = sc_galias.nextLine();              
                        gid = interfaz.findGroup(galias);
                        if(interfaz.AllowMembers(galias)!=false && interfaz.isMember(galias, alias_propio)!=null){
                            System.out.println("\n Miembros desbloqueados correctamente \n");
                        }else{
                            System.out.println("\nSe ha producido un error al desbloquear los miembros \n");
                        }
                        break;
                        
                    case 7:   //7. Mostrar grupos
                        System.out.println(interfaz.ListGroup());
                        System.out.println("\n");
                        break;
                        
                    case 8:   //8. Mostrar miembros de un grupo
                        System.out.print("Nombre del grupo: ");
                        galias = sc_galias.next();
                        if (interfaz.findGroup(galias)!=-1){
                            System.out.println(interfaz.ListMembers(galias));
                            System.out.println("\n");
                        }else
                            System.out.println("El grupo "+galias+" no existe.\n");
                        break;    
                                                                      
                    case 9:
                        //Seleccionando esta opción, daremos por concluido el programa.
                        System.out.println("Terminado");
                        //Para finalizar su ejecución el cliente debe darse de baja como servidor
                        UnicastRemoteObject.unexportObject(cliente, true);
                        break;
                    default:
                        System.out.println("Esa opción no existe, elija una de las opciones disponibles");
                        break;
                }	
            }
        } catch (ConnectException ex){
                System.out.println("Hay que cambiar el hostname del Cliente");
        } catch (RemoteException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

}