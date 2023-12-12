import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import org.json.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.FileWriter;

public class ClientHandler implements Runnable {
    private Client client;
    private List<Client> clients;
    private List<Salon> salons;
    private String FICHIER_JSON = "message.json";

    public ClientHandler(Client client, List<Client> clients, List<Salon> salons) {
        this.client = client;
        this.clients = clients;
        this.salons = salons;
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException {
        out.writeUTF(BibliothequeString.CLEAR);
    }

    public int getIdUtilisateurWithNomInJson(String nom){
        int id = 0;
        try{
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONObject jsonObject = new JSONObject(contenuFichier);

            // Récupération du tableau "connexion"
            JSONArray connexionArray = jsonObject.getJSONArray("connexion");

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < connexionArray.length(); i++) {
                JSONObject obj = connexionArray.getJSONObject(i);
                // on parcours le data de chaque objet
                JSONObject dataObj = obj.getJSONObject("data");
                if (dataObj.getString("nom").equals(nom)){
                    id = dataObj.getInt("id");
                }
            }
        }
        catch(Exception e){
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return id;
    }

    public void enregistrerMessage(int idMessage, String user, String content,LocalDateTime date ) throws JSONException {
        // Créer un objet JSON représentant le message
        JSONObject data = new JSONObject();
       
        JSONObject id = new JSONObject();
        JSONObject message = new JSONObject();
        message.put("idMessage", idMessage);
        message.put("user", user);
        message.put("content", content);
        message.put("date", date);
        int idUtilisateur = getIdUtilisateurWithNomInJson(user);
        id.put("idUtilisateur", idUtilisateur);
        id.put("data", message);
        
    
        // Charger les messages existants depuis le fichier JSON
        JSONArray messagesExistants = new JSONArray();
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(this.FICHIER_JSON)));
            messagesExistants = new JSONArray(contenuFichier);
        } catch (IOException e) {
            // Le fichier n'existe probablement pas encore, c'est acceptable.
        }
    
    
        // Enregistrer la liste mise à jour dans le fichier JSON
        try (FileWriter fichierJson = new FileWriter(this.FICHIER_JSON)) {
            fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void afficherSalons(DataOutputStream out) throws IOException {
        clearTerminalClient(out);

        // On affiche les salons
        if (salons.size() > 0) {
            out.writeUTF(BibliothequeStyle.ANSI_CYAN + BibliothequeStyle.SOULIGNAGE
                    + BibliothequeString.SALON_DISPONIBLE + BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
            for (Salon salon : salons) {
                out.writeUTF(BibliothequeStyle.ANSI_GREEN + salon + "" + BibliothequeStyle.ANSI_RESET
                        + BibliothequeString.VIDE);
            }
        } else {
            out.writeUTF(BibliothequeStyle.ANSI_CYAN + BibliothequeString.NOTIFICATION_PAS_DE_SALON
                    + BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
        }
        out.writeUTF("" + BibliothequeStyle.ANSI_CYAN + BibliothequeString.REMARQUE_HELP + BibliothequeStyle.ANSI_RESET
                + BibliothequeString.VIDE);
    }

    public String verifName(DataInputStream in, DataOutputStream out) throws IOException {
        // On récupère le nom du client et on vérifie si il est déjà utilisé
        Boolean isNameSet = false;
        String nomClient = BibliothequeString.VIDE;
        while (!isNameSet) {
            nomClient = in.readUTF();
            if (nomClient.length() > 0) {
                Boolean isNameUsed = false;
                for (Client client : clients) {
                    String name = client.getNameClient();
                    if (name.equals(nomClient)) {
                        isNameUsed = true;
                        out.writeUTF(BibliothequeString.TRUE);
                    }
                }
                if (!isNameUsed) {
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.setNameClient(nomClient);
                        }
                    }
                    isNameSet = true;
                    out.writeUTF(BibliothequeString.FALSE);
                }
            }
        }
        return nomClient;
    }

    public void mettreClientDansGlobal(DataOutputStream out) throws IOException {
            for (Salon salon : this.salons) {
                if (salon.getNomSalon().equals("Global")) {
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.setSalon("Global");
                        }
                    }
                    out.writeUTF(BibliothequeString.TP);
                }
            }
        }

    public void run() {
        try {

            // On met le client dans le salon global

            DataInputStream in = new DataInputStream(client.getSocket().getInputStream());
            DataOutputStream out = new DataOutputStream(client.getSocket().getOutputStream());
            // On demande le nom du client

            String nomClient = verifName(in, out);
            // On demande le nom du salon
            mettreClientDansGlobal(out);


            while (true) {
                // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd HH:mm");
                LocalDateTime now = LocalDateTime.now();
                // On lit le message envoyé par le client
                String message = in.readUTF();
                if (!message.startsWith("/")) {
                    String msg_a_envoyer = dtf.format(now) + " [" + this.client.getSalon() + "] " + nomClient + " - "
                            + message;

                    // On enregistre le message dans le fichier JSON
                    System.out.println( "id : " + this.client.getId() + " user : " + nomClient + " content : " + message + " date : " + dtf.format(now));
                    enregistrerMessage( this.client.getId(), client.getNameClient(), message, now);

                    // message privé -> on envoie le message uniquement au client mentionné
                    boolean contientEspace = false;
                    for (int i = 0; i < message.length(); i++) {
                        if (message.charAt(i) == ' ') {
                            contientEspace = true;
                        }
                    }
                    if (message.startsWith("@") && contientEspace) {
                        String pseudo = message.substring(message.indexOf("@") + 1, message.indexOf(" "));
                        for (Client client : clients) {
                            if (pseudo.equals(client.getNameClient())) {
                                DataOutputStream out3 = new DataOutputStream(client.getSocket().getOutputStream());
                                out3.writeUTF("MP" + this.client.getNameClient() + " vous chuchotte : "
                                        + message.substring(message.indexOf(" ") + 1));
                            }
                        }
                    } else {

                        // if (message.startsWith("!follow") && contientEspace) {
                        // String pseudo = message.substring(message.indexOf("!follow")+8,
                        // message.indexOf(" "));
                        // for (Client client: clients) {
                        // DataOutputStream out7 = new
                        // DataOutputStream(client.getSocket().getOutputStream());
                        // out7.writeUTF("res = " + pseudo.equals(client.getNameClient()) + " pseudo " +
                        // pseudo + " client " + client.getNameClient());
                        // if (pseudo.equals(client.getNameClient())) {
                        // DataOutputStream out3 = new
                        // DataOutputStream(client.getSocket().getOutputStream());
                        // out3.writeUTF("MP" + this.client.getNameClient() + " vous suit");
                        // if (this.followers.containsKey(client.getNameClient())){
                        // this.followers.put(client.getNameClient(),
                        // this.followers.get(client.getNameClient())+1);
                        // }else{
                        // this.followers.put(client.getNameClient(), 1);
                        // }
                        // }
                        // }
                        // }
                        // else {
                        // On envoie le message à tous les clients
                        for (Client client : clients) {
                            Socket keySocket = client.getSocket();
                            if (keySocket != this.client.getSocket()
                                    && client.getSalon().equals(this.client.getSalon())) {
                                DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                out2.writeUTF(msg_a_envoyer);
                            }
                        }
                    }
                } else if (message.startsWith("/")) {

                    switch (message) {
                        case BibliothequeString.COMMANDE_QUIT:
                        for (Client client : clients) {
                            if (client.getSocket() == this.client.getSocket()) {
                                client.getSocket().close();
                                clients.remove(client);
                            }
                        }

                        case BibliothequeString.COMMANDE_UPTIME:
                            // dit depuis combien de temps le salon est ouvert
                            String salon_du_client = this.client.getSalon();
                            for (Salon salon : salons) {
                                if (salon.getNomSalon().equals(salon_du_client)) {
                                    out.writeUTF("Le salon " + salon_du_client + " est ouvert depuis "
                                            + salon.tempsEntreCreationEtMaintenant());
                                }
                            }
                            break;

                        case BibliothequeString.COMMANDE_USER:
                            // donne le nombre de personne connectée sur le serveur
                            out.writeUTF("Il y a " + clients.size() + " personnes connectées sur le serveur");
                            break;

                        case "/followers":
                            break;

                        case BibliothequeString.COMMANDE_HELP:
                            out.writeUTF(BibliothequeString.COMMANDE_LIST);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                                    + BibliothequeString.COMMANDE_QUIT + BibliothequeStyle.ANSI_RESET
                                    + BibliothequeString.COMMANDE_QUIT_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                                    + BibliothequeString.COMMANDE_UPTIME + BibliothequeStyle.ANSI_RESET
                                    + BibliothequeString.COMMANDE_UPTIME_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                                    + BibliothequeString.COMMANDE_USER + BibliothequeStyle.ANSI_RESET
                                    + BibliothequeString.COMMANDE_USER_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                                    + BibliothequeString.COMMANDE_MP + BibliothequeStyle.ANSI_RESET
                                    + BibliothequeString.COMMANDE_MP_INFO);

                            break;

                        default:
                            out.writeUTF(BibliothequeString.COMMANDE_INCONNU + BibliothequeStyle.ANSI_RED
                                    + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_HELP
                                    + BibliothequeStyle.ANSI_RESET);
                            break;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println(BibliothequeString.NOTIFICATION_DECONNEXION);
            for (Client client : clients) {
                Socket keySocket = client.getSocket();
                if (keySocket == this.client.getSocket()) {
                    clients.remove(client);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println(BibliothequeString.NOTIFICATION_ERREUR_THREAD);
        }
    }
}