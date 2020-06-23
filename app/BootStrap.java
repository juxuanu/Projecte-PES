
import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class BootStrap extends Job {
    // A l'iniciar el servidor, cal crear alguns usuaris i com a mínim un blog.
    // Cal crear un usuari pel contingut eliminat.
    // Creem un usuari "admin" però no té cap permís especial.
    public void doJob() {
        Usuari u = new Usuari("admin", "1234", "admin_blog@gmail.com", "9/7/98");
        Usuari u2 = new Usuari("<usuari_eliminat>","aBmFtZDV75bggDJ","-","-");
        if(Usuari.find("byNom","admin").first() == null)
          u.save();

        if(Usuari.find("byNom","<usuari_eliminat>").first() == null)
          u2.save();

        if(Blog.count() == 0){
            new Blog(u,"“No os diré no lloréis, pues no todas las lágrimas son amargas” – Gandalf el Blanco." +
                " Això és una prova. Això és una prova. Això és una prova. Això és una prova. " +
                "Això és una prova. Això és una prova. Això és una prova. Això és una prova. ",
                "The Lord of The Rings","Frase Gandalf").save();
        }
    }

}
