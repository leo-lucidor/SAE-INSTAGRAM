import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import org.json.*;
import java.util.ArrayList;
import java.nio.file.Paths;
import java.nio.file.Files;



public class ClientHandler implements Runnable {
    private Client client;
    private List<Client> clients;
    private List<Salon> salons;
    private JsonHandler jsonHandler;

    public ClientHandler(Client client, List<Client> clients, List<Salon> salons, JsonHandler jsonHandler) {
        this.client = client;
        this.clients = clients;
        this.salons = salons;
        this.jsonHandler = jsonHandler;
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException {
        out.writeUTF(BibliothequeString.CLEAR);
    }

    public List<String> getListSuivi(String nomClient){
        List<String> followers = new ArrayList<>();
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                JSONArray followersObj = obj.getJSONArray("followers");
                for (int j =0; j < followersObj.length(); j++){
                    if (followersObj.getString(j).equals(nomClient)){
                        followers.add(this.jsonHandler.retrouverNomClientByIdInJson(obj.getInt("idUtilisateur")));
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return followers;
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
                    int id = this.jsonHandler.trouverIdUtilisateurParPseudo(nomClient);
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
                        if (message.startsWith("/like") && message.contains(" ")) {
                                // Découper le message pour extraire le pseudo et l'id du message
                                int indexOfSlash = message.indexOf("/");
                                int indexOfSpace = message.indexOf(" ");

                                int idMessage = Integer.parseInt(message.split(" ")[1]);
                                String msg_a_envoyer = " Vous avez liké le message n°" + message.substring(indexOfSpace + 1);
                                
                                for (Client client : clients) {
                                        Socket keySocket = client.getSocket();
                                        if (keySocket != this.client.getSocket()
                                                && client.getSalon().equals(this.client.getSalon())) {
                                            DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                            out2.writeUTF(msg_a_envoyer);
                                        }
                                    }


                                // Incrémenter le nombre de likes dans le fichier JSON
                                this.jsonHandler.incrementeLikeInJson(idMessage, this.client.getNameClient());
                                }

                    if (message.startsWith("/nblike") && message.contains(" ")){
                        int indexOfSlash = message.indexOf("/");
                        int indexOfSpace = message.indexOf(" ");
                        int idMessage = Integer.parseInt(message.split(" ")[1]);
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        String msg_a_envoyer = "Le nombre de like du message n°" + idMessage + " est de : " + this.jsonHandler.nbLikeMessageInJson(idMessage);
                        out4.writeUTF(msg_a_envoyer);

                    }
                    if (message.startsWith("/follow") && message.contains(" ")){
                        int indexOfSlash = message.indexOf("/");
                        int indexOfSpace = message.indexOf(" ");
                        String pseudo = message.substring(indexOfSlash + 8);
                        if (!pseudo.equals(this.client.getNameClient())){
                        String res = this.jsonHandler.addFollowerInJson(this.client.getNameClient(), pseudo);
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        out4.writeUTF(res);
                        }else{
                            DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                            out4.writeUTF("Vous ne pouvez pas vous suivre vous même");
                        
                        }

                    }

                    if (message.startsWith("/unfollow") && message.contains(" ")){
                        int indexOfSlash = message.indexOf("/");
                        int indexOfSpace = message.indexOf(" ");
                        String pseudo = message.substring(indexOfSlash + 10);
                        String msg_a_envoyer = this.jsonHandler.removeFollowerInJson(this.client.getNameClient(), pseudo);
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        out4.writeUTF(msg_a_envoyer);
                    }

                    if (message.startsWith("/delete") && message.contains(" ")){
                        int indexOfSlash = message.indexOf("/");
                        int indexOfSpace = message.indexOf(" ");
                        int idMessage = Integer.parseInt(message.substring(indexOfSlash + 8));
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        String msg_a_envoyer = this.jsonHandler.supprimerMessageWithIdmessageInJson(idMessage, this.client.getNameClient());
                        out4.writeUTF(msg_a_envoyer);
                    }

                    if (message.startsWith("/get")){
                        int indexOfSlash = message.indexOf("/");
                        int indexOfSpace = message.indexOf(" ");
                        int idMessage = Integer.parseInt(message.substring(indexOfSlash + 5));
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        String msg_a_envoyer = this.jsonHandler.getMessageWithIdmessageInJson(idMessage);
                        out4.writeUTF(msg_a_envoyer);
                    }

                    if(message.startsWith("/help")){
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        String msg_a_envoyer = BibliothequeStyle.ANSI_RED+"\n Commandes disponibles :"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+"\n \n /like <idMessage> :"+BibliothequeStyle.ANSI_CYAN+" permet de liker un message "+BibliothequeStyle.ANSI_RESET+"\n"+BibliothequeStyle.ANSI_BLUE+" /nblike <idMessage> : "+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+" permet de connaitre le nombre de like d'un message "+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+"\n /follow <pseudo> :"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+" permet de suivre un utilisateur"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+" \n /unfollow <pseudo> : "+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+" permet de ne plus suivre un utilisateur \n"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+" /delete <idMessage> :"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+" permet de supprimer un message \n "+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+"/get <idMessage> : "+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+"permet de récupérer un message \n"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_BLUE+" /help :"+BibliothequeStyle.ANSI_RESET+BibliothequeStyle.ANSI_CYAN+" permet d'afficher les commandes disponibles"+BibliothequeStyle.ANSI_RESET;
                        out4.writeUTF(msg_a_envoyer);
                    }

                    if(message.startsWith("/suivi")){
                        DataOutputStream out4 = new DataOutputStream(client.getSocket().getOutputStream());
                        String msg_a_envoyer = "Vous suivez : " + this.getListSuivi(this.client.getNameClient());
                        out4.writeUTF(msg_a_envoyer);
                    }




                    break;
                    case "false":
                      
                        int id = this.jsonHandler.trouverIdUtilisateurParPseudo(nomClient);
                        int nouvelIdMessage = this.jsonHandler.obtenirNouvelIdMessage();
                        this.jsonHandler.ajouterMessage(nouvelIdMessage, message, now, id, null);

                        String msg_a_envoyer = dtf.format(now) + " [" + this.client.getSalon() + "] | [ID Message : " + nouvelIdMessage + "] " + nomClient + " : " + message;
                        // On enregistre le message dans le fichier JSON
                        System.out.println(" user : " + nomClient + " content : " + message + " date : " + dtf.format(now));


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
            e.printStackTrace();
            System.out.println(BibliothequeString.NOTIFICATION_ERREUR_THREAD);
        }
    }
}