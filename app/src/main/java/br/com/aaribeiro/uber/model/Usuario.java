package br.com.aaribeiro.uber.model;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DatabaseReference;

import java.io.Serializable;

import br.com.aaribeiro.uber.config.ConfiguracaoFirebase;
import br.com.aaribeiro.uber.helper.Helper;

public class Usuario implements Serializable {
    private String id;
    private String nome;
    private String email;
    private String senha;
    private String perfil;
    private String latitude;
    private String longitude;
    private static Usuario sessao;

    private static final String USUARIOS = "usuarios";

    private Usuario(){}

    private Usuario(String id, String nome, String email, String senha, String perfil) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
        this.perfil = perfil;
    }

    private Usuario validar() throws Exception {
        if (nome == null || nome.equals("")) {
            throw new Exception("Seu nome nao foi preenchido.");
        }
        if (email == null || email.equals("")) {
            throw new Exception("Seu e-mail nao foi preenchido.");
        }
        if (senha == null || senha.equals("")) {
            throw new Exception("Sua senha nao foi preenchida.");
        }
        return this;
    }

    public static DatabaseReference getUsuarioFirebaseDatabase(String email) {
        String id = Helper.codificarBase64(email);
        return ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(USUARIOS)
                .child(id);
    }

    public void setUsuarioFirebaseDatabase() throws Exception {
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(USUARIOS)
                .child(id)
                .setValue(this);
    }

    public static Task<AuthResult> getUsuarioFirebaseAuth(String email, String senha){
        return ConfiguracaoFirebase.getFirebaseAuth().signInWithEmailAndPassword(email, senha);
    }

    public Task<AuthResult> setUsuarioFirebaseAuth(){
        return ConfiguracaoFirebase.getFirebaseAuth().createUserWithEmailAndPassword(email, senha);
    }

    public static Usuario toEntity(String id, String nome, String email, String senha, String perfil) throws Exception {
        return new Usuario(id, nome, email, senha, perfil).validar();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String lat) {
        latitude = lat;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String lon) {
        longitude = lon;
    }

    public static void setUsuarioSessao(Usuario usuario){
        sessao = usuario;
    }

    public static Usuario getUsuarioSessao(){
        return sessao;
    }
}
