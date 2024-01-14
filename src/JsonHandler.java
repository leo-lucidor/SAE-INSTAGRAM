import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class JsonHandler {
    private static final String FICHIER_CONNEXION = "connexion.json";
    private static final String FICHIER_MESSAGES = "message.json";
    private static final int LIMITE_MESSAGES = 50;

    public static int getIdUtilisateurWithNomInJson(String nom) {
        int id = 0;
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_CONNEXION)));
            JSONObject jsonObject = new JSONObject(contenuFichier);
            JSONArray connexionArray = jsonObject.getJSONArray("connexion");

            for (int i = 0; i < connexionArray.length(); i++) {
                JSONObject obj = connexionArray.getJSONObject(i);
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

    public static boolean verifierIdUtilisateurExiste(int idUtilisateur) {
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_MESSAGES)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject messageUtilisateur = messagesExistants.getJSONObject(i);
                int id = messageUtilisateur.getInt("idUtilisateur");

                if (id == idUtilisateur) {
                    return true;
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void supprimerMessagePlusAncien() throws JSONException{
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_MESSAGES)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // (Votre implémentation actuelle pour supprimer le message le plus ancien)

            // Enregistrez les modifications dans le fichier JSON
            try {
                Files.write(Paths.get(FICHIER_MESSAGES), messagesExistants.toString(4).getBytes(),
                        StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int trouverIdUtilisateurParPseudo(String pseudo) {
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_CONNEXION)));
            JSONArray utilisateursExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < utilisateursExistants.length(); i++) {
                JSONObject utilisateur = utilisateursExistants.getJSONObject(i);

                if (utilisateur.getString("pseudo").equals(pseudo)) {
                    return utilisateur.getInt("idUtilisateur");
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int obtenirNouvelIdMessage() {
        try {
            int maxId = 0;
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_MESSAGES)));
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                JSONArray dataObj = obj.getJSONArray("data");

                for (int j = 0; j < dataObj.length(); j++) {
                    JSONObject message = dataObj.getJSONObject(j);
                    int idMessage = message.getInt("idMessage");

                    if (idMessage > maxId) {
                        maxId = idMessage;
                    }
                }
            }
            return maxId + 1;
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
            return -1;
        }
    }

    public static int compterMessages() {
        try {
            String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_MESSAGES)));
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

    public static void ajouterMessage(int idMessage, String contenu, LocalDateTime date, int idUtilisateur,
            List<Integer> likes) {
        try {
            int cpt = compterMessages();
            if (cpt >= LIMITE_MESSAGES) {
                supprimerMessagePlusAncien();
            }
            DateTimeFormatter dtfD = DateTimeFormatter.ofPattern("YYYY/MM/dd");
            DateTimeFormatter dtfH = DateTimeFormatter.ofPattern("HH:mm:ss");

            JSONObject message = new JSONObject();
            message.put("idMessage", idMessage);
            message.put("contenu", contenu);
            message.put("date", dtfD.format(date));
            message.put("heure", dtfH.format(date));

            if (likes == null) {
                likes = new ArrayList<>();
            }

            message.put("like", new JSONArray(likes));

            JSONArray messagesExistants = new JSONArray();
            try {
                String contenuFichier = new String(Files.readAllBytes(Paths.get(FICHIER_MESSAGES)));
                messagesExistants = new JSONArray(contenuFichier);
            } catch (IOException e) {
                // Le fichier n'existe probablement pas encore, c'est acceptable.
            }

            JSONObject messageUtilisateur = new JSONObject();
            messageUtilisateur.put("idUtilisateur", idUtilisateur);
            messageUtilisateur.put("followers", new JSONArray());
            messageUtilisateur.put("data", new JSONArray().put(message));

            boolean utilisateurExiste = false;

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                int id = obj.getInt("idUtilisateur");

                if (id == idUtilisateur) {
                    obj.getJSONArray("data").put(message);
                    utilisateurExiste = true;
                    break;
                }
            }

            if (!utilisateurExiste) {
                messagesExistants.put(messageUtilisateur);
            }

            // Enregistrez les modifications dans le fichier JSON
            try {
                Files.write(Paths.get(FICHIER_MESSAGES), messagesExistants.toString(4).getBytes(),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementeLikeInJson(int idMessage, String nomLiker) {
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                JSONArray dataObj = obj.getJSONArray("data");
                for (int j = 0; j < dataObj.length(); j++) {
                    JSONObject message = dataObj.getJSONObject(j);
                    if (message.getInt("idMessage") == idMessage) {
                        JSONArray like = message.getJSONArray("like");
                        like.put(trouverIdUtilisateurParPseudo(nomLiker));
                    }
                }
            }

            // Enregistrez les modifications dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter("message.json")) {
                fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage());
        }
    }

    public int nbLikeMessageInJson(int idMessage){
        int nbLike = 0;
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                JSONArray dataObj = obj.getJSONArray("data");
                for (int j = 0; j < dataObj.length(); j++) {
                    JSONObject message = dataObj.getJSONObject(j);
                    if (message.getInt("idMessage") == idMessage) {
                        JSONArray like = message.getJSONArray("like");
                        nbLike = like.length();
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage());
        }
        return nbLike;
    }

    public String addFollowerInJson(String nomFollower, String nomFollowed) {
        
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);

                // On parcourt les "followers" de l'utilisateur followed
                if (obj.getInt("idUtilisateur") == trouverIdUtilisateurParPseudo(nomFollowed)) {
                    JSONArray followersObj = obj.getJSONArray("followers");

                // Vérifier si le follower existe déjà
                boolean followerExists = false;

                for (int j = 0; j < followersObj.length(); j++) {
                    if (followersObj.getString(j).equals(nomFollower)) {
                        followerExists = true;
                        break;
                    }
                }

                // Si le follower n'existe pas, l'ajouter à la liste
                if (!followerExists) {
                    followersObj.put(nomFollower);

                    // Enregistrez les modifications dans le fichier JSON
                    try (FileWriter fichierJson = new FileWriter("message.json")) {
                        fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return "Vous suivez désormais " + nomFollowed;
                }
                return "Vous suivez déjà " + nomFollowed;

                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage());
            return "Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage();
        }
        return "Erreur lors de la lecture ou de la modification du fichier JSON : " + "L'utilisateur " + nomFollowed + " n'existe pas";
    }

    public String retrouverNomClientByIdInJson(int idUtilisateur){
        String nomClient = "";
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONObject jsonObject = new JSONObject(contenuFichier);

            for (int i = 0; i < jsonObject.length(); i++) {
                if (jsonObject.getInt("idUtilisateur") == idUtilisateur){
                    nomClient = jsonObject.getString("pseudo");
                }
            }
            
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return nomClient;
    }

    public String removeFollowerInJson(String nomFollower, String nomFollowed) {
        String res = "";
        
        try {
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);
            // Parcours des éléments du tableau "connexion"
            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);

                // On parcourt les "followers" de l'utilisateur followed
                if (obj.getInt("idUtilisateur") == trouverIdUtilisateurParPseudo(nomFollowed)) {
                    JSONArray followersObj = obj.getJSONArray("followers");

                // Vérifier si le follower existe déjà
                boolean followerExists = false;

                for (int j = 0; j < followersObj.length(); j++) {
                    if (followersObj.getString(j).equals(nomFollower)) {
                        followersObj.remove(j);   
                        followerExists = true;
                        }
                    
                    if (!followerExists) {
                        res = "Vous ne suivez plus " + nomFollowed;
                    }else{
                        res = "Vous ne suiviez pas " + nomFollowed;
                        }
                    
                    
                

                    // Enregistrez les modifications dans le fichier JSON
                    try (FileWriter fichierJson = new FileWriter("message.json")) {
                        fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage());
            return "Erreur lors de la lecture ou de la modification du fichier JSON : " + e.getMessage();

        }
        return res;
    }

    public String supprimerMessageWithIdmessageInJson(int idMessage, String nomClient){
        try {

            int idnomClient = trouverIdUtilisateurParPseudo(nomClient);
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            boolean messageExiste = false;

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                if (obj.getInt("idUtilisateur") == idnomClient){
                    JSONArray dataObj = obj.getJSONArray("data");
                    for (int j = 0; j < dataObj.length(); j++){
                        JSONObject message = dataObj.getJSONObject(j);
                        if (message.getInt("idMessage") == idMessage && messageExiste == false){
                            dataObj.remove(j);
                            messageExiste = true;
                        }
                    }
                }
            }

            // Enregistrez les modifications dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter("message.json")) {
                fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (messageExiste){
                return "Le message a bien été supprimé";
            } else {
                return "Le message ne vous appartient pas ou n'existe pas";
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
            return "Erreur lors de la lecture du fichier JSON : " + e.getMessage();
        }
    }

    // public boolean veridMotsDePasseInJson(String motsDePasse) {
    //     boolean verid = false;
    //     try {
    //         // Lecture du contenu du fichier JSON dans une chaîne
    //         String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));

    //         // Création d'un objet JSON à partir de la chaîne lue
    //         JSONObject jsonObject = new JSONObject(contenuFichier);

    //         // Récupération du tableau "connexion"
    //         JSONArray connexionArray = jsonObject.getJSONArray("connexion");

    //         // Parcours des éléments du tableau "connexion"
    //         for (int i = 0; i < connexionArray.length(); i++) {
    //             JSONObject obj = connexionArray.getJSONObject(i);
    //             // on parcours le data de chaque objet
    //             JSONObject dataObj = obj.getJSONObject("data");
    //             if (dataObj.getString("nom").equals(this.client.getNameClient())) {
    //                 if (dataObj.getString("motsDePasse").equals(motsDePasse)) {
    //                     verid = true;
    //                 }
    //             }
    //         }
    //     } catch (Exception e) {
    //         System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
    //     }
    //     return verid;
    // }


    public void removeAllMessageInJson(String nomClient){
        try {
            int idnomClient = trouverIdUtilisateurParPseudo(nomClient);
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                if (obj.getInt("idUtilisateur") == idnomClient){
                    JSONArray dataObj = obj.getJSONArray("data");
                    for (int j = 0; j < dataObj.length(); j++){
                        dataObj.remove(j);
                    }
                }
            }

            // Enregistrez les modifications dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter("message.json")) {
                fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }


        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
    }

    public void removeUtilisateurInJsonConnexion(String nomClient){
        try {
            int idnomClient = trouverIdUtilisateurParPseudo(nomClient);
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("connexion.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                if (obj.getInt("idUtilisateur") == idnomClient){
                    messagesExistants.remove(i);
                }
            }

            // Enregistrez les modifications dans le fichier JSON
            try (FileWriter fichierJson = new FileWriter("connexion.json")) {
                fichierJson.write(messagesExistants.toString(4)); // Indentation de 4 espaces pour la lisibilité
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
    }

    public String removeAllCompleted(String nomClient){
        removeAllMessageInJson(nomClient);
        removeUtilisateurInJsonConnexion(nomClient);
        return "Vous avez supprimé le compte de " + nomClient + " et tous ses messages";
    }

    public List<String> getfollowersInJson(String nomClient){
        List<String> followers = new ArrayList<String>();
        try {
            int idnomClient = trouverIdUtilisateurParPseudo(nomClient);
            // Lecture du contenu du fichier JSON dans une chaîne
            String contenuFichier = new String(Files.readAllBytes(Paths.get("message.json")));

            // Création d'un objet JSON à partir de la chaîne lue
            JSONArray messagesExistants = new JSONArray(contenuFichier);

            for (int i = 0; i < messagesExistants.length(); i++) {
                JSONObject obj = messagesExistants.getJSONObject(i);
                // on parcours le data de chaque objet
                if (obj.getInt("idUtilisateur") == idnomClient){
                    JSONArray followersObj = obj.getJSONArray("followers");
                    for (int j = 0; j < followersObj.length(); j++){
                        followers.add(followersObj.getString(j));
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la lecture du fichier JSON : " + e.getMessage());
        }
        return followers;
    }


    // ... (ajoutez d'autres méthodes selon vos besoins)
}
