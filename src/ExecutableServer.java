import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.json.*;

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
                
                try {
                    // Lecture du contenu du fichier JSON dans une chaîne
                    String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));

                    // Création d'un objet JSON à partir de la chaîne lue
                    JSONObject jsonObject = new JSONObject(contenuFichier);

                    // Récupération du tableau "connexion"
                    JSONArray connexionArray = jsonObject.getJSONArray("connexion");

                    // Parcours des éléments du tableau "connexion"
                    for (int i = 0; i < connexionArray.length(); i++) {
                        JSONObject obj = connexionArray.getJSONObject(i);

                    // Récupération des valeurs pour chaque objet
                    String id = obj.getString("id");
                    String nom = obj.getString("nom");

                    // Utilisation des valeurs récupérées
                    System.out.println("ID : " + id + ", Nom : " + nom);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    
                }
            


                System.out.println("Client connecté");
                Client client = new Client("");
                client.setSocket(socket);


                clients.add(client);

                // creation du salon global
                if (salons.size() == 0) {
                    Salon salon = new Salon("Global");
                    salons.add(salon);
                }                

                // On lance un thread qui va gérer la connexion avec ce client
                Thread t = new Thread(new ClientHandler(client, clients, salons));
                t.start();
            }
        }
    }
}
