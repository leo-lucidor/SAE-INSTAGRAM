import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.time.format.DateTimeFormatter; 
import java.time.LocalDateTime; 

public class ClientHandler implements Runnable{
    private Client client;
    private List<Client> clients;
    private List<Salon> salons;
    public ClientHandler(Client client, List<Client> clients, List<Salon> salons){
        this.client = client;
        this.clients = clients;
        this.salons = salons; 
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException{
        out.writeUTF("clear");
    }

    public void afficherSalons(DataOutputStream out) throws IOException{
        clearTerminalClient(out);

        // On affiche les salons
        if (salons.size() > 0) {
            out.writeUTF("\u001b[34;1m\u001b[4mSalons disponibles :\u001b[0m");
            for (Salon salon : salons) {
                out.writeUTF("\u001b[32m-"+salon+"\u001b[0m");
            }
        } else {
            out.writeUTF("\u001b[34;1mIl n'y a pas de salon, créez-en un !\u001b[0m");
        }
        out.writeUTF("\u001b[34;1m(si vous faites /quit ici, vous serez déconnecté)\u001b[0m");
    }


    public String verifName(DataInputStream in, DataOutputStream out) throws IOException{
        // On récupère le nom du client et on vérifie si il est déjà utilisé
        Boolean isNameSet = false;
        String nomClient = "";
        while (!isNameSet) {
            nomClient = in.readUTF();
            if (nomClient.length() > 0) {
                Boolean isNameUsed = false;
                for (Client client : clients) {
                    String name = client.getNameClient();
                    if (name.equals(nomClient)) {
                        isNameUsed = true;
                        out.writeUTF("true");
                    }
                }
                if (!isNameUsed) {
                    for (Client client : clients) {
                        if (client.getSocket() == this.client.getSocket()) {
                            client.setNameClient(nomClient);
                        }
                    }
                    isNameSet = true;
                    out.writeUTF("false");
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
        String nomSalon = "";
        afficherSalons(out);
        // tant que le salon n'est pas défini
        while (!isSalonSet) {
            // on récupère le nom du salon
            nomSalon = in.readUTF();

            // si le nom du salon n'est pas vide
            if (nomSalon.length() > 0) {

                // si le client envoie /quit on le déconnecte
                if (nomSalon.equals("/quit")) {
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
                            out.writeUTF("tp");
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
                        out.writeUTF("new");
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
                    out.writeUTF("new");
                }
            }
        }
        out.writeUTF("\u001b[34;1mVous êtes dans le salon "+nomSalon+"\u001b[0m");
    }
    
    public void run(){
        try {
            DataInputStream in = new DataInputStream(client.getSocket().getInputStream());
            DataOutputStream out = new DataOutputStream(client.getSocket().getOutputStream());
            
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

                    boolean contientEspace = false;
                    for (int i=0; i<message.length(); i++) {
                        if (message.charAt(i) == ' ') {
                            contientEspace = true;
                        }
                    }
                        // On envoie le message à tous les clients
                        for (Client client : clients) {
                            Socket keySocket = client.getSocket();
                            if (keySocket != this.client.getSocket() && client.getSalon().equals(this.client.getSalon())) {
                                DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                out2.writeUTF(msg_a_envoyer);
                            }
                        }
                    }
                else{
                    // mettre les commande
                }
            }
        } catch (IOException e) {
            System.out.println("Un client vient de se déconnecter.");
            for (Client client : clients) {
                Socket keySocket = client.getSocket();
                if (keySocket == this.client.getSocket()) {
                    clients.remove(client);
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("Un client vient de se déconnecter.");
        }
    }
}