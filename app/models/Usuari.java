package models;

import play.db.jpa.Model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Usuari extends Model {

    public String nom;
    public String contra;
    public String mail;
    public String naixament;

    public Usuari(String nom, String contra, String mail, String naixament) {
        this.nom = nom;
        this.contra = contra;
        this.naixament = naixament;
        this.mail = mail;
    }

    public Usuari () {
    }
    
    @OneToMany(mappedBy = "autor")
    public List<Comentari> comentaris = new ArrayList<Comentari>();

    @OneToMany(mappedBy = "autor")
    public List<Blog> blogs = new ArrayList<Blog>();

}