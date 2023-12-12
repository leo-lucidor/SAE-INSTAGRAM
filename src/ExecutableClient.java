import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.json.JSONArray;
import org.json.JSONObject;


public class ExecutableClient {

    
    public static int getLastIdInJsonInConnexion() {
        int lastId = 0;
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
                // Récupération de l'ID à partir de l'objet "data"
                JSONObject dataObj = obj.getJSONObject("data");
                int id = dataObj.getInt("id");
                if (id > lastId) {
                    lastId = id;
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        System.out.println("Dernier ID : " + lastId);
        return lastId;
    }
    
    

    public static void enregistrerClienInJson( Client client, String jsonFile){
        try{
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get(jsonFile)));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONObject jsonObject = new JSONObject(contenuFichier);

            // Récupération du tableau "connexion"
            JSONArray connexionArray = jsonObject.getJSONArray("connexion");

            // Création d'un objet JSON pour stocker les informations du nouveau client
            JSONObject newClient = new JSONObject();
            newClient.put("id", client.getId());
            newClient.put("nom", client.getNameClient());

            // Ajout du nouvel objet JSON au tableau "connexion"
            connexionArray.put(newClient);

            // Ajout du tableau "connexion" dans l'objet JSON
            jsonObject.put("connexion", connexionArray);

            // Ecriture dans le fichier
            Files.write(Paths.get(jsonFile), jsonObject.toString().getBytes());
        }catch(Exception e){
            System.out.println("Error reading json file");
        }
    }


    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Client client1 = new Client("", getLastIdInJsonInConnexion()+1);
        Boolean connecte = true;
        try {
            client1.startClient();
            DataInputStream in = new DataInputStream(client1.getSocket().getInputStream());
            DataOutputStream out = new DataOutputStream(client1.getSocket().getOutputStream());
            try {
                while (connecte) {
                    String message = sc.nextLine();
                    if (message.length() > 0) {
                        out.writeUTF(message);
                    }
                }
            } catch (Exception e) {
                System.out.println("\u001b[31;1mDéconnexion forcé du serveur, fermeture du client" + BibliothequeStyle.ANSI_RESET);
            }
        } catch (Exception e) {
            System.out.println("\u001b[31;1mErreur lors de la connexion au serveur" + BibliothequeStyle.ANSI_RESET);
            connecte = false;
        }
    }
}