
import play.*;
import play.jobs.*;
import play.test.*;

import models.*;

@OnApplicationStart
public class BootStrap extends Job {

    public void doJob() {
        Usuari u = new Usuari("admin", "1234", "admin_blog@gmail.com", "9/7/98");
        if(Usuari.count() == 0) {
            u.save();
        }

        if(Blog.count() == 0){
            new Blog(u,"“No os diré no lloréis, pues no todas las lágrimas son amargas” – Gandalf el Blanco." +
                " Això és una prova. Això és una prova. Això és una prova. Això és una prova. " +
                "Això és una prova. Això és una prova. Això és una prova. Això és una prova. ",
                "The Lord of The Rings","Frase Gandalf").save();
        }
    }

}