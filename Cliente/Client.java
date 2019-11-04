/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface{
    Client() throws RemoteException {
        
    }
}
