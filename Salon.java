import java.util.List;

public class Salon {
    private String nomSalon;
    private String ip;
    private List<Utilisateur> utilisateurs;

    public Salon(String nomSalon, String ip, List<Utilisateur> utilisateurs) {
        this.nomSalon = nomSalon;
        this.ip = ip;
        this.utilisateurs = utilisateurs;
    }

    public String getNomSalon() {
        return nomSalon;
    }

    public String getIp() {
        return ip;
    }

    public List<Utilisateur> getUtilisateurs() {
        return utilisateurs;
    }

    public void setNomSalon(String nomSalon) {
        this.nomSalon = nomSalon;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setUtilisateurs(List<Utilisateur> utilisateurs) {
        this.utilisateurs = utilisateurs;
    }

    @Override
    public String toString() {
        return "Salon{" + "nomSalon=" + nomSalon + ", ip=" + ip + ", utilisateurs=" + utilisateurs + '}';
    }
}
