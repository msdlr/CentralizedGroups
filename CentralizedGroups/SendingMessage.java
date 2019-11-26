/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CentralizedGroups;

import Cliente.ClientInterface;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel
 */
public class SendingMessage extends Thread{
    GroupMessage msg;
    GroupMember dst;
    ObjectGroup grp;  
    //Registro
    Registry reg;
    //Objeto client
    ClientInterface client;
    
    
    public SendingMessage(ObjectGroup grp, GroupMessage msg, GroupMember dst){
        this.msg = msg;
        this.dst = dst;
        this.grp = grp;
    }
    
    
    @Override
    public void run(){        
        try {
            //Obtiene el objeto sobre el que invocar el metodo
            reg = LocateRegistry.getRegistry(dst.hostname, dst.port);
            client = (ClientInterface) reg.lookup(dst.alias); //lookup se usa para tratar como string relativo
            
            Random r = new Random();
            //Genera el tiempo de espera
            int espera = 10 + r.nextInt(11);
            Thread.sleep(espera * 1000);
            //Deposita el msg y notifica al grp
            client.DepositMessage(msg);
            grp.EndSending();
            
        //Tratamiento de exceptiones del método
        } catch (InterruptedException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
