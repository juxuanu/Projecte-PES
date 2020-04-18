package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Lob;
import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Blog extends Model {

    @Lob
    @Column(columnDefinition="CLOB")
    public String contingut;

    public String titol;
    public String tema;
    public int valoracio;

    public Blog(Usuari autor, String contingut, String tema, String titol) {
        this.autor = autor;
        this.contingut = contingut;
        this.tema = tema; 
        this.valoracio = 0;
        this.titol = titol;
    }

    public Blog () {
        this.contingut = "";
        this.tema = "";
        this.valoracio = 0;
        this.titol = "";
    }

    @OneToMany(mappedBy = "blog")
    public List<Comentari> comentaris = new ArrayList<Comentari>();

    @ManyToOne
    public Usuari autor;

}
