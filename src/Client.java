import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Client {
    private Socket socket;
    private String nameClient;
    private String salon;
    private String motsDePasse;
    private boolean estConnecte;

    public Client(String nameClient) {
        this.socket = null;
        this.nameClient = nameClient;
        this.salon = "";
        this.estConnecte = false;
    }

    public void setEstConnecte(boolean bool){
        this.estConnecte = bool;
    }

    public boolean getEstConnecte(){
        return this.estConnecte;
    }

    public void setMotsDePasse(String motsDePasse) {
        this.motsDePasse = motsDePasse;
    }

    public String getMotsDePasse() {
        return motsDePasse;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getNameClient() {
        return nameClient;
    }

    public void setNameClient(String nameClient) {
        this.nameClient = nameClient;
    }

    public String getSalon() {
        return salon;
    }

    public void setSalon(String salon) {
        this.salon = salon;
    }

    public void closeClient() {
        try {
            socket.close();
            System.out.println("Client disconnected");
        } catch (Exception e) {
            System.out.println("Error closing client");
        }
    }

    public static void clearTerminal() {
        String os = System.getProperty("os.name");
        if (os.contains("Windows")) {
            try {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } catch (Exception e) {
                System.out.println("Erreur lors du nettoyage du terminal.");
            }
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
        }
    }

    public void setConnexion() {
        clearTerminal();
        // On se connecte au serveur
        System.out.println("\u001b[4mAdresse IP du serveur :\u001b[0m");
        Scanner scanner = new Scanner(System.in);
        // String ipServeur = scanner.nextLine();
        String ipServeur = "localhost";


        System.out.println("\u001b[4mPort du serveur :\u001b[0m");
        int portServeur = scanner.nextInt();
        scanner.nextLine();

        try {
            socket = new Socket(ipServeur, portServeur);
            System.out.println("Client connected");
            setSocket(socket);
        } catch (Exception e) {
            if (e instanceof UnknownHostException) {
                System.out.println("Je ne trouve de serveur avec cette adresse IP.");
            } else if (e instanceof IOException) {
                System.out.println("Erreur de connexion.");
            }
        }
    }

    public void startClient() throws IOException {
        clearTerminal();
        // On se connecte au serveur
        setConnexion();
        demanderNom();
        miseEnEcoute(new DataInputStream(this.getSocket().getInputStream()));
    }

    public void miseEnEcoute(DataInputStream in) {
        // On lance un thread qui va écouter les messages du serveur
        Thread t = new Thread(new Affichage(in));
        t.start();
    }

    private int obtenirDernierIdUtilisateur(JSONArray utilisateursExistants) throws JSONException {
        int dernierId = -1;

        for (int i = 0; i < utilisateursExistants.length(); i++) {
            JSONObject utilisateur = utilisateursExistants.getJSONObject(i);
            int id = utilisateur.getInt("idUtilisateur");
            if (id > dernierId) {
                dernierId = id;
            }
        }

        return dernierId;
    }

    public int obtenirNouvelIdUtilisateur(JSONArray utilisateursExistants) throws JSONException {
        int dernierId = obtenirDernierIdUtilisateur(utilisateursExistants);

        // Si aucun utilisateur existant, retourner 1 comme premier ID, sinon retourner
        // le prochain ID
        return (dernierId == -1) ? 1 : dernierId + 1;
    }

    public boolean verifPseudoInJson(String pseudo) throws JSONException {
        String FICHIER_JSON = "connexion.json";
        JSONArray utilisateursExistants = new JSONArray();
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
            utilisateursExistants = new JSONArray(contenuFichier);
        } catch (IOException e) {
            // Le fichier n'existe probablement pas encore, c'est acceptable.
        }
        for (int i = 0; i < utilisateursExistants.length(); i++) {
            JSONObject utilisateur = utilisateursExistants.getJSONObject(i);
            String pseudoJson = utilisateur.getString("pseudo");
            if (pseudoJson.equals(pseudo)) {
                return true;
            }
        }
        return false;
    }

    public boolean verifMDPInJson(String pseudo, String mdp) throws JSONException {
        String FICHIER_JSON = "connexion.json";
        JSONArray utilisateursExistants = new JSONArray();
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
            utilisateursExistants = new JSONArray(contenuFichier);
        } catch (IOException e) {
            // Le fichier n'existe probablement pas encore, c'est acceptable.
        }
        for (int i = 0; i < utilisateursExistants.length(); i++) {
            JSONObject utilisateur = utilisateursExistants.getJSONObject(i);
            String pseudoJson = utilisateur.getString("pseudo");
            String mdpJson = utilisateur.getString("motsDePasse");
            if (pseudoJson.equals(pseudo) && mdpJson.equals(mdp)) {
                return true;
            }
        }
        return false;
    }

    public void ajouterUtilisateur() {
        String FICHIER_JSON = "connexion.json";

        try {
            demanderMotsdePasses();
        } catch (Exception e) {
            System.out.println("Erreur lors de la saisie du mots de passe.");
        }
        try {
            // Charger les utilisateurs existants depuis le fichier JSON
            JSONArray utilisateursExistants = new JSONArray();
            try {
                String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_JSON)));
                utilisateursExistants = new JSONArray(contenuFichier);
            } catch (IOException e) {
                // Le fichier n'existe probablement pas encore, c'est acceptable.
            }
            String pseudo = this.nameClient;
            String mDP = this.motsDePasse;
            // L'ID n'existe pas encore, créer une nouvelle entrée pour l'utilisateur
            JSONObject utilisateur = new JSONObject();
            utilisateur.put("idUtilisateur", obtenirNouvelIdUtilisateur(utilisateursExistants));
            utilisateur.put("pseudo", pseudo);
            utilisateur.put("motsDePasse", mDP);
            utilisateursExistants.put(utilisateur);

            // Enregistrer la liste mise à jour dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter(FICHIER_JSON)) {
                fichierJson.write(utilisateursExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void demanderNom() throws IOException {

        clearTerminal();
        Scanner scanner = new Scanner(System.in);

        // On récupère le nom du client et on vérifie s'il est déjà utilisé
        boolean isNameUsed = false;
        boolean isNameSet = false;
        String nomClient = "";
        DataInputStream in = new DataInputStream(this.getSocket().getInputStream());
        DataOutputStream out = new DataOutputStream(this.getSocket().getOutputStream());

        while (!isNameSet) {
            if (!isNameUsed) {
                // On demande le nom du client
                System.out.println("\u001b[4mNom du client :\u001b[0m");
                nomClient = scanner.nextLine();
            }

            // On envoie le nom au serveur
            if (nomClient.length() > 0) {
                try {
                    isNameUsed = verifPseudoInJson(nomClient);
                } catch (Exception e) {
                    System.out.println("Erreur lors de la vérification du nom.");
                }

                if (isNameUsed) {
                    // Si le nom est déjà utilisé
                    clearTerminal();
                    System.out.println("Veux tu te connecter (O/N)");
                    String reponse = scanner.nextLine();
                    switch (reponse) {
                        case "O":
                            boolean isMDPCorrect = false;
                            System.out.println("Merci de saisir votre mot de passe :");
                            String mdp = scanner.nextLine();
                            try {
                                isMDPCorrect = verifMDPInJson(nomClient, mdp);
                            } catch (Exception e) {
                                System.out.println("Erreur lors de la vérification du mot de passe.");
                            }
                            if (isMDPCorrect) {
                                // On enregistre le nom du client
                                isNameSet = true;
                                out.writeUTF(nomClient);
                                this.setEstConnecte(true);
                                this.setNameClient(nomClient);
                                // clearTerminal();
                            } else {
                                // clearTerminal();
                                System.out.println("\u001b[31;1mMot de passe incorrect.\u001b[0m");
                            }
                            break;
                        case "N":
                            // On recommence au début ou on demande le nom du client
                            isNameUsed = false; // Réinitialiser la vérification du nom
                            break;
                        default:
                            break;
                    }
                } else {
                    // Le nom n'est pas utilisé, on enregistre le nom du client
                    isNameSet = true;
                    this.setNameClient(nomClient);
                    setEstConnecte(true);
                    System.out.println(getEstConnecte());

                    // Ajouter la logique pour ajouter l'utilisateur dans le fichier JSON
                    ajouterUtilisateur();

                    System.out.println("\u001b[34;1mNom du client enregistré.\u001b[0m");
                }
            }
        }
    }

    public void demanderMotsdePasses() throws IOException {
        clearTerminal();
        Scanner scanner = new Scanner(System.in);

        // On récupère le nom du client et on vérifie si il est déjà utilisé
        Boolean isMotsDePasseSet = false;
        String mostDePasse = "";
        DataInputStream in = new DataInputStream(this.getSocket().getInputStream());
        DataOutputStream out = new DataOutputStream(this.getSocket().getOutputStream());
        while (!isMotsDePasseSet) {
            // On demande le nom du client
            System.out.println("\u001b[4mMots de passe :\u001b[0m");
            mostDePasse = scanner.nextLine();

            if (mostDePasse.length() > 0) {
                if (mostDePasse.contains(" ")) {
                    clearTerminal();
                    System.out.println("\u001b[31;1mLe mots de passe ne doit pas contenir d'espace.\u001b[0m");
                } else {
                    isMotsDePasseSet = true;
                }
            }
        }
        setMotsDePasse(mostDePasse);
    }


    @Override
    public String toString() {
        return "ClientS [nameClient=" + nameClient + ", salon=" + salon + ", socket=" + socket + "]";
    }
}