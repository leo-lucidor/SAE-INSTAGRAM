public class Utilisateur {
    private String ip;
    private String nomUtilisateur;

    public Utilisateur(String ip, String nomUtilisateur) {
        this.ip = ip;
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getIp() {
        return ip;
    }

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    @Override
    public String toString() {
        return "Utilisateur{" + "ip=" + ip + ", nomUtilisateur=" + nomUtilisateur + '}';
    }
}
