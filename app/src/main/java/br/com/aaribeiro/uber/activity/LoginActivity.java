package br.com.aaribeiro.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.helper.Helper;
import br.com.aaribeiro.uber.model.Usuario;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText txtEmail;
    TextInputEditText txtSenha;
    Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.configurarToolBar();

        txtEmail = findViewById(R.id.txtLoginEmail);
        txtSenha = findViewById(R.id.txtLoginSenha);
        btnEntrar = findViewById(R.id.btnLoginEntrar);
        btnEntrar.setOnClickListener(btnEntrarClickListner);
    }

    private void configurarToolBar(){
        getSupportActionBar().setTitle("Acessar minha conta");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    View.OnClickListener btnEntrarClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (validarCamposLogin()) {
                Usuario.getUsuarioFirebaseAuth(txtEmail.getText().toString(), txtSenha.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Usuario.getUsuarioFirebaseDatabase(txtEmail.getText().toString())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    Usuario usuario = dataSnapshot.getValue(Usuario.class);
                                                    Usuario.setUsuarioSessao(usuario);
                                                    redirecionarUsuarioPorPerfil(usuario.getPerfil(), usuario.getNome());
                                                }
                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                } else {
                                    String exception = "";
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthInvalidUserException e) {
                                        exception = "Usuario nao cadastrado!";
                                    } catch (FirebaseAuthInvalidCredentialsException e) {
                                        exception = "E-mail e/ou senha invalido(s)";
                                    } catch (Exception e) {
                                        exception = "Erro ao realizar login!";
                                    }
                                    Toast.makeText(LoginActivity.this, exception, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    };

    private Boolean validarCamposLogin(){
        if (txtEmail == null || txtEmail.getText().toString().isEmpty()){
            Toast.makeText(LoginActivity.this, "Preencha o campo E-mail!", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (txtSenha == null || txtSenha.getText().toString().isEmpty()){
            Toast.makeText(LoginActivity.this, "Preencha o campo senha!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void redirecionarUsuarioPorPerfil(String perfil, String nomeUsuario){
        if (perfil.equals("P")){
            startActivity(new Intent(LoginActivity.this, PassageiroActivity.class));
            Toast.makeText(LoginActivity.this, "Bem vindo passageiro " + nomeUsuario + "!", Toast.LENGTH_LONG).show();
        } else if (perfil.equals("M")){
            startActivity(new Intent(LoginActivity.this, MotoristaActivity.class));
            Toast.makeText(LoginActivity.this, "Bem vindo motorista " + nomeUsuario + "!", Toast.LENGTH_LONG).show();
        }
    }
}
