package br.com.aaribeiro.uber.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

import br.com.aaribeiro.uber.R;
import br.com.aaribeiro.uber.helper.Helper;
import br.com.aaribeiro.uber.model.Usuario;

public class CadastroActivity extends AppCompatActivity {
    TextInputEditText txtCadastroNome;
    TextInputEditText txtCadastroEmail;
    TextInputEditText txtCadastroSenha;
    Switch swtCadastroMotorista;
    Switch swtCadastroPassageiro;
    Button btnCadastroCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);
        this.configurarToolBar();

        txtCadastroNome = findViewById(R.id.txtCadastroNome);
        txtCadastroEmail = findViewById(R.id.txtCadastroEmail);
        txtCadastroSenha = findViewById(R.id.txtCadastroSenha);
        swtCadastroMotorista = findViewById(R.id.swtCadastroMotorista);
        swtCadastroPassageiro = findViewById(R.id.swtCadastroPassageiro);
        btnCadastroCadastrar = findViewById(R.id.btnCadastroCadastrar);

        btnCadastroCadastrar.setOnClickListener(btnCadastroCadastrarClickListner);
        swtCadastroPassageiro.setOnClickListener(swtCadastroClickListner);
        swtCadastroMotorista.setOnClickListener(swtCadastroClickListner);
    }

    private void configurarToolBar(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Cadastrar uma conta");
    }

    View.OnClickListener swtCadastroClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == swtCadastroMotorista.getId()){
                swtCadastroPassageiro.setChecked(false);
            }
            if (v.getId() == swtCadastroPassageiro.getId()){
                swtCadastroMotorista.setChecked(false);
            }
        }
    };

    View.OnClickListener btnCadastroCadastrarClickListner = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (validarOpcaoPerfil()) {
                    cadastrarUsuario(Usuario.toEntity(
                            Helper.codificarBase64(txtCadastroEmail.getText().toString()),
                            txtCadastroNome.getText().toString(),
                            txtCadastroEmail.getText().toString(),
                            txtCadastroSenha.getText().toString(),
                            swtCadastroPassageiro.isChecked() ? "P" : "M"));
                }
            } catch (Exception e) {
                Toast.makeText(CadastroActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    };

    private Boolean validarOpcaoPerfil(){
        if (!swtCadastroPassageiro.isChecked() && !swtCadastroMotorista.isChecked()) {
            Toast.makeText(this, "Selecione uma opcao de perfil.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void cadastrarUsuario(final Usuario usuario){
        usuario.setUsuarioFirebaseAuth().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    try {
                        usuario.setUsuarioFirebaseDatabase();
                        startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                        Toast.makeText(CadastroActivity.this, "Seu cadastro foi realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(CadastroActivity.this, "Ocorreu um erro ao salvar os dados em nosso sistema.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    String excecao = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthWeakPasswordException e){
                        excecao = "Digite uma senha mais forte!";
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        excecao = "Digite um e-mail valido!";
                    } catch (FirebaseAuthUserCollisionException e){
                        excecao = "Já existe uma conta com este e-mail!";
                    } catch (Exception e){
                        excecao = "Houve um erro ao processar seu cadastro: " + e.getMessage();
                    }
                    Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

/*    private void redirecionarUsuarioPorPerfil(String perfil){
        if (perfil.equals("P")){
            startActivity(new Intent(CadastroActivity.this, PassageiroActivity.class));
        } else if (perfil.equals("M")){
            startActivity(new Intent(CadastroActivity.this, MotoristaActivity.class));
        }
    }*/
}