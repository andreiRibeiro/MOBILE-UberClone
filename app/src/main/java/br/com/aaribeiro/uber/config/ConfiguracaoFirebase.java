package br.com.aaribeiro.uber.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracaoFirebase {

    private static FirebaseAuth firebaseAuth;
    private static FirebaseDatabase firebaseDatabase;

    //Retorna uma instancia FirebaseAuth
    public static FirebaseAuth getFirebaseAuth(){
        if (firebaseAuth == null){
            firebaseAuth = FirebaseAuth.getInstance();
        }
        return firebaseAuth;
    }

    //Retorna uma instancia FirebaseDatabase
    public static FirebaseDatabase getFirebaseDatabase(){
        if (firebaseDatabase == null){
            firebaseDatabase = FirebaseDatabase.getInstance();
        }
        return firebaseDatabase;
    }
}
