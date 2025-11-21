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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;

public class Recover_accountFragment extends Fragment {

    private static final String TAG = "RecoverAccountFragment";

    private EditText etEmailRecover;
    private Button btnEnviar;
    private ImageView btnVoltar;

    private FirebaseAuth mAuth;



    public Recover_accountFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recover_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etEmailRecover = view.findViewById(R.id.etemailrecover);
        btnEnviar = view.findViewById(R.id.btnInscrever);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        btnEnviar.setOnClickListener(v -> sendPasswordResetEmail());

        btnVoltar.setOnClickListener(v -> {
            if (isAdded()) {
                NavHostFragment.findNavController(Recover_accountFragment.this).popBackStack();
            }
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmailRecover.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmailRecover.setError("E-mail é obrigatório.");
            etEmailRecover.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailRecover.setError("Por favor, insira um e-mail válido.");
            etEmailRecover.requestFocus();
            return;
        }

        btnEnviar.setEnabled(false);

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (!isAdded()) {
                        return;
                    }

                    btnEnviar.setEnabled(true);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "E-mail de redefinição de senha enviado.");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "E-mail de redefinição enviado para " + email, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                        String errorMessage = "Falha ao enviar e-mail.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            Log.e(TAG, "Firebase Error: " + task.getException().getMessage());
                            if (task.getException().getMessage().contains("ERROR_USER_NOT_FOUND")) {
                                errorMessage = "Nenhuma conta encontrada com este e-mail.";
                            } else if (task.getException().getMessage().contains("ERROR_INVALID_EMAIL")) {
                                errorMessage = "O formato do e-mail é inválido.";
                            } else {
                                errorMessage = task.getException().getLocalizedMessage();
                            }
                        }

                        if (getContext() != null) {
                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
