package br.com.aaribeiro.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseError;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.model.Requisicao;
import br.com.aaribeiro.uber.model.Usuario;

public class CorridaActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Marker marcadorMotorista;
    private Marker marcadorPassageiro;
    private Marker marcadorDestino;
    private Button btnMotoristaAceitarCorrida;
    private String latLngDeNavegacaoGoogleMaps;
    private Requisicao corrida;
    private Circle circuloLocalizacao;
    private FloatingActionButton btnCorridaRota;
    private static String MODO_DIRIGINDO = "d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_corrida);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnMotoristaAceitarCorrida = findViewById(R.id.btnMotoristaAceitarCorrida);
        btnCorridaRota = findViewById(R.id.btnCorridaRota);

        btnMotoristaAceitarCorrida.setOnClickListener(btnMotoristaAceitarCorridaClickListener);
        btnCorridaRota.setOnClickListener(fabRotaClickListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        this.recuperarDadosDaCorridaSelecionada();
        this.recuperarLocalizacaoMotorista();
    }

/*    @Override
    public boolean onSupportNavigateUp() {
        Toast.makeText(CorridaActivity.this, "Você tem uma corrida em andamento. Conclua esta corrida para poder iniciar uma nova.", Toast.LENGTH_LONG).show();
        return false;
    }*/

    View.OnClickListener fabRotaClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Uri uri = Uri.parse("google.navigation:q=" + latLngDeNavegacaoGoogleMaps + "&mode="+ MODO_DIRIGINDO);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.apps.maps");
            startActivity(intent);
        }
    };

    View.OnClickListener btnMotoristaAceitarCorridaClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            atualizarParaAcaminhoDoPassageiro();
            corrida.setStatus(Requisicao.MOTORISTA_A_CAMINHO_PASSAGEIRO);
            Requisicao.setCorridaPassageiroById(corrida, corrida.getPassageiro().getId());
            Requisicao.setCorridaMotoristaById(corrida, corrida.getMotorista().getId());
            Requisicao.deleteCorridaAbertaById(corrida.getPassageiro().getId());
        }
    };

    private void atualizarParaAcaminhoDoPassageiro(){
        btnMotoristaAceitarCorrida.setText("A caminho do passageiro");
        btnCorridaRota.setVisibility(View.VISIBLE);

        this.adicionarMarcadorMotorista(this.getLatLong(corrida.getMotorista().getLatitude(), corrida.getMotorista().getLongitude()));
        this.adicionarMarcadorPassageiro(this.getLatLong(corrida.getPassageiro().getLatitude(), corrida.getPassageiro().getLongitude()));

        this.centralizarMarcadores(new Marker[]{marcadorMotorista, marcadorPassageiro});

        this.monitorarChegadaDoMotorista(
                Double.parseDouble(corrida.getPassageiro().getLatitude()),
                Double.parseDouble(corrida.getPassageiro().getLongitude()),
                Requisicao.MOTORISTA_A_CAMINHO_DESTINO
        );
    }

    private void atualizarParaAcaminhoDoDestino(){
        btnMotoristaAceitarCorrida.setText("A caminho do destino");
        btnCorridaRota.setVisibility(View.VISIBLE);

        this.adicionarMarcadorMotorista(this.getLatLong(corrida.getMotorista().getLatitude(), corrida.getMotorista().getLongitude()));
        this.adicionarMarcadorDestino(this.getLatLong(corrida.getDestino().getLatitude(), corrida.getDestino().getLongitude()));

        this.centralizarMarcadores(new Marker[]{marcadorMotorista, marcadorDestino});

        this.monitorarChegadaDoMotorista(
                Double.parseDouble(corrida.getDestino().getLatitude()),
                Double.parseDouble(corrida.getDestino().getLongitude()),
                Requisicao.CORRIDA_FINALIZADA
        );
    }

    private void atualizarParaCorridaFinalizada(){
        btnMotoristaAceitarCorrida.setText("Corrida Finalizada - R$ " + this.calcularPrecoCorrida(this.calcularDistanciaCorrida()));
        btnCorridaRota.setVisibility(View.GONE);

        if (marcadorMotorista != null){
            marcadorMotorista.remove();
        }
        this.adicionarMarcadorDestino(this.getLatLong(corrida.getDestino().getLatitude(), corrida.getDestino().getLongitude()));

        this.centralizarMarcadores(new Marker[]{marcadorDestino});

        circuloLocalizacao.remove();
    }

    private void monitorarChegadaDoMotorista(Double latitude, Double longitude, final String statusCorrida){
        final GeoQuery geoQuery = Requisicao.updateDadosLocalizacaoGeofire()
                .queryAtLocation(new GeoLocation(latitude, longitude), 0.05); //kilometros (50 metros)

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (key.equals(corrida.getMotorista().getId())){
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", statusCorrida);

                    Requisicao.updateStatusCorridaMotorista(status, corrida.getMotorista().getId());
                    Requisicao.updateStatusCorridaPassageiro(status, corrida.getPassageiro().getId());

                    geoQuery.removeAllListeners();
                    circuloLocalizacao.remove();
                    verificarStatusDaCorrida(statusCorrida);
                }
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
            }
        });
    }

    private void centralizarMarcadores(Marker[] markers){
        LatLngBounds.Builder marcadores = new LatLngBounds.Builder();

        for (Marker marcador : markers){
            marcadores.include(marcador.getPosition());
        }
        LatLngBounds bounds = marcadores.build();

        int larguraDisplay = getResources().getDisplayMetrics().widthPixels;
        int alturaDisplay = getResources().getDisplayMetrics().heightPixels;
        int paddingInterno = (int) (larguraDisplay * 0.25);

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, larguraDisplay, alturaDisplay, paddingInterno));
    }

    private void adicionarMarcadorMotorista(LatLng localizacaoMotorista){
        if (marcadorMotorista != null){
            marcadorMotorista.remove();
        }
        marcadorMotorista = mMap.addMarker(new MarkerOptions()
                .position(localizacaoMotorista)
                .title(corrida.getMotorista().getNome())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.carro))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoMotorista, 17));
    }

    private void adicionarMarcadorPassageiro(LatLng localizacaoPassageiro){
        if (marcadorPassageiro != null){
            marcadorPassageiro.remove();
        }
        marcadorPassageiro = mMap.addMarker(new MarkerOptions()
                .position(localizacaoPassageiro)
                .title(corrida.getPassageiro().getNome())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
        this.adicionarCirculoDeMarcacao(this.getLatLong(corrida.getPassageiro().getLatitude(), corrida.getPassageiro().getLongitude()));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoPassageiro, 17));
    }

    private void adicionarMarcadorDestino(LatLng localizacaoDestino){
        if (marcadorPassageiro != null){
            marcadorPassageiro.remove();
        }
        if (marcadorDestino != null){
            marcadorDestino.remove();
        }
        marcadorDestino = mMap.addMarker(new MarkerOptions()
                .position(localizacaoDestino)
                .title(corrida.getPassageiro().getNome())
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.destino))
        );
        this.adicionarCirculoDeMarcacao(localizacaoDestino);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoDestino, 17));
    }

    private void adicionarCirculoDeMarcacao(LatLng latLng){
        circuloLocalizacao = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(50) //metros
                .fillColor(Color.argb(90, 255, 153, 0))
                .strokeColor(Color.argb(190, 255, 152,0))
        );
    }

    private LatLng getLatLong(String latitude, String longitude){
        return new LatLng(
                Double.parseDouble(latitude),
                Double.parseDouble(longitude)
        );
    }

    private void recuperarDadosDaCorridaSelecionada(){
        if (getIntent().getExtras().containsKey("corrida")){
            corrida = (Requisicao) getIntent().getExtras().getSerializable("corrida");
            corrida.setMotorista(Usuario.getUsuarioSessao());

            this.verificarStatusDaCorrida(corrida.getStatus());
        } else {
            Toast.makeText(CorridaActivity.this, "Opss algo deu errado...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void verificarStatusDaCorrida(String status){
        if (status.equals(Requisicao.MOTORISTA_A_CAMINHO_PASSAGEIRO)){
            latLngDeNavegacaoGoogleMaps = (corrida.getPassageiro().getLatitude() + "," + corrida.getPassageiro().getLongitude());
            this.atualizarParaAcaminhoDoPassageiro();

        } else if (status.equals(Requisicao.MOTORISTA_A_CAMINHO_DESTINO)){
            latLngDeNavegacaoGoogleMaps = (corrida.getDestino().getLatitude() + "," + corrida.getDestino().getLongitude());
            this.atualizarParaAcaminhoDoDestino();

        } else if (status.equals(Requisicao.CORRIDA_FINALIZADA)){
            this.atualizarParaCorridaFinalizada();

        } else {
            this.adicionarMarcadorMotorista(this.getLatLong(corrida.getMotorista().getLatitude(), corrida.getMotorista().getLongitude()));
        }
    }

    private void recuperarLocalizacaoMotorista() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        //Solicitar atualizacao de localizacao
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, //segundos
                    10, //metros
                    locationListener
            );
        }
    }

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            corrida.getMotorista().setLatitude(String.valueOf(location.getLatitude()));
            corrida.getMotorista().setLongitude(String.valueOf(location.getLongitude()));

            adicionarMarcadorMotorista(getLatLong(corrida.getMotorista().getLatitude(), corrida.getMotorista().getLongitude()));
            //implementar monitorarChegadaDoMotorista() para chegar em tempo real a chegada

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

    private float calcularDistanciaCorrida(){
        Location localInicial = new Location("Local Inicial");
        localInicial.setLatitude(Double.parseDouble(corrida.getPassageiro().getLatitude()));
        localInicial.setLongitude(Double.parseDouble(corrida.getPassageiro().getLongitude()));

        Location localFinal = new Location("Local Final");
        localFinal.setLatitude(Double.parseDouble(corrida.getDestino().getLatitude()));
        localFinal.setLongitude(Double.parseDouble(corrida.getDestino().getLongitude()));

        return localInicial.distanceTo(localFinal) / 1000; //Resultado eh dado em metros. Convertendo em KM
    }

    private String calcularPrecoCorrida(float distancia){
        DecimalFormat decimalFormat = new DecimalFormat("0.00");
        float valor = distancia * 4;
        return decimalFormat.format(valor);
    }
}
