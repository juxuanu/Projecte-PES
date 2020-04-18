package models;

import play.db.jpa.Model;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Comentari extends Model {

    public int valoracio;

    public String contingut;
   
    public Comentari (String contingut, Blog b, Usuari autor) {
        this.blog = b;
        this.autor = autor;
        this.contingut = contingut;
        this.valoracio = 0;
    }

    @ManyToOne
    public Blog blog;

    @ManyToOne
    public Usuari autor;
}
