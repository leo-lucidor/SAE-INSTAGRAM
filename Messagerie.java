import java.util.Scanner;

public class Messagerie {

    public static void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }  
    public static void main(String[] args) {
        Utilisateur leo = new Utilisateur("192.168.0.1", "leo");
        Utilisateur erwan = new Utilisateur("192.168.0.2", "erwan");
        // IP ou nom
        Scanner inputIP = new Scanner(System.in);
        System.out.print("Entrer votre IP ou nom : ");
        String ip = inputIP.nextLine();
        // nom d'utilisateur
        Scanner inputNom = new Scanner(System.in);
        System.out.print("Entrer votre nom d'utilisateur : ");
        String nom = inputNom.nextLine();
        // affichage
        clearScreen();
        System.out.println("votre ip : "+ip);
        System.out.println("votre nom d'utilisateur : "+nom);
    }
}