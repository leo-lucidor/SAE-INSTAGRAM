import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Messagerie {

    private List<Salon> salons;
    private List<Utilisateur> utilisateurs;

    public Messagerie() {
        this.salons = new ArrayList<Salon>();
        this.utilisateurs = new ArrayList<Utilisateur>();
        // ajout des utlisateurs et salons
        Utilisateur leo = new Utilisateur("192.168.0.1", "leo");
        Utilisateur erwan = new Utilisateur("192.168.0.2", "erwan");
        this.utilisateurs.add(leo);
        this.utilisateurs.add(erwan);
        Salon salon = new Salon("salon", "192.168.1.1", this.utilisateurs);
        this.salons.add(salon);
        System.out.println(this.salons);
    }

    public static void clearScreen() {  
        System.out.print("\033[H\033[2J");  
        System.out.flush();  
    }  
    public static void main(String[] args) {
        Messagerie messagerie = new Messagerie();
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