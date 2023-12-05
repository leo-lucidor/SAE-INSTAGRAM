import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class ExecutableServer {
    public static void main(String[] args) throws IOException {
        // demande le port
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez le port : ");
        int port = sc.nextInt();
        sc.close();

        try (ServerSocket serveur = new ServerSocket(port)) {
            // On crée une liste qui va contenir les clients
            List<Client> clients = new ArrayList<Client>();
            System.out.println("Serveur lancé, en attente de connexion...");

            // On crée une liste qui va contenir les salons
            List<Salon> salons = new ArrayList<Salon>();


            while(true){
                // On attend une connexion d'un client
                Socket socket = serveur.accept();
                System.out.println("Client connecté");
                Client client = new Client("");
                client.setSocket(socket);
                clients.add(client);

                // On lance un thread qui va gérer la connexion avec ce client
                Thread t = new Thread(new ClientHandler(client, clients, salons));
                t.start();
            }
        }
    }
}
