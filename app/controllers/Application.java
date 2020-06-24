package controllers;

import play.*;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.*;

import java.util.*;

import models.*;

import javax.persistence.*;

public class Application extends Controller {
/*
En cada mètode on es fa un render, es mira primer si l'usuari ha fet login
i si ho ha fet, es passen uns renderArgs adequats. Equivalent a utilitzar el mòdul
de Security però fet a ma.
*/

	/*
	Mètode que es crida a / (GET /).
	Mostra la pàgina de login en cas que no estiguis loguejat.
	*/
    public static void index() {
      String n = session.get("user");
    	if(n!=null){
    		LlistaBlogsPerUsuari(n);
    	}
    	else
    		renderTemplate("Application/index.html");
    }

    public static void logout(){
    	if(session.get("user")!=null){
    		session.remove("user");
    	}
    	renderTemplate("Application/index.html");
    }

    public static String login_android(String usuari, String contrasenya){
      Usuari user = Usuari.find("byNomAndContra",usuari,contrasenya).first();
      if(user != null) {
        session.put("user","user.nom");
        return "LOGIN_OK";
      }
      return "LOGIN_ERROR";
    }

	public static String register_android(String usuari, String contrasenya, String mail, String naixament){
		Usuari user = Usuari.find("byNom",usuari).first();
		if(isNullOrEmpty(usuari) || isNullOrEmpty(contrasenya) || isNullOrEmpty(mail) || isNullOrEmpty(naixament))
			return "REGISTER_INVALID";
		if(user == null){
			Usuari nou = new Usuari(usuari,contrasenya,mail,naixament);
			nou.save();
			session.put("user","nou.nom");
			return "REGISTER_OK";
		}
		return "REGISTER_USEREXISTS";
	}

    public static void login(@Valid Usuari user){
      Usuari u = Usuari.find("byNomAndContra", user.nom, user.contra).first();
      if(u != null) {
      	Usuari u2 = new Usuari(); u2.nom = user.nom;
          session.put("user", u2.nom);
          LlistaBlogsPerUsuari(u2.nom);
      }
      else {
          renderText("Error de login");
      }
    }

	/*
	Mostra la pagina html amb una llista dels blogs d'un usuari
	*/
    public static void LlistaBlogsPerUsuari(String nom){
    	String n = session.get("user");
    	if(n!=null){
    		Usuari u = new Usuari(); u.nom = n;
    		renderArgs.put("user",u);
    	}
      validation.required(nom);
      if(validation.hasErrors()) {
        renderText("Cap usuari a visualitzar.");
      }
      if(Objects.equals(nom,"<usuari_eliminat>"))
        renderText("Usuari no existeix");
      Usuari u = Usuari.find("byNom",nom).first();
      if (u!=null) {
        renderArgs.put("usuari_visualitzat",u);
        renderArgs.put("blogs",u.blogs);
        renderTemplate("Application/loggedIn.html");
      }
      else {
        renderText("Error amb el nom d'usuari");
      }
    }

    public static void Registrar(@Valid Usuari usuari) {
    	validation.required(usuari.nom);
    	validation.required(usuari.contra);
    	validation.required(usuari.mail);
    	validation.required(usuari.naixament);
      if (Usuari.find("byNom",usuari.nom).first() == null && !validation.hasErrors()) {
        usuari.create();
        session.put("user", usuari.nom);
    	  LlistaBlogsPerUsuari(usuari.nom);
      }
      else {
          renderText("L'usuari ja existeix o les dades no són vàlides.");
      }
    }

	/*
	Per no perdre el contingut generat per l'usuari a borrar,
	assignem l'autor a <usuari_eliminat> abans de borrar-lo.
	*/
    public static void BorrarUsuari(){
      String n = session.get("user");
      if(n!=null){
        Usuari u = Usuari.find("byNom",n).first();
        Usuari anon = Usuari.find("byNom","<usuari_eliminat>").first();
        if(u!=null){
          for(Blog b: u.blogs){
            b.autor = anon;
            b.save();
          }
          for(Comentari c: u.comentaris){
            c.autor = anon;
            c.save();
          }
          u.delete();
          session.remove("user");
          index();
        }
        else
          renderText("Usuari no trobat.");
      }
    }

