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
                Client client = new Client("", getLastIdInJson("connexion.json")+1);
                client.setSocket(socket);


                clients.add(client);

                


                // creation du salon global
                if (salons.size() == 0) {
                    Salon salon = new Salon("Global");
                    salons.add(salon);
                }  


                try {
                 
                    //recuperation des messages dans le json pour les afficher
                    String contenuFichierMessage = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
                    JSONArray messages = new JSONArray(contenuFichierMessage);
                    for (int i = 0; i < messages.length(); i++) {
                        JSONObject messageObj = messages.getJSONObject(i).getJSONObject("message");
                        String user = messageObj.getString("user");
                        String content = messageObj.getString("content");
                        String date = messageObj.getString("date");
                        System.out.println(""+date+" "+user + " - " +content);
                    }
                } catch (Exception e) {
                    // TODO: handle exception
                }

                // On lance un thread qui va gérer la connexion avec ce client
                Thread t = new Thread(new ClientHandler(client, clients, salons));
                t.start();
            }
        }
    }
}
