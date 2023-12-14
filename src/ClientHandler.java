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
    private int LIMITE_MESSAGES = 50;

    public ClientHandler(Client client, List<Client> clients, List<Salon> salons) {
        this.client = client;
        this.clients = clients;
        this.salons = salons;
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException {
        out.writeUTF(BibliothequeString.CLEAR);
    }

    public int getIdUtilisateurWithNomInJson(String nom) {
        int id = 0;
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
                // on parcours le data de chaque objet
                JSONObject dataObj = obj.getJSONObject("data");
                if (dataObj.getString("nom").equals(nom)) {
                    id = dataObj.getInt("id");
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return id;
    }

    public boolean verifierIdUtilisateurExiste(int idUtilisateur) {
        try {
            // Charger les messages existants depuis le fichier JSON
            String contenuFichier = new String(Files.readAllBytes(Paths.get(this.FICHIER_JSON)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // Parcourir les messages pour vérifier si l'ID utilisateur existe déjà
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject messageUtilisateur = messagesExistants.getJSONObject(i);
                int id = messageUtilisateur.getInt("idUtilisateur");
                if (id == idUtilisateur) {
                    return true; // L'ID utilisateur existe déjà
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false; // L'ID utilisateur n'existe pas
    }

    public static void supprimerMessagePlusAncien() {
        String FICHIER_JSON = "message.json";
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            LocalDateTime messagePlusAncien = LocalDateTime.MAX; // Initialisation à la date maximale possible
            int indexUtilisateur = -1;
            int indexMessage = -1;

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONArray data = messagesExistants.getJSONObject(i).getJSONArray("data");
                for (int j = 0; j < data.length(); j++) {
                    JSONObject message = data.getJSONObject(j);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime messageDate = LocalDateTime
                            .parse(message.getString("date") + " " + message.getString("heure"), formatter);

                    if (messageDate.isBefore(messagePlusAncien)) {
                        messagePlusAncien = messageDate;
                        indexUtilisateur = i;
                        indexMessage = j;
                    }
                }
            }

            if (indexUtilisateur != -1 && indexMessage != -1) {
                JSONArray messagesUtilisateur = messagesExistants.getJSONObject(indexUtilisateur).getJSONArray("data");
                messagesUtilisateur.remove(indexMessage);
                messagesExistants.getJSONObject(indexUtilisateur).put("data", messagesUtilisateur);

                // Écrire les modifications dans le fichier JSON
                try (FileWriter fichierJson = new FileWriter(FICHIER_JSON)) {
                    fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public int trouverIdUtilisateurParPseudo(String pseudo) {
        System.out.println("pseudo : " + pseudo);
        try {
            // Charger les utilisateurs existants depuis le fichier JSON
            String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));
            JSONArray utilisateursExistants = new JSONArray(contenuFichier);

            // Parcourir les utilisateurs pour trouver l'ID correspondant au pseudo donné
            for (int i = 0; i < utilisateursExistants.length(); i++) {
                JSONObject utilisateur = utilisateursExistants.getJSONObject(i);
                System.out.println(utilisateur);
                if (utilisateur.getString("pseudo").equals(pseudo)) {
                    System.out.println("id : " + utilisateur.getInt("idUtilisateur"));
                    return utilisateur.getInt("idUtilisateur");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        // Si le pseudo n'est pas trouvé, retourner -1 ou une valeur qui indique
        // l'absence de l'utilisateur
        return -1;
    }

    public static int obtenirDernierIdMessagePourUtilisateur(int idUtilisateur) {
        int dernierIdMessage = 0;
        String FICHIER_JSON = "message.json";

        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject utilisateur = messagesExistants.getJSONObject(i);
                if (utilisateur.getInt("idUtilisateur") == idUtilisateur) {
                    JSONArray messagesUtilisateur = utilisateur.getJSONArray("data");
                    if (messagesUtilisateur.length() > 0) {
                        JSONObject dernierMessage = messagesUtilisateur.getJSONObject(messagesUtilisateur.length() - 1);
                        dernierIdMessage = dernierMessage.getInt("idMessage");
                    }
                    break;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return dernierIdMessage;
    }

    public static int obtenirNouvelIdMessage(int idUtilisateur) {
        int dernierIdMessage = obtenirDernierIdMessagePourUtilisateur(idUtilisateur);
        return dernierIdMessage + 1;
    }

    public static int compterMessages() {
        String FICHIER_JSON = "message.json";
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);
            int totalMessages = 0;

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONArray data = messagesExistants.getJSONObject(i).getJSONArray("data");
                totalMessages += data.length();
            }

            return totalMessages;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void ajouterMessage(int idMessage, String contenu, LocalDateTime date, int idUtilisateur) {
        try {
            int cpt = compterMessages();
            if (cpt >= this.LIMITE_MESSAGES) {
                System.out.println(cpt);
                supprimerMessagePlusAncien();
            }
            DateTimeFormatter dtfD = DateTimeFormatter.ofPattern("YYYY/MM/dd");
            DateTimeFormatter dtfH = DateTimeFormatter.ofPattern("HH:mm:ss");
            // Créer un objet JSON représentant le message
            JSONObject message = new JSONObject();
            message.put("idMessage", idMessage);
            message.put("contenu", contenu);
            message.put("date", dtfD.format(date));
            message.put("heure", dtfH.format(date));
            // Charger les messages existants depuis le fichier JSON
            JSONArray messagesExistants = new JSONArray();
            try {
                String contenuFichier = new String(Files.readAllBytes(Paths.get(this.FICHIER_JSON)));
                messagesExistants = new JSONArray(contenuFichier);
            } catch (IOException e) {
                // Le fichier n'existe probablement pas encore, c'est acceptable.
            }

            boolean idExiste = verifierIdUtilisateurExiste(idUtilisateur);

            // Parcourir les messages pour vérifier si l'ID existe déjà
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject messageUtilisateur = messagesExistants.getJSONObject(i);
                int id = messageUtilisateur.getInt("idUtilisateur");
                if (id == idUtilisateur) {
                    // L'ID existe déjà, ajouter simplement le nouveau message
                    JSONArray data = messageUtilisateur.getJSONArray("data");
                    if (data == null) {
                        data = new JSONArray();
                    }
                    data.put(message);
                    messageUtilisateur.put("data", data);
                    idExiste = true;
                    break;
                }
            }

            if (!idExiste) {
                // L'ID n'existe pas encore, créer une nouvelle entrée
                JSONObject messageData = new JSONObject();
                JSONArray data = new JSONArray();
                data.put(message);
                messageData.put("idUtilisateur", idUtilisateur);
                messageData.put("data", data);
                messagesExistants.put(messageData);
            }

            // Enregistrer la liste mise à jour dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter(this.FICHIER_JSON)) {
                fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
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

    public boolean veridMotsDePasseInJson(String motsDePasse) {
        boolean verid = false;
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
                // on parcours le data de chaque objet
                JSONObject dataObj = obj.getJSONObject("data");
                if (dataObj.getString("nom").equals(this.client.getNameClient())) {
                    if (dataObj.getString("motsDePasse").equals(motsDePasse)) {
                        verid = true;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return verid;
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
                    int id = trouverIdUtilisateurParPseudo(nomClient);
                    if (id == -1) {
                        isNameUsed = true;
                        out.writeUTF(BibliothequeString.TRUE);
                    }
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.setNameClient(nomClient);
                        }

                        isNameSet = true;
                        out.writeUTF(BibliothequeString.FALSE);
                    }
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
                out.writeUTF();
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

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                // mettre now en MM/dd HH:mm

                // On lit le message envoyé par le client
                String message = in.readUTF();
                boolean boolleanSlash = message.startsWith("/");
                String boolleanString = String.valueOf(boolleanSlash);

                switch (boolleanString) {
                    case "true":

                        // switch (message) {
                        // case BibliothequeString.COMMANDE_QUIT:
                        // for (Client client : clients) {
                        // if (client.getSocket() == this.client.getSocket()) {
                        // client.getSocket().close();
                        // clients.remove(client);
                        // }
                        // }

                        // case BibliothequeString.COMMANDE_UPTIME:
                        // // dit depuis combien de temps le salon est ouvert
                        // String salon_du_client = this.client.getSalon();
                        // for (Salon salon : salons) {
                        // if (salon.getNomSalon().equals(salon_du_client)) {
                        // out.writeUTF("Le salon " + salon_du_client + " est ouvert depuis "
                        // + salon.tempsEntreCreationEtMaintenant());
                        // }
                        // }
                        // break;

                        // case BibliothequeString.COMMANDE_USER:
                        // // donne le nombre de personne connectée sur le serveur
                        // out.writeUTF("Il y a " + clients.size() + " personnes connectées sur le
                        // serveur");
                        // break;

                        // case "/followers":
                        // break;

                        // case BibliothequeString.COMMANDE_HELP:
                        // out.writeUTF(BibliothequeString.COMMANDE_LIST);
                        // out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                        // + BibliothequeString.COMMANDE_QUIT + BibliothequeStyle.ANSI_RESET
                        // + BibliothequeString.COMMANDE_QUIT_INFO);
                        // out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                        // + BibliothequeString.COMMANDE_UPTIME + BibliothequeStyle.ANSI_RESET
                        // + BibliothequeString.COMMANDE_UPTIME_INFO);
                        // out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                        // + BibliothequeString.COMMANDE_USER + BibliothequeStyle.ANSI_RESET
                        // + BibliothequeString.COMMANDE_USER_INFO);
                        // out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE
                        // + BibliothequeString.COMMANDE_MP + BibliothequeStyle.ANSI_RESET
                        // + BibliothequeString.COMMANDE_MP_INFO);

                        // break;

                        // default:
                        // out.writeUTF(BibliothequeString.COMMANDE_INCONNU + BibliothequeStyle.ANSI_RED
                        // + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_HELP
                        // + BibliothequeStyle.ANSI_RESET);
                        // break;
                        // }
                        break;

                    case "false":
                        String msg_a_envoyer = dtf.format(now) + " [" + this.client.getSalon() + "] " + nomClient
                                + " - " + message;
                        // On enregistre le message dans le fichier JSON
                        System.out.println(
                                " user : " + nomClient + " content : " + message + " date : " + dtf.format(now));
                        System.out.println("dernier id message : " + obtenirDernierIdMessagePourUtilisateur(0));

                        int id = trouverIdUtilisateurParPseudo(nomClient);

                        ajouterMessage(obtenirNouvelIdMessage(id), message, now, id);

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
                            for (Client client : clients) {
                                Socket keySocket = client.getSocket();
                                if (keySocket != this.client.getSocket()
                                        && client.getSalon().equals(this.client.getSalon())) {
                                    DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                    out2.writeUTF(msg_a_envoyer);
                                }
                            }
                        }

                        break;

                    default:
                        break;
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