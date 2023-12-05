import java.sql.Date;

public class Salon {
    private String nomSalon;
    private Date dateCreation;

    public Salon(String nomSalon) {
        this.nomSalon = nomSalon;
        this.dateCreation = new Date(System.currentTimeMillis());
    }

    public String getNomSalon() {
        return nomSalon;
    }

    public void setNomSalon(String nomSalon) {
        this.nomSalon = nomSalon;
    }

    public Date getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String tempsEntreCreationEtMaintenant(){
        Date dateMaintenant = new Date(System.currentTimeMillis());
        long diff = dateMaintenant.getTime() - this.dateCreation.getTime();
        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);
        if (diffDays > 0){
            return diffDays + " jours" + " " + diffHours + " heures" + " " + diffMinutes + " minutes" + " " + diffSeconds + " secondes";
        } else if (diffHours > 0){
            return diffHours + " heures" + " " + diffMinutes + " minutes" + " " + diffSeconds + " secondes";
        } else if (diffMinutes > 0){
            return diffMinutes + " minutes" + " " + diffSeconds + " secondes";
        } else if (diffSeconds > 0){
            return diffSeconds + " secondes";
        }
        return "0 secondes";
    }
    
    @Override
    public String toString() {
        return this.nomSalon + " (créé il y a " + tempsEntreCreationEtMaintenant()+ ")";
    }
}
