/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import CentralizedGroups.GroupServerInterface;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import static javafx.application.Platform.exit;

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
    
    public static void main(String[] args) throws UnknownHostException, IOException{
        
        //Asignar fichero de política de seguridad
        System.setProperty("java.security.policy", "D:\\DOC\\NetBeansProjects\\CentralizedGroups\\src\\Cliente\\seguridad.txt");
        
        //Crear gestor de seguridad si no hay ninguno
        if (System.getSecurityManager() == null) System.setSecurityManager(new SecurityManager());
        
        //Lanzar el registro en el puerto 1099
        try{
            Registry registro = LocateRegistry.getRegistry(1099);
            System.out.println("Registro (local) lanzado correctamente");
        }catch(RemoteException e){ System.out.println("Error lanzando el registro"); }
        
        //Creamos el objeto de tipo Cliente
        Client c;
        c = new Client();
        
        //Objeto del tipo de la interfaz -> proxy
        GroupServerInterface proxy = null;
        

        //Conexión local / remota
        System.out.println("Local/IP");
        
        Scanner s = new Scanner(System.in);
        String ip = s.nextLine();
        String url=null;
        
        //Si no introducimos IP conectamos con el servidor local
        if("".equals(ip)){
            ip = "192.168.0.28";
        }
        
        url = "rmi://"+ip+"/GroupServer";
        System.out.println("Buscando servidor: "+url);
        
        System.setProperty("java.rmi.server.hostname","192.168.0.28");
        
        try {
            proxy = (GroupServerInterface) Naming.lookup(url);
            System.out.println("PROXY OBTENIDO CORRECTAMENTE");
        } catch (NotBoundException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Name not bound");
            exit();
        } catch (MalformedURLException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error en la direccion");
            exit();
        } catch (RemoteException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se ha podido contactar con el registro");
            exit();
        } catch (java.security.AccessControlException ex) {
            //Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Error de seguridad (revisar pathname del fichero de políticas)");
            exit();
        }
        
        System.out.println("Grupos del servidor:");
        
        System.out.println(proxy.ListGroup().toString());;
        
        System.out.println("asdasd");
        
    }
    
    
}