	// Mostrar un blog
    public static void CarregarBlog(long id){
		  String n = session.get("user");
    	if(n!=null){
    		Usuari u = new Usuari(); u.nom = n;
        renderArgs.put("user",u);
    	}
    	Blog b = Blog.find("byId",id).first();
    	if(b != null){
    		renderArgs.put("blog",b);
    		renderTemplate("Application/blog.html");
    	}
    	else
    		renderText("Blog no trobat (id = " + id + ")");
    }

	/*
	Mostrar la pàgina per crear un blog nou,
	a més, també serveix per carregar un blog a modificar.
	*/
    public static void CarregarBlogNou(int blogId){
    	String n = session.get("user");
    	if(n!=null){
    		Usuari u = new Usuari(); u.nom = n;
    		renderArgs.put("user",u);
    	}
    	else {
    		renderText("Login primer!");
    	}
    	if(blogId>0){	//Això vol dir que estem editant un blog, no creant-lo
    		Blog b = Blog.find("byId",(long)blogId).first();
    		if(b==null) renderText("El blog a modificar no existeix!");
    		renderArgs.put("blog",b);
    	}
    	renderTemplate("Application/blognou.html");
    }

	// Mètode cridat quan apretes el botó de comentar en un blog.
    public static void BotoComentar(String contingut, int blogId){
    	String n = session.get("user");
    	if(n!=null){
        if(!(isNullOrEmpty(contingut) || Objects.equals(contingut,"Comentari nou..."))){
        	Usuari u = Usuari.find("byNom",n).first();
        	Blog b = Blog.find("byId",(long)blogId).first();
        	if(b!=null){
        		Comentari c = new Comentari(contingut, b, u);
        		c.save();
        		CarregarBlog((long)blogId);
        	}
        	else
        		renderText("El blog no existeix!");
        }
        else
        	renderText("Introdueix un comentari vàlid.");
    	}
    	else
    		renderText("Inicia sessió per comentar!");
    }

	// Mètode necessari per poguer afegir valoracions numèriques a un blog
    public static void ValorarBlog(int idBlog, int val){
    	//val: 1 -> +1 | altre -> -1
    	Blog b = Blog.find("byId",(long)idBlog).first();
    	if(b!=null){
    		if(val==1)
    			b.valoracio += 1;
    		else
    			b.valoracio -= 1;
    		b.save();
    		CarregarBlog((long)idBlog);
    	}
    	else
    		renderText("El blog no existeix!");
    }

	// Mètode necessari per poguer afegir valoracions numèriques a un comentari
    public static void ValorarComentari(int idComentari, int val){
      Comentari c = Comentari.find("byId",(long)idComentari).first();
      if(c!=null){
        if(val==1)
          c.valoracio += 1;
        else
          c.valoracio -= 1;
        c.save();
        CarregarBlog(c.blog.id);
      }
    }

	/*
	El "camp" ens indica si buscar per tema, titol o autor.
	*/
    public static void BuscarBlog(String camp, String searchString){
      if (Objects.equals(camp,"titol")){
        List<Blog> blogs = Blog.find("byTitol",searchString).fetch();
        renderArgs.put("blogs",blogs);
        String n = session.get("user");
        if(n!=null){
      		Usuari u = new Usuari(); u.nom = n;
      		renderArgs.put("user",u);
      	}
        renderTemplate("Application/buscarBlogs.html");
      }
      if (Objects.equals(camp,"autor")){
        List<Blog> blogs = Blog.find("byAutor.nom",searchString).fetch();
        renderArgs.put("blogs",blogs);
        String n = session.get("user");
        if(n!=null){
      		Usuari u = new Usuari(); u.nom = n;
      		renderArgs.put("user",u);
      	}
        renderTemplate("Application/buscarBlogs.html");
      }
      if (Objects.equals(camp,"tema")){
        List<Blog> blogs = Blog.find("byTema",searchString).fetch();
        renderArgs.put("blogs",blogs);
        String n = session.get("user");
        if(n!=null){
      		Usuari u = new Usuari(); u.nom = n;
      		renderArgs.put("user",u);
      	}
        renderTemplate("Application/buscarBlogs.html");
      }
      renderText("Camp '"+ camp +"' no reconegut. Ha de ser: " +
        "titol, autor o tema");
    }

