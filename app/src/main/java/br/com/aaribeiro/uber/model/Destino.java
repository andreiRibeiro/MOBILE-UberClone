package br.com.aaribeiro.uber.model;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class Destino implements Serializable {

    private String estado;
    private String rua;
    private String bairro;
    private String numero;
    private String cep;
    private String latitude;
    private String longitude;

    private Destino(){}

    public Destino(String estado, String rua, String bairro, String numero, String cep, String latitude, String longitude) {
        this.estado = estado;
        this.rua = rua;
        this.bairro = bairro;
        this.numero = numero;
        this.cep = cep;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getBairro() {
        return bairro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder mensagemConfirmacao = new StringBuilder();
        mensagemConfirmacao.append("Estado: " + getEstado() + "\n");
        mensagemConfirmacao.append("Rua: " + getRua() + "\n");
        mensagemConfirmacao.append("Bairro: " + getBairro() + "\n");
        mensagemConfirmacao.append("Numero: " + getNumero() + "\n");
        mensagemConfirmacao.append("Cep: " + getCep() + "\n");
        return mensagemConfirmacao.toString();
    }
}
