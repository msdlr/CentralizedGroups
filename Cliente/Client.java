/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.rmi.CORBA.Stub;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface{
    Client() throws RemoteException {
        //Localizar el servidor en el registro
        String host=null;
        //Registro donde buscar el servidor
        Registry registro; 
        //Proxy para los métodos del servidor
        Stub GroupServerInterface; 
    }
}
