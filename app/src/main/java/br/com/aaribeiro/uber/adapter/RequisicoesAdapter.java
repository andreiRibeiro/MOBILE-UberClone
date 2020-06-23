package br.com.aaribeiro.uber.adapter;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.List;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.model.Requisicao;
import br.com.aaribeiro.uber.model.Usuario;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.RequisicoesViewHolder> {
    private List<Requisicao> requisicoes;
    private Context context;
    private Usuario motorista;
    private Usuario passageiro;
    private View.OnClickListener nomePassageiroOnClickListener;
    private View.OnClickListener corridaSelecionadaOnClickListener;
    private int layoutPosition;

    public RequisicoesAdapter(List<Requisicao> requisicoes, Context context, Usuario motorista) {
        this.requisicoes = requisicoes;
        this.context = context;
        this.motorista = motorista;
    }

    @NonNull
    @Override
    public RequisicoesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_requisicoes, parent, false);
        return new RequisicoesViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull RequisicoesViewHolder holder, int position) {
        Requisicao requisicao = requisicoes.get(position);
        this.passageiro = requisicao.getPassageiro();

        if (motorista != null){
            holder.txtDistanciaPassageiro.setText(this.formatarDistancia(this.calcularDistanciaCorrida()) + " - aproximadamente");
        }
        holder.txtNomePassageiro.setText(passageiro.getNome());
    }

    private String formatarDistancia(float distancia){
        if (distancia < 1){
            distancia = distancia * 1000;
            return  Math.round(distancia) + " mts";
        } else {
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            return decimalFormat.format(distancia) + " Km";
        }
    }

    private float calcularDistanciaCorrida(){
        Location localInicial = new Location("Local Inicial");
        localInicial.setLatitude(Double.parseDouble(motorista.getLatitude()));
        localInicial.setLongitude(Double.parseDouble(motorista.getLongitude()));

        Location localFinal = new Location("Local Final");
        localFinal.setLatitude(Double.parseDouble(passageiro.getLatitude()));
        localFinal.setLongitude(Double.parseDouble(passageiro.getLongitude()));

        return localInicial.distanceTo(localFinal) / 1000; //Resultado eh dado em metros. Convertendo em KM
    }

    @Override
    public int getItemCount() {
        return requisicoes.size();
    }

    public class RequisicoesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtNomePassageiro;
        private TextView txtDistanciaPassageiro;

        public RequisicoesViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomePassageiro = itemView.findViewById(R.id.txtRequisicaoNomePassageiro);
            txtDistanciaPassageiro = itemView.findViewById(R.id.txtRequisicaoDistanciaPassageiro);

            itemView.setOnClickListener(this); //pega clique da view toda
            //txtNomePassageiro.setOnClickListener(nomePassageiroOnClickListener); //pega clique do campo especifico
        }

        @Override
        public void onClick(View v) {
            layoutPosition = getLayoutPosition();
            corridaSelecionadaOnClickListener.onClick(v);
        }
    }

    public void setNomePassageiroOnClickListener(View.OnClickListener implementacaoOnClick){
        this.nomePassageiroOnClickListener = implementacaoOnClick;
    }

    public void setCorridaSelecionadaOnClickListener(View.OnClickListener implementacaoOnClick){
        this.corridaSelecionadaOnClickListener = implementacaoOnClick;
    }

    public int getLayoutPosition() {
        return layoutPosition;
    }
}
