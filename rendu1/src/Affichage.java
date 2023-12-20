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
                        String private_message = "\u001b[1m\u001b[33m" + message.substring(message.indexOf("P")+1) + "\u001b[0m";
                        System.out.println(private_message);
                    }else{
                        System.out.println(message);
                    }
                }else{
                    if (message.equals("insalon")) {
                        System.out.println("\u001b[34;1mVous êtes déjà dans ce salon\u001b[0m");
                    } else if (message.equals("tp")) {
                        System.out.println("\u001b[34;1mVous avez été téléporté dans le salon\u001b[0m");
                    } else if (message.equals("new")) {
                        System.out.println("\u001b[34;1mLe salon a été créé\u001b[0m");
                    } else if (message.equals("clear")) {
                        Client.clearTerminal();
                    }
                }
            } catch (Exception e) {
                if (e instanceof EOFException || e instanceof SocketException) {
                    System.out.println("\u001b[31;1mLe serveur a fermé la connexion ou vous avez été déconnecté\u001b[0m");
                    System.exit(0);
                } else if (e instanceof IOException) {
                    System.out.println("\u001b[31;1mErreur d'entrée/sortie\u001b[0m");
                    System.exit(0);
                }
            }
        }
    }
}
