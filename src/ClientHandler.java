import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.time.format.DateTimeFormatter; 
import java.time.LocalDateTime; 

public class ClientHandler extends Thread{
    private Client client;
    private List<Client> clients;
    private List<Salon> salons;
    private HashMap<String, Integer> followers;

    public ClientHandler(Client client, List<Client> clients, List<Salon> salons){
        this.client = client;
        this.clients = clients;
        this.salons = salons; 
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException{
        out.writeUTF(BibliothequeString.CLEAR);
    }

    public void afficherSalons(DataOutputStream out) throws IOException{
        clearTerminalClient(out);

        // On affiche les salons
        if (salons.size() > 0) {
            out.writeUTF(BibliothequeStyle.ANSI_CYAN +BibliothequeStyle.SOULIGNAGE + "Salons disponibles :"+ BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
            for (Salon salon : salons) {
                out.writeUTF("\u001b[32m-"+salon+""+ BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
            }
        } else {
            out.writeUTF(BibliothequeStyle.ANSI_CYAN + "Il n'y a pas de salon, créez-en un !"+ BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
        }
        out.writeUTF(BibliothequeStyle.ANSI_CYAN + "(si vous faites /quit ici, vous serez déconnecté)"+ BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
    }


    public String verifName(DataInputStream in, DataOutputStream out) throws IOException{
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

    public void changerSalon(DataInputStream in, DataOutputStream out) throws IOException{
        // On récupère le nom du salon et on vérifie si il existe
        // si il existe, on vérifie si le client est déjà dedans
        // si il n'existe pas, on le crée
        Boolean isSalonSet = false;
        String nomSalon = BibliothequeString.VIDE;
        afficherSalons(out);
        // tant que le salon n'est pas défini
        while (!isSalonSet) {
            // on récupère le nom du salon
            nomSalon = in.readUTF();

            // si le nom du salon n'est pas vide
            if (nomSalon.length() > 0) {

                // si le client envoie /quit on le déconnecte
                if (nomSalon.equals(BibliothequeString.COMMANDE_QUIT)) {
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.getSocket().close();
                            clients.remove(client);
                        }
                    }
                }

                // si il y existe des salons
                if (salons.size() > 0) {
                    //si le salon existe déja
                    Boolean isSalonExist = false;
                    for (Salon salon: this.salons){
                        if (salon.getNomSalon().equals(nomSalon)){
                            isSalonExist = true;
                            for (Client client : clients) {
                                if (client.getSocket() == this.client.getSocket()) {
                                    client.setSalon(nomSalon);
                                }
                            }
                            isSalonSet = true;
                            out.writeUTF(BibliothequeString.TP);
                        }
                    }
                    // si le salon n'existe pas
                    if (!isSalonExist) {
                        // on crée le salon
                        Salon salon = new Salon(nomSalon);
                        salons.add(salon);
                        for (Client client : clients) {
                            if (client.getSocket() == this.client.getSocket()) {
                                client.setSalon(nomSalon);
                            }
                        }
                        isSalonSet = true;
                        out.writeUTF(BibliothequeString.NEW);
                    }
                } else {
                    // si il n'y a pas de salon
                    // on crée le salon
                    Salon salon = new Salon(nomSalon);
                    salons.add(salon);
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.setSalon(nomSalon);
                        }
                    }
                    isSalonSet = true;
                    out.writeUTF(BibliothequeString.NEW);
                }
            }
        }
        out.writeUTF(BibliothequeStyle.ANSI_CYAN + BibliothequeString.NOTIFICATION_SALON + nomSalon+ BibliothequeStyle.ANSI_RESET + BibliothequeString.VIDE);
    }

    public DataInputStream dis(Client client) throws IOException{
        DataInputStream res = new DataInputStream(client.getSocket().getInputStream());
        return res;
    }

    public DataOutputStream dos(Client client) throws IOException{
        DataOutputStream res = new DataOutputStream(client.getSocket().getOutputStream());
        return res;
    }

    public void run(){
        try {
            DataInputStream in = dis(client);
            DataOutputStream out = dos(client);
            // On demande le nom du client
            String nomClient = verifName(in,out);
            // On demande le nom du salon
            changerSalon(in,out);
            
            while(true){
                // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM/dd HH:mm");

                LocalDateTime now = LocalDateTime.now();
                // On lit le message envoyé par le client
                String message = in.readUTF();
                if (!message.startsWith("/")) {
                    String msg_a_envoyer = dtf.format(now)+ " ["+this.client.getSalon()+"] "+nomClient+ " - " + message;
                    System.out.println(msg_a_envoyer);

                    // message privé -> on envoie le message uniquement au client mentionné
                    boolean contientEspace = false;
                    for (int i=0; i<message.length(); i++) {
                        if (message.charAt(i) == ' ') {
                            contientEspace = true;
                        }
                    }
                    if (message.startsWith("@") && contientEspace) {
                        String pseudo = message.substring(message.indexOf("@")+1, message.indexOf(" "));
                        for (Client client: clients) {
                            if (pseudo.equals(client.getNameClient())) {
                                DataOutputStream out3 = new DataOutputStream(client.getSocket().getOutputStream());
                                out3.writeUTF("MP" + this.client.getNameClient() + " vous chuchotte : " + message.substring(message.indexOf(" ")+1));
                            }
                        }
                    }else{
                        
                        // if (message.startsWith("!follow") && contientEspace) {
                        //     String pseudo = message.substring(message.indexOf("!follow")+8, message.indexOf(" "));
                        //     for (Client client: clients) {
                        //         DataOutputStream out7  = new DataOutputStream(client.getSocket().getOutputStream());
                        //         out7.writeUTF("res = " + pseudo.equals(client.getNameClient()) + " pseudo " + pseudo + " client " + client.getNameClient());
                        //         if (pseudo.equals(client.getNameClient())) {
                        //             DataOutputStream out3 = new DataOutputStream(client.getSocket().getOutputStream());
                        //             out3.writeUTF("MP" + this.client.getNameClient() + " vous suit");
                        //             if (this.followers.containsKey(client.getNameClient())){
                        //                 this.followers.put(client.getNameClient(), this.followers.get(client.getNameClient())+1);
                        //             }else{
                        //                 this.followers.put(client.getNameClient(), 1);
                        //             }
                        //         }
                        //     }
                        // }
                        // else {
                            // On envoie le message à tous les clients
                            for (Client client : clients) {
                                Socket keySocket = client.getSocket();
                                if (keySocket != this.client.getSocket() && client.getSalon().equals(this.client.getSalon())) {
                                    DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                    out2.writeUTF(msg_a_envoyer);
                                }
                            }
                        }
                } else if (message.startsWith("/")) {

                    switch (message) {
                        case BibliothequeString.COMMANDE_QUIT:
                            changerSalon(in, out);
                            break;

                        case BibliothequeString.COMMANDE_UPTIME:
                            // dit depuis combien de temps le salon est ouvert
                            String salon_du_client = this.client.getSalon();
                            for (Salon salon : salons) {
                                if (salon.getNomSalon().equals(salon_du_client)) {
                                    out.writeUTF("Le salon " + salon_du_client + " est ouvert depuis " + salon.tempsEntreCreationEtMaintenant());
                                }
                            }
                            break;

                        case BibliothequeString.COMMANDE_USER:
                            // donne le nombre de personne connectée sur le serveur
                            out.writeUTF("Il y a " + clients.size() + " personnes connectées sur le serveur");
                            break;

                        
                        // case "/followers":
                        //     // donne le nombre de follow de l'utilisateur
                        //     // out.writeUTF("Vous avez " + this.client.getFollowers().size()+ " followers");   
                        //     System.out.println(this.followers.containsKey(this.client.getNameClient()) + " " + this.client.getNameClient() + " " + this.followers); 
                        //     if (this.followers.containsKey(this.client.getNameClient())){                      
                        //         out.writeUTF("Vous avez " +  this.followers.get(this.client.getNameClient())+ " followers");
                        //     }
                        //     else{
                        //         out.writeUTF("Vous n'avez pas de followers");
                        //     }


                        //     break;

                        case BibliothequeString.COMMANDE_HELP:
                            out.writeUTF(BibliothequeString.COMMANDE_LIST);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_QUIT + BibliothequeStyle.ANSI_RESET + BibliothequeString.COMMANDE_QUIT_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_UPTIME + BibliothequeStyle.ANSI_RESET + BibliothequeString.COMMANDE_UPTIME_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_USER + BibliothequeStyle.ANSI_RESET + BibliothequeString.COMMANDE_USER_INFO);
                            out.writeUTF(BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_MP + BibliothequeStyle.ANSI_RESET + BibliothequeString.COMMANDE_MP_INFO);

                            break;   

                        default:
                            out.writeUTF(BibliothequeString.COMMANDE_INCONNU +BibliothequeStyle.ANSI_RED + BibliothequeStyle.SOULIGNAGE + BibliothequeString.COMMANDE_HELP + BibliothequeStyle.ANSI_RESET);
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