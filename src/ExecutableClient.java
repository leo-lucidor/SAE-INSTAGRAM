import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class ExecutableClient {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Client client1 = new Client("");
        Boolean connecte = true;
        try {
            client1.startClient();
            DataInputStream in = new DataInputStream(client1.getSocket().getInputStream());
            DataOutputStream out = new DataOutputStream(client1.getSocket().getOutputStream());
            try {
                while (connecte) {
                    String message = sc.nextLine();
                    if (message.length() > 0) {
                        out.writeUTF(message);
                    }
                }
            } catch (Exception e) {
                System.out.println("\u001b[31;1mDéconnexion forcé du serveur, fermeture du client" + BibliothequeStyle.ANSI_RESET);
            }
        } catch (Exception e) {
            e.getMessage();
            System.out.println("\u001b[31;1mErreur lors de la connexion au serveur" + BibliothequeStyle.ANSI_RESET);
            connecte = false;
        }
    }
}