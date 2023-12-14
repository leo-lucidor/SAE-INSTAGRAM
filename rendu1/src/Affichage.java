import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Affichage implements Runnable {
    private DataInputStream in;

    public Affichage(DataInputStream in){
        this.in = in;

    }
    public void run(){
        List<String> motPasAfficher = new ArrayList<>(Arrays.asList("false", "true","insalon","tp","new","clear"));

        
        while(true){
            try {
                // On lit le message envoyé par le serveur
                String message = in.readUTF();
                if (!motPasAfficher.contains(message)) {
                    if (message.startsWith("MP") &&  message.contains("vous chuchotte : ")) {
                        String private_message = "\u001b[1m\u001b[33m" + message.substring(message.indexOf("P")+1) + "" + BibliothequeStyle.ANSI_RESET;
                        System.out.println(private_message);
                    }else{
                        System.out.println(message);
                    }
                }else{

                    switch (message) {
                        case "insalon":
                            System.out.println(BibliothequeStyle.ANSI_PURPLE + "Vous êtes déjà dans ce salon" + BibliothequeStyle.ANSI_RESET);
                            break;
                        case "tp":
                            System.out.println(BibliothequeStyle.ANSI_PURPLE + "Vous avez été téléporté dans le salon" + BibliothequeStyle.ANSI_RESET);
                            break;
                        case "new":
                            System.out.println(BibliothequeStyle.ANSI_PURPLE + "Le salon a été créé" + BibliothequeStyle.ANSI_RESET);
                            break;
                        case "clear":
                            Client.clearTerminal();
                            break;
                        default:
                            break;
                        }
                    }
            } catch (Exception e) {
                if (e instanceof EOFException || e instanceof SocketException) {
                    System.out.println(BibliothequeStyle.ANSI_RED + "Le serveur a fermé la connexion ou vous avez été déconnecté" + BibliothequeStyle.ANSI_RESET);
                    System.exit(0);
                } else if (e instanceof IOException) {
                    System.out.println(BibliothequeStyle.ANSI_RED + "Erreur d'entrée/sortie" + BibliothequeStyle.ANSI_RESET);
                    System.exit(0);
                }
            }
        }
    }
}
