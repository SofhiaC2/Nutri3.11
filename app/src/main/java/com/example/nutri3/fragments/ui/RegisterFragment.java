package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    private EditText etNome, etDataNasc, etEmail, etSenha, etConfirmSenha;
    private Button btnInscrever;
    private ImageView btnVoltar;
    private TextView tvToolbarTitle, tvSenhaLabel, tvConfirmSenhaLabel;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private NavController navController;
    private boolean isEditMode = false;
    private String currentDataNasc = "";
    private final Calendar cal = Calendar.getInstance();

    public RegisterFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference("usuarios");

        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        navController = NavHostFragment.findNavController(RegisterFragment.this);
        setupListeners();

        if (!isEditMode) {
            setupRegisterModeUI();
        }
    }

    private void initializeViews(View view) {
        etNome = view.findViewById(R.id.etNome);
        etDataNasc = view.findViewById(R.id.etDataNasc);
        etEmail = view.findViewById(R.id.etEmail);
        etSenha = view.findViewById(R.id.etSenha);
        etConfirmSenha = view.findViewById(R.id.etConfirmSenha);
        btnInscrever = view.findViewById(R.id.btnInscrever);
        btnVoltar = view.findViewById(R.id.btnVoltar);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle2);
        tvSenhaLabel = view.findViewById(R.id.tvSenhaLabel);
        tvConfirmSenhaLabel = view.findViewById(R.id.tvConfirmSenhaLabel);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (isEditMode) {
            mAuthListener = firebaseAuth -> {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    setupEditModeUI(user);
                } else if (isAdded()) {
                    Toast.makeText(getContext(), "Sessão expirada.", Toast.LENGTH_LONG).show();
                    navController.popBackStack();
                }
            };
            mAuth.addAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void setupRegisterModeUI() {
        tvToolbarTitle.setText("Inscreva-se");
        btnInscrever.setText("Inscrever-se");

        btnInscrever.setOnClickListener(v -> validarEProcessarRegistro());
    }

    private void setupEditModeUI(FirebaseUser user) {
        tvToolbarTitle.setText("Editar Perfil");
        btnInscrever.setText("Salvar Alterações");

        etSenha.setVisibility(View.GONE);
        tvSenhaLabel.setVisibility(View.GONE);

        etConfirmSenha.setVisibility(View.GONE);
        tvConfirmSenhaLabel.setVisibility(View.GONE);

        etEmail.setEnabled(false);
        etEmail.setAlpha(0.7f);

        loadUserData(user.getUid());

        btnInscrever.setOnClickListener(v -> atualizarUsuario());
    }

    private void setupListeners() {
        etDataNasc.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private boolean deletingSlash;
            private int slashStart;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                if (count == 1 && s.charAt(start) == '/') {
                    deletingSlash = true;
                    slashStart = start;
                } else deletingSlash = false;
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                if (deletingSlash && slashStart > 0 && slashStart < s.length()) {
                    if (Character.isDigit(s.charAt(slashStart - 1))) {
                        s.delete(slashStart - 1, slashStart);
                    }
                }

                String raw = s.toString().replaceAll("[^\\d]", "");
                StringBuilder sb = new StringBuilder();

                try {
                    if (raw.length() >= 1) sb.append(raw.substring(0, Math.min(2, raw.length())));
                    if (raw.length() >= 3) sb.append("/").append(raw.substring(2, Math.min(4, raw.length())));
                    if (raw.length() >= 5) sb.append("/").append(raw.substring(4, Math.min(8, raw.length())));
                } catch (Exception ignored) {}

                currentDataNasc = sb.toString();
                s.replace(0, s.length(), currentDataNasc);
                isFormatting = false;
            }
        });

        btnVoltar.setOnClickListener(v -> navController.navigateUp());
    }

    private void loadUserData(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    etNome.setText(snapshot.child("nomeCompleto").getValue(String.class));
                    etDataNasc.setText(snapshot.child("dataNascimento").getValue(String.class));
                    etEmail.setText(snapshot.child("email").getValue(String.class));
                } else {
                    Toast.makeText(getContext(), "Não foi possível carregar perfil.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUsuario() {
        String nome = etNome.getText().toString();
        String data = etDataNasc.getText().toString();

        if (nome.isEmpty() || !data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Preencha corretamente.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> map = new HashMap<>();
        map.put("nomeCompleto", nome);
        map.put("dataNascimento", data);

        mDatabase.child(user.getUid()).updateChildren(map)
                .addOnCompleteListener(t -> {
                    if (t.isSuccessful()) {
                        Toast.makeText(getContext(), "Atualizado!", Toast.LENGTH_SHORT).show();
                        navController.popBackStack();
                    }
                });
    }

    private void validarEProcessarRegistro() {
        String nome = etNome.getText().toString();
        String data = etDataNasc.getText().toString();
        String email = etEmail.getText().toString();
        String senha = etSenha.getText().toString();
        String confirmar = etConfirmSenha.getText().toString();

        if (nome.isEmpty() || data.isEmpty() || email.isEmpty() ||
                senha.isEmpty() || confirmar.isEmpty()) {
            Toast.makeText(getContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!data.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Data inválida.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "E-mail inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmar)) {
            Toast.makeText(getContext(), "Senhas não coincidem.", Toast.LENGTH_SHORT).show();
            return;
        }

        registrarUsuarioComEmailESenha(email, senha, nome, data);
    }

    private void registrarUsuarioComEmailESenha(String email, String senha, String nome, String data) {
        btnInscrever.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        btnInscrever.setEnabled(true);
                        Toast.makeText(getContext(),
                                "Falha: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }

                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user == null) return;

                    String uid = user.getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("nomeCompleto", nome);
                    map.put("email", email);
                    map.put("dataNascimento", data);

                    mDatabase.child(uid).setValue(map)
                            .addOnCompleteListener(t2 -> {
                                btnInscrever.setEnabled(true);
                                if (t2.isSuccessful()) {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Cadastro concluído", Toast.LENGTH_SHORT).show();
                                    }

                                    navController.navigateUp();
                                }
                            });
                });
    }
}