	// Mètode per mostrar la àgina on es modifiquen les dades de l'usuari.
    public static void CarregarAdminUsuari(){
    	String n = session.get("user");
    	if(n!=null){
    		Usuari u = Usuari.find("byNom",n).first();
    		u.contra = ""; //Per seguretat, no enviar la contrasenya actual al client
    		renderArgs.put("user",u);
    		renderTemplate("Application/admiusuari.html");
    	}
    	else
    		renderText("Inicia sessió primer");
    }

	// Mètode cridat en apretar el botó d'OK en la pagina de modificar usuari.
    public static void ModificarUsuari(Usuari usuariModificat){
    	String n = session.get("user");
    	if(n!=null){
    		Usuari usuariActual = Usuari.find("byNom", n).first();
    		if(!isNullOrEmpty(usuariModificat.contra))
    			usuariActual.contra = usuariModificat.contra;
    		if(!isNullOrEmpty(usuariModificat.naixament))
    			usuariActual.naixament = usuariModificat.naixament;
    		if(!isNullOrEmpty(usuariModificat.mail))
    			usuariActual.mail = usuariModificat.mail;
    		usuariActual.save();
    		CarregarAdminUsuari();
    	}
    	else
    		renderText("Sessió no iniciada. Què vols modificar si no entres al teu usuari primer?");
    }

	// Com a Java no pots comprovar si un String és null, està buit i/o és només espais en blanc,
	// Aquest mètode et permet comprovar-ho.
    private static Boolean isNullOrEmpty(String s){
    	if(s == null || s.trim().length() == 0 || s.length() == 0 || s == ""){
    		return true;
    	}
    	else return false;
    }

    // Mètode necessari per mostrar la pàgina de búsqueda de blogs
    public static void buscarBlogs(){
      String n = session.get("user");
      if(n!=null){
        Usuari u = new Usuari(); u.nom = n;
        renderArgs.put("user",u);
      }
      render();
    }

    // Carregar la llista de blog per usuari de l'usuari que ha iniciat sessio actualment
    public static void CarregarLoggedIn(){
      LlistaBlogsPerUsuari(session.get("user"));
    }

	// Mètode cridat en apretar el botó de Publicar en la pàgina de crear un blog nou.
    public static void CrearBlogNou(String titol, String tema, String contingut, long idBlog){
    	//idBlog > 0 -> modificar, idBlog <=0 -> crear
    	String n = session.get("user");
    	if(n!=null){
    		if(idBlog<=0){ //Crear Blog nou
    			Blog nou;
    			if(!isNullOrEmpty(titol) && !isNullOrEmpty(tema) && !isNullOrEmpty(contingut)){
    				Usuari u = Usuari.find("byNom",n).first();
    				nou = new Blog(u,contingut,tema,titol).save();
    				CarregarBlog(nou.id);
    			}
    			else
    				renderText("Assegura't d'omplir el títol, el tema i el text a publicar!");
    		}
    		else{	//Modificar Blog amb id = idBlog
    			if(!isNullOrEmpty(titol) && !isNullOrEmpty(tema) && !isNullOrEmpty(contingut)){
    				Blog actual = Blog.find("byId",idBlog).first();
    				actual.titol = titol;
    				actual.tema = tema;
    				actual.contingut = contingut;
    				actual.save();
    				CarregarBlog(idBlog);
    			}
    			else
    				renderText("Assegura't d'omplir el títol, el tema i el text a publicar!");
    		}
    	}
    	else{
    		renderText("Inicia sessió per poguer crear blogs nous!");
    	}
    }

}
