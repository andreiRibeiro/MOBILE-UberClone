package br.com.aaribeiro.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.helper.Helper;

public class MainActivity extends AppCompatActivity {
    Button btnLogin;
    Button btnCadastrar;
    String[] permissoes = new String[] {Manifest.permission.INTERNET, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        Helper.validarPermissoes(this, permissoes);

        btnLogin = findViewById(R.id.btnLogin);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnLogin.setOnClickListener(btnLoginClickListner);
        btnCadastrar.setOnClickListener(btnCadastrarClickListner);
    }

    View.OnClickListener btnLoginClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
    };

    View.OnClickListener btnCadastrarClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, CadastroActivity.class));
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults){
            if (permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaPermissaoNegada();
            }
        }
    }

    private void alertaPermissaoNegada(){
        AlertDialog.Builder alerta = new AlertDialog.Builder(this);
        alerta.setTitle("Permissões negadas");
        alerta.setMessage("Para utilizar o app é necessário aceitar as permissões.");
        alerta.setCancelable(false);
        alerta.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        alerta.create().show();
    }
}
