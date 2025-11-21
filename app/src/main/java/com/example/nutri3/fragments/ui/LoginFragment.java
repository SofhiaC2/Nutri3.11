package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private EditText etEmailLogin;
    private EditText etSenhaLogin;
    private Button btnEntrar;
    private TextView btnRegister;
    private TextView btnRecover;
    private ProgressBar progressBarLogin;

    private FirebaseAuth mAuth;

    public LoginFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmailLogin = view.findViewById(R.id.email);
        etSenhaLogin = view.findViewById(R.id.senha);
        btnEntrar = view.findViewById(R.id.btnEntrar);
        btnRegister = view.findViewById(R.id.btnRegister);
        btnRecover = view.findViewById(R.id.btnRecover);

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
                            .navigate(R.id.action_loginFragment_to_registerFragment);
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

        if (TextUtils.isEmpty(email)) {
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return;
        }
        if (TextUtils.isEmpty(senha)) {
            return;
        }

        if (progressBarLogin != null) {
            progressBarLogin.setVisibility(View.VISIBLE);
        }
        btnEntrar.setEnabled(false);

        mAuth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (!isAdded()) {
                        return;
                    }

                    if (progressBarLogin != null) {
                        progressBarLogin.setVisibility(View.GONE);
                    }
                    btnEntrar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithEmail:success");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                        }

                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Falha na autenticação: " +
                                            (task.getException() != null ? task.getException().getMessage() : "Erro desconhecido."),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}


