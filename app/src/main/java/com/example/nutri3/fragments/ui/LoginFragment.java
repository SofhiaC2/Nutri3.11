package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.text.TextUtils; // Importar TextUtils
import android.util.Log;    // Importar Log
import android.util.Patterns; // Importar Patterns
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;   // Importar Button
import android.widget.EditText; // Importar EditText
import android.widget.ProgressBar; // Importar ProgressBar (opcional)
import android.widget.TextView;
import android.widget.Toast;    // Importar Toast

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth; // Importar FirebaseAuth
import com.google.firebase.auth.FirebaseUser; // Importar FirebaseUser

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment"; // Tag para logs

    private EditText etEmailLogin;
    private EditText etSenhaLogin;
    private Button btnEntrar;
    private TextView btnRegister;
    private TextView btnRecover;
    private ProgressBar progressBarLogin; // Opcional: para feedback visual

    private FirebaseAuth mAuth;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance(); // Inicializa o FirebaseAuth
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa as Views
        etEmailLogin = view.findViewById(R.id.email); // Substitua pelo ID correto do seu EditText de email
        etSenhaLogin = view.findViewById(R.id.senha); // Substitua pelo ID correto do seu EditText de senha
        btnEntrar = view.findViewById(R.id.btnEntrar);       // Substitua pelo ID correto do seu Button de Entrar
        btnRegister = view.findViewById(R.id.btnRegister);
        btnRecover = view.findViewById(R.id.btnRecover);
       // progressBarLogin = view.findViewById(R.id.progressBarLogin); // Substitua pelo ID correto, se você tiver um ProgressBar

        // Configura o OnClickListener para o botão de Entrar
        btnEntrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUsuario();
            }
        });

        btnRegister.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                try {
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_loginFragment_to_register);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Ação action_loginFragment_to_registerFragment não encontrada.", e);
                }
            }
        });

        btnRecover.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                try {
                    NavHostFragment.findNavController(LoginFragment.this)
                            .navigate(R.id.action_loginFragment_to_recover_accountFragment);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Ação action_loginFragment_to_recover_accountFragment não encontrada.", e);
                }
            }
        });
    }

    private void loginUsuario() {
        String email = etEmailLogin.getText().toString().trim();
        String senha = etSenhaLogin.getText().toString().trim();

        // ... (suas validações de email e senha) ...
        if (TextUtils.isEmpty(email)) {
            // ...
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // ...
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            // ...
            return;
        }

        if (progressBarLogin != null) {
            progressBarLogin.setVisibility(View.VISIBLE);
        }
        btnEntrar.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(requireActivity(), task -> { // 'requireActivity()' aqui é bom para o listener em si
                    // É CRUCIAL verificar se o fragmento ainda está no estado esperado
                    // ANTES de interagir com a UI ou usar getContext().
                    if (!isAdded()) {
                        // O Fragment não está mais anexado à sua Activity.
                        // Não faça nada relacionado à UI.
                        return;
                    }

                    if (progressBarLogin != null) {
                        progressBarLogin.setVisibility(View.GONE);
                    }
                    btnEntrar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        // Verifique o contexto ANTES de usar
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                        }
                        // O AuthStateListener na MainActivity deve cuidar do resto.

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        // Verifique o contexto ANTES de usar
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Falha na autenticação: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Erro desconhecido."),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}


