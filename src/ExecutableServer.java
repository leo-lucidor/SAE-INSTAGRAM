import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.json.JSONArray;
import org.json.JSONObject;



public class ExecutableServer {
    public static int getLastIdInJson(String jsonFile){
        int id = 0;
        try{
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get(jsonFile)));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONObject jsonObject = new JSONObject(contenuFichier);

            // Récupération du tableau "connexion"
            JSONArray connexionArray = jsonObject.getJSONArray("connexion");

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < connexionArray.length(); i++) {
                JSONObject obj = connexionArray.getJSONObject(i);

            // Récupération des valeurs pour chaque objet
            id = obj.getInt("id");
            }
        }catch(Exception e){
            System.out.println("Error reading json file");
        }
        System.out.println("Last id : " + id);
        return id;
    }



    public static void main(String[] args) throws IOException {
    String FICHIER_JSON = "message.json";


    
    // Demande le port
    Scanner sc = new Scanner(System.in);
    System.out.println("Entrez le port : ");
    int port = sc.nextInt();

    // On crée une liste qui va contenir les clients
    List<Client> clients = new ArrayList<Client>();
    System.out.println("Serveur lancé, en attente de connexion...");

    // On crée une liste qui va contenir les salons
    List<Salon> salons = new ArrayList<Salon>();

    JsonHandler JsonHandler = new JsonHandler();


    // On lance un thread pour la lecture des commandes depuis la console
    new Thread(() -> {
        JsonHandler jsonHandlerThread = new JsonHandler();

        while (true) {

            // Lecture des commandes depuis la console
            System.out.println("Entrez une commande ->");
            String commande = sc.nextLine(); // Utilisez nextLine() ici

            // Traitement des commandes spéciales
            if (commande.startsWith("/delete") && commande.contains(" ")) {
                // Extrait l'id du message à supprimer
                try{
                    int idMessageToDelete = Integer.parseInt(commande.split(" ")[1]);
                }
                catch(Exception e){
                    System.out.println("Error parsing id");
                }

                // Appelle une méthode pour supprimer le message
                // supprimerMessage(idMessageToDelete, salons);
                System.out.println("Message supprimé");

            } else if (commande.startsWith("/remove") && commande.contains(" ")) {
                // Extrait le nom de l'utilisateur à supprimer
                String nomUtilisateurToDelete = commande.split(" ")[1];
                String res = jsonHandlerThread.removeAllCompleted(nomUtilisateurToDelete);
                




                // Appelle une méthode pour supprimer l'utilisateur et tous ses messages
                // supprimerUtilisateurEtMessages(nomUtilisateurToDelete, salons);
                System.out.println(res);
            }
            else if (commande.equals("/help")){
                System.out.println("Commandes disponibles :");
                System.out.println("/delete <id> : supprime le message avec l'id spécifié");
                System.out.println("/remove <nom> : supprime l'utilisateur et tous ses messages");
            }
        }
    }).start();

    try (ServerSocket serveur = new ServerSocket(port)) {
        while (true) {
            // On attend une connexion d'un client
            Socket socket = serveur.accept();
            System.out.println("Client connecté");
            Client client = new Client("");
            client.setSocket(socket);
            clients.add(client);

            // Création du salon global
            if (salons.isEmpty()) {
                Salon salon = new Salon("Global");
                salons.add(salon);
            }

            try {
                // Récupération des messages dans le json pour les afficher
                String contenuFichierMessage = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
                JSONArray messages = new JSONArray(contenuFichierMessage);
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject messageObj = messages.getJSONObject(i).getJSONObject("message");
                    String user = messageObj.getString("user");
                    String content = messageObj.getString("content");
                    String date = messageObj.getString("date");
                    System.out.println("" + date + " " + user + " - " + content);
                }
            } catch (Exception e) {
                // TODO: handle exception
            }

            // On lance un thread qui va gérer la connexion avec ce client
            Thread t = new Thread(new ClientHandler(client, clients, salons, JsonHandler));
            t.start();
        }
    } catch (IOException e) {
        e.printStackTrace();
    } finally {
        // Fermez le scanner à la fin de l'utilisation
        sc.close();
    }
}

}
