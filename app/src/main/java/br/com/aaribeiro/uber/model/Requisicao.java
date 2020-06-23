package br.com.aaribeiro.uber.model;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.io.Serializable;
import java.util.Map;

import br.com.aaribeiro.uber.config.ConfiguracaoFirebase;

public class Requisicao implements Serializable {
    private String id;
    private String status;
    private Usuario passageiro;
    private Usuario motorista;
    private Destino destino;

    public static final String AGUARDANDO_MOTORISTA           = "aguardando_motorista";
    public static final String MOTORISTA_A_CAMINHO_PASSAGEIRO = "motorista_a_caminho_passageiro";
    public static final String MOTORISTA_A_CAMINHO_DESTINO    = "motorista_a_caminho_destino";
    public static final String CORRIDA_FINALIZADA             = "corrida_finalizada";

    private static final String CORRIDAS               = "corridas";
    private static final String ABERTAS                = "abertas";
    private static final String PASSAGEIROS            = "passageiros";
    private static final String MOTORISTAS             = "motoristas";
    private static final String CONCLUIDAS_PASSAGEIROS = "concluidas_passageiros";
    private static final String CONCLUIDAS_MOTORISTAS  = "concluidas_motoristas";

    public Requisicao() {}

    public Requisicao(String id, String status, Usuario passageiro, Usuario motorista, Destino destino) {
        this.id = id;
        this.status = status;
        this.passageiro = passageiro;
        this.motorista = motorista;
        this.destino = destino;
    }

    public static Query getCorridaPassageiroById(){
        DatabaseReference requisicoes = ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(PASSAGEIROS)
                .child(Usuario.getUsuarioSessao().getId());
        return requisicoes.orderByChild(Usuario.getUsuarioSessao().getId());
    }

    public static Query getCorridaMotoristaById(){
        DatabaseReference requisicoes = ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(MOTORISTAS)
                .child(Usuario.getUsuarioSessao().getId());
        return requisicoes.orderByChild(Usuario.getUsuarioSessao().getId());
    }

    public static Query getCorridasAbertas(){
        DatabaseReference requisicoes = ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(ABERTAS);
        return requisicoes.orderByChild(ABERTAS);
    }

    public static void updateCorridaPassageiroById(Map<String, Object> corrida, String idPassageiro){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(PASSAGEIROS)
                .child(idPassageiro)
                .updateChildren(corrida);
    }

    public static void setCorridaMotoristaById(Requisicao corrida, String idMotorista){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(MOTORISTAS)
                .child(idMotorista)
                .setValue(corrida);
    }

    public static void updateStatusCorridaMotorista(Map status, String idMotorista){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(MOTORISTAS)
                .child(idMotorista)
                .updateChildren(status);
    }

    public static void updateStatusCorridaPassageiro(Map status, String idPassageiro){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(PASSAGEIROS)
                .child(idPassageiro)
                .updateChildren(status);
    }

    public static void setCorridaPassageiroById(Requisicao corrida, String idPassageiro){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(PASSAGEIROS)
                .child(idPassageiro)
                .setValue(corrida);
    }

    public void setCorridaAbertaById(String idPassageiro){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(ABERTAS)
                .child(idPassageiro)
                .setValue(this);
    }

    public static void deleteCorridaAbertaById(String idPassageiro){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(ABERTAS)
                .child(idPassageiro)
                .removeValue();
    }

    public static GeoFire updateDadosLocalizacaoGeofire(){
        DatabaseReference localUsuario = ConfiguracaoFirebase.getFirebaseDatabase().getReference().child("geofire");
       return new GeoFire(localUsuario);
    }

/*    private String getIdRequisicao(String id){
        return ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(CONCLUIDAS)
                .child(id)
                .push().getKey();
    }

    public void setCorridaMotorista(String id){
        ConfiguracaoFirebase.getFirebaseDatabase().getReference()
                .child(CORRIDAS)
                .child(MOTORISTAS)
                .child(id)
                .child(this.getIdRequisicao(id))
                .setValue(this);
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Usuario getPassageiro() {
        return passageiro;
    }

    public void setPassageiro(Usuario passageiro) {
        this.passageiro = passageiro;
    }

    public Usuario getMotorista() {
        return motorista;
    }

    public void setMotorista(Usuario motorista) {
        this.motorista = motorista;
    }

    public Destino getDestino() {
        return destino;
    }

    public void setDestino(Destino destino) {
        this.destino = destino;
    }
}
