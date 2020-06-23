package br.com.aaribeiro.uber.activity;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.adapter.RequisicoesAdapter;
import br.com.aaribeiro.uber.config.ConfiguracaoFirebase;
import br.com.aaribeiro.uber.model.Requisicao;
import br.com.aaribeiro.uber.model.Usuario;

public class MotoristaActivity extends AppCompatActivity {
    private TextView txtSemRequisicoes;
    private RecyclerView recyclerRequisicoes;
    private List<Requisicao> corridas = new ArrayList<>();
    private RequisicoesAdapter requisicoesAdapter;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_motorista);

        txtSemRequisicoes = findViewById(R.id.txtSemRequisicoes);
        recyclerRequisicoes = findViewById(R.id.recyclerRequisicoes);

        recyclerRequisicoes.setVisibility(View.GONE);

        this.configurarRecyclerRequisicoes();
        this.recuperarLocalizacaoMotorista();

        //requisicoesAdapter.setNomePassageiroOnClickListener(passageiroOnClickListener);
        requisicoesAdapter.setCorridaSelecionadaOnClickListener(corridaSelecionadaOnClickListener);
    }

    View.OnClickListener corridaSelecionadaOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Requisicao corrida = corridas.get(requisicoesAdapter.getLayoutPosition());
            redirecionarMotoristaParaCorridaEmAtendimento(corrida);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnMenuSair) {
            ConfiguracaoFirebase.getFirebaseAuth().signOut();
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void redirecionarMotoristaParaCorridaEmAtendimento(Requisicao corrida){
        Intent intent = new Intent(MotoristaActivity.this, CorridaActivity.class);
        intent.putExtra("corrida", corrida);
        startActivity(intent);
    }

    private void verificarSeMotoristaEstaAtendendoAlgumaCorrida(){
        Requisicao.getCorridaMotoristaById().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null && dataSnapshot.getKey() != null) {
                    Requisicao corrida = dataSnapshot.getValue(Requisicao.class);
                    redirecionarMotoristaParaCorridaEmAtendimento(corrida);
                } else {
                    recuperarCorridasAbertas();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void recuperarCorridasAbertas(){
        Requisicao.getCorridasAbertas().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                controlarApresentacaoLayoutRequisicoes(dataSnapshot);
                corridas.clear();

                for (DataSnapshot corrida : dataSnapshot.getChildren()){
                    corridas.add(corrida.getValue(Requisicao.class));
                }
                requisicoesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    
    private void controlarApresentacaoLayoutRequisicoes(DataSnapshot requisicao){
        if (requisicao.getChildrenCount() > 0){
            recyclerRequisicoes.setVisibility(View.VISIBLE);
            txtSemRequisicoes.setVisibility(View.GONE);
        } else {
            recyclerRequisicoes.setVisibility(View.GONE);
            txtSemRequisicoes.setVisibility(View.VISIBLE);
        }
    }

    private void configurarRecyclerRequisicoes(){
        requisicoesAdapter = new RequisicoesAdapter(corridas, MotoristaActivity.this, Usuario.getUsuarioSessao());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MotoristaActivity.this);
        recyclerRequisicoes.setLayoutManager(layoutManager);
        recyclerRequisicoes.setHasFixedSize(true);
        recyclerRequisicoes.setAdapter(requisicoesAdapter);
    }

    private void recuperarLocalizacaoMotorista() {
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Solicitar atualizacao de localizacao
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    0, //segundos
                    0, //metros
                    locationListener
            );
        }
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Usuario.getUsuarioSessao().setLatitude(String.valueOf(location.getLatitude()));
            Usuario.getUsuarioSessao().setLongitude(String.valueOf(location.getLongitude()));

            verificarSeMotoristaEstaAtendendoAlgumaCorrida();

            locationManager.removeUpdates(locationListener);
            requisicoesAdapter.notifyDataSetChanged();

            Requisicao.updateDadosLocalizacaoGeofire().setLocation(
                    Usuario.getUsuarioSessao().getId(),
                    new GeoLocation(location.getLatitude(), location.getLongitude()),
                    new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                        }
                    });
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    };

}
