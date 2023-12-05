package ancien;
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
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.ip);
        hash = 97 * hash + Objects.hashCode(this.nomUtilisateur);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Utilisateur)) {
            return false;
        }
        Utilisateur other = (Utilisateur) obj;
        return this.ip.equals(other.ip) && this.nomUtilisateur.equals(other.nomUtilisateur);
    }

    @Override
    public String toString() {
        return "Utilisateur{" + "ip=" + ip + ", nomUtilisateur=" + nomUtilisateur + '}';
    }
}
