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
    //Para identificar grupo, miembro de destino y mensaje
    ObjectGroup grp;
    GroupMessage msg;
    GroupMember dst;
    //Registro y proxy para RMI
    Registry reg;
    ClientInterface proxy;
    
    public SendingMessage(ObjectGroup grp, GroupMessage msg, GroupMember dst){
        this.msg = msg;
        this.dst = dst;
        this.grp = grp;
    }
    
    @Override
    public void run(){        
        try {
            //Conseguimos el registro y el proxy
            reg = LocateRegistry.getRegistry(dst.hostname, dst.port);
            proxy = (ClientInterface) reg.lookup(dst.alias);
            
            Random r = new Random();
            //Se genera un tiempo de espera aleatorio entre 10 y 30 (milisegundos!)
            int espera = 10 + r.nextInt(11);
            Thread.sleep(espera * 1000);
            //Deposita el mensaje, notifica al grupo
            proxy.DepositMessage(msg);
            grp.EndSending();
            
        //Excepciones
        } catch (InterruptedException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NotBoundException ex) {
            Logger.getLogger(SendingMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
