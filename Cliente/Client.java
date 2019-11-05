/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Cliente;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 *
 * @author Miguel
 */
public class Client extends UnicastRemoteObject implements ClientInterface{
    Client() throws RemoteException {
        
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String command = "";
        boolean menu = true;
        
        /* establecer conexion remota o local */
        System.out.println("Conexión remota o local? (l/<dir. ip>)");
        command = scanner.nextLine();
        if (command.equals("l")) {
            host = "127.0.0.1";
        } else {
            host = command;
        }
        
        /* menu */
        System.out.println("Para consultar todas las acciones disponibles, escribe /?");
        System.out.print(">> ");
        while (menu) {
            command = scanner.next("/*");
            switch (command) {
                case "/?":
                    System.out.println("help");
                    break;
                    
            }
        }
    }
}
