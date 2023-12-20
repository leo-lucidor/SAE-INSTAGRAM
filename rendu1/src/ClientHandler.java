import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class ClientHandler implements Runnable {
    private Client client;
    private List<Client> clients;
    private List<Salon> salons;

    public ClientHandler(Client client, List<Client> clients, List<Salon> salons) {
        this.client = client;
        this.clients = clients;
        this.salons = salons;
    }

    public void clearTerminalClient(DataOutputStream out) throws IOException {
        out.writeUTF(BibliothequeString.CLEAR);
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
                        break;
                    case "false":
                        String msg_a_envoyer = dtf.format(now) + " | " + nomClient
                                + " - " + message;
                        System.out.println(
                                " user : " + nomClient + " content : " + message + " date : " + dtf.format(now));

                        for (int i = 0; i < message.length(); i++) {
                            if (message.charAt(i) == ' ') {
                                contientEspace = true;
                            }
                        }

                        for (Client client : clients) {
                            Socket keySocket = client.getSocket();
                            if (keySocket != this.client.getSocket()
                                    && client.getSalon().equals(this.client.getSalon())) {
                                DataOutputStream out2 = new DataOutputStream(keySocket.getOutputStream());
                                out2.writeUTF(msg_a_envoyer);
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