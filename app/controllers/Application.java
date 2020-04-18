package controllers;

import play.*;
import play.data.validation.Valid;
import play.db.jpa.JPA;
import play.mvc.*;

import java.util.*;

import models.*;

import javax.persistence.*;

public class Application extends Controller {

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

    public static void login(@Valid Usuari user){
        Usuari u = Usuari.find("byNomAndContra", user.nom, user.contra).first();
        if(u != null) {
        	Usuari u2 = new Usuari(); u2.nom = user.nom;
            session.put("user", u2.nom);
            LlistaBlogsPerUsuari(u2.nom);
        }else {
            renderText("Error de login");
        }
    }

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
        Usuari u = Usuari.find("byNom",nom).first();
        if (u!=null) {
            List<Blog> blogs = u.blogs;
            renderArgs.put("usuari_visualitzat",u);
            renderArgs.put("blogs",u.blogs);
            renderTemplate("Application/loggedIn.html");
        }else {
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

    public static void BorrarUsuari(){
      String n = session.get("user");
      if(n!=null){
        Usuari u = Usuari.find("byNom",n).first();
        if(u!=null){
            for(Blog b: u.blogs){
              b.delete();
            }
            for(Comentari c: u.comentaris){
              c.delete();
            }
            u.delete();
            session.remove("user");
            index();
        }
        else
            renderText("Usuari no trobat.");
      }
    }

    public static void CarregarLoggedIn(){
    	LlistaBlogsPerUsuari(session.get("user"));
    }

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

    public static void CarregarBlogNou(int blogId){
    	String n = session.get("user");
    	if(n!=null){
    		Usuari u = new Usuari(); u.nom = n;
    		renderArgs.put("user",u);
    	}
    	else{
    		renderText("Login primer!");
    	}

    	if(blogId>0){	//Això vol dir que estem editant un blog, no creant-lo
    		Blog b = Blog.find("byId",(long)blogId).first();
    		if(b==null) renderText("El blog a modificar no existeix!");
    		renderArgs.put("blog",b);
    	}

    	renderTemplate("Application/blognou.html");
    }

    public static void BotoComentar(String contingut, int blogId){
    	String n = session.get("user");
    	if(n!=null){
        	if(!(contingut == null || contingut == "Comentari nou..." || contingut.trim().length() == 0 || contingut.length() == 0)){
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

    private static Boolean isNullOrEmpty(String s){
    	if(s == null || s.trim().length() == 0 || s.length() == 0 || s == ""){
    		return true;
    	}
    	else return false;
    }

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
