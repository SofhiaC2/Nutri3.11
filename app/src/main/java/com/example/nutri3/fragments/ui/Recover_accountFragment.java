package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment; // Para navegação
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView; // Para o botão voltar
import android.widget.ProgressBar; // Se você decidir adicionar um
import android.widget.Toast;

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;

public class Recover_accountFragment extends Fragment { // Mantendo seu nome com "_"

    private static final String TAG = "RecoverAccountFragment"; // Tag para logs

    // Declaração dos componentes do layout
    private EditText etEmailRecover;
    private Button btnEnviar; // O ID no seu XML é btnInscrever, mas o texto é "Enviar"
    private ImageView btnVoltar;
    // private ProgressBar progressBarRecover; // Descomente se adicionar um ProgressBar ao XML

    private FirebaseAuth mAuth;



    public Recover_accountFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recover_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Inicializa os componentes da UI
        etEmailRecover = view.findViewById(R.id.etemailrecover); // ID do seu XML
        btnEnviar = view.findViewById(R.id.btnInscrever); // ID do seu XML (apesar do texto ser "Enviar")
        btnVoltar = view.findViewById(R.id.btnVoltar);
        // progressBarRecover = view.findViewById(R.id.your_progressbar_id); // Se você adicionar um

        // Configura o OnClickListener para o botão de Enviar
        btnEnviar.setOnClickListener(v -> sendPasswordResetEmail());

        // Configura o OnClickListener para o botão Voltar
        btnVoltar.setOnClickListener(v -> {
            if (isAdded()) {
                // Simplesmente volta para a tela anterior na pilha de navegação
                NavHostFragment.findNavController(Recover_accountFragment.this).popBackStack();
            }
        });
    }

    private void sendPasswordResetEmail() {
        String email = etEmailRecover.getText().toString().trim();

        // Validações básicas
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

        // if (progressBarRecover != null) progressBarRecover.setVisibility(View.VISIBLE);
        btnEnviar.setEnabled(false); // Desabilita o botão para evitar cliques múltiplos

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(requireActivity(), task -> { // Use requireActivity() para o contexto do listener
                    if (!isAdded()) {
                        // Fragmento não está mais anexado, não faça nada com a UI
                        return;
                    }

                    // if (progressBarRecover != null) progressBarRecover.setVisibility(View.GONE);
                    btnEnviar.setEnabled(true); // Reabilita o botão

                    if (task.isSuccessful()) {
                        Log.d(TAG, "E-mail de redefinição de senha enviado.");
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "E-mail de redefinição enviado para " + email, Toast.LENGTH_LONG).show();
                        }
                        // Opcional: Navegar de volta para o LoginFragment após o envio
                        // if (isAdded()) {
                        //     NavHostFragment.findNavController(Recover_accountFragment.this).popBackStack();
                        // }
                    } else {
                        Log.w(TAG, "sendPasswordResetEmail:failure", task.getException());
                        String errorMessage = "Falha ao enviar e-mail.";
                        if (task.getException() != null && task.getException().getMessage() != null) {
                            // Tente obter mensagens de erro mais específicas do Firebase
                            // Ex: "ERROR_USER_NOT_FOUND", "ERROR_INVALID_EMAIL"
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
