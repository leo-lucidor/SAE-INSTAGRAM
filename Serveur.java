import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Serveur {
    public static void main(String[] args) {
        try {
        ServerSocket socketServer = new ServerSocket(4444);
        Socket socketClient = socketServer.accept();
        System.out.println("connexion d'un client");
        socketClient.close();
        socketServer.close();
        }catch (IOException e) {
        e.printStackTrace();
        }
    }
}   