package br.com.aaribeiro.uber.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.config.ConfiguracaoFirebase;
import br.com.aaribeiro.uber.model.Destino;
import br.com.aaribeiro.uber.model.Requisicao;
import br.com.aaribeiro.uber.model.Usuario;

public class PassageiroActivity extends AppCompatActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private Button btnPassageiroSolicitarUber;
    private EditText txtEnderecoDestino;
    private Destino destino;
    private Boolean uberJaFoiSolicitado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passageiro);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.verificarSePassageiroJaEstaSendoAtendido();

        btnPassageiroSolicitarUber = findViewById(R.id.btnPassageiroSolicitarUber);
        txtEnderecoDestino = findViewById(R.id.txtEnderecoDestino);

        btnPassageiroSolicitarUber.setOnClickListener(btnPassageiroSolicitarUberClickListner);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        this.recuperarLocalizacaoPassageiro();
    }

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

    private void adicionarMarcadorPassageiro(LatLng localizacaoPassageiro){
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(localizacaoPassageiro)
                .title("Meu local")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.usuario))
        );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacaoPassageiro, 17));
    }



    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Usuario.getUsuarioSessao().setLatitude(String.valueOf(location.getLatitude()));
            Usuario.getUsuarioSessao().setLongitude(String.valueOf(location.getLongitude()));
            adicionarMarcadorPassageiro(new LatLng(location.getLatitude(), location.getLongitude()));

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

    private void recuperarLocalizacaoPassageiro() {
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

    View.OnClickListener btnPassageiroSolicitarUberClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String enderecoDestino = txtEnderecoDestino.getText().toString();

            if (!uberJaFoiSolicitado) {
                if (!enderecoDestino.equals("")) {
                    Address addressDestino = recuperarEnderecoDestinoUsuario(enderecoDestino);
                    if (addressDestino != null) {
                        destino = new Destino(
                                addressDestino.getAdminArea(),
                                addressDestino.getThoroughfare(),
                                addressDestino.getSubLocality(),
                                addressDestino.getFeatureName(),
                                addressDestino.getPostalCode(),
                                String.valueOf(addressDestino.getLatitude()),
                                String.valueOf(addressDestino.getLongitude())
                        );

                        AlertDialog.Builder dialogConfirmacaoEndereco = new AlertDialog.Builder(PassageiroActivity.this)
                                .setTitle("Confirme seu endereço!")
                                .setMessage(destino.toString())
                                .setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        salvarCorrida();
                                        ocultarLayoutLocalizacaoPassageiro();
                                        alterarBotaoParaCancelarUber();
                                        verificarSePassageiroJaEstaSendoAtendido();
                                        uberJaFoiSolicitado = true;
                                    }
                                })
                                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                });
                        dialogConfirmacaoEndereco.create().show();
                    } else {
                        Toast.makeText(PassageiroActivity.this, "Não foi possivel localizar o endereço informado!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PassageiroActivity.this, "Informe um endereço valido", Toast.LENGTH_SHORT).show();
                }
            } else {
                //cancelar uber
            }
        }
    };

    private Address recuperarEnderecoDestinoUsuario(String enderecoDestino){
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaEnderecos = geocoder.getFromLocationName(enderecoDestino, 1);
            if (listaEnderecos != null && !listaEnderecos.isEmpty()){
                return listaEnderecos.get(0);
            }
        } catch (Exception e){
            Toast.makeText(PassageiroActivity.this, "Ocorreu um erro ao buscar seu endereço.", Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    private void salvarCorrida(){
        Requisicao requisicao = new Requisicao(
                null,
                Requisicao.AGUARDANDO_MOTORISTA,
                Usuario.getUsuarioSessao(),
                null,
                destino
        );
        requisicao.setCorridaAbertaById(Usuario.getUsuarioSessao().getId());
    }

    private void ocultarLayoutLocalizacaoPassageiro(){
        LinearLayout layoutLocalizacaoPassageiro = findViewById(R.id.layoutLocalizacaoPassageiro);
        layoutLocalizacaoPassageiro.setVisibility(View.GONE);
    }

    private void alterarBotaoParaCancelarUber(){
        Button btnPassageiroSolicitarUber = findViewById(R.id.btnPassageiroSolicitarUber);
        btnPassageiroSolicitarUber.setText("Cancelar Uber");
    }

    private void verificarSePassageiroJaEstaSendoAtendido(){
        Requisicao.getCorridaPassageiroById().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getKey() != null && dataSnapshot.getValue() != null) {
                    ocultarLayoutLocalizacaoPassageiro();
                    alterarBotaoParaCancelarUber();
                    uberJaFoiSolicitado = true;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
