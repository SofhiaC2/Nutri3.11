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

    // --- Componentes da UI ---
    private EditText etNome;
    private EditText etDataNasc;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmSenha;
    private Button btnInscrever;
    private ImageView btnVoltar;
    private TextView tvToolbarTitle;
    private TextView tvSenhaLabel; // Adicionado para modo de edição
    private TextView tvConfirmSenhaLabel; // Adicionado para modo de edição

    // --- Firebase e Navegação ---
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private NavController navController;

    // --- Controle de Modo ---
    private boolean isEditMode = false;

    // Variáveis para o TextWatcher da data
    private String currentDataNasc = "";
    private final String ddmmyyyy = "DDMMYYYY";
    private final Calendar cal = Calendar.getInstance();

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        // Ajuste o nome do seu nó principal se for diferente de "usuarios"
        mDatabase = FirebaseDatabase.getInstance("https://nutri-c79a2-default-rtdb.firebaseio.com").getReference("usuarios");

        // --- LÓGICA DE MODO (CADASTRO vs EDIÇÃO) ---
        if (getArguments() != null) {
            isEditMode = getArguments().getBoolean("isEditMode", false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- INICIALIZAÇÃO DOS COMPONENTES ---
        initializeViews(view);
        navController = NavHostFragment.findNavController(RegisterFragment.this);

        // --- CONFIGURAÇÃO DA UI COM BASE NO MODO ---
        if (isEditMode) {
            setupEditMode();
        } else {
            setupRegisterMode();
        }

        // --- LISTENERS ---
        setupListeners();
    }

    private void initializeViews(View view) {
        etNome = view.findViewById(R.id.etNome);
        etDataNasc = view.findViewById(R.id.etDataNasc);
        etEmail = view.findViewById(R.id.etEmail);
        etSenha = view.findViewById(R.id.etSenha);
        etConfirmSenha = view.findViewById(R.id.etConfirmSenha);
        btnInscrever = view.findViewById(R.id.btnInscrever);
        btnVoltar = view.findViewById(R.id.btnVoltar);
        tvToolbarTitle = view.findViewById(R.id.tvToolbarTitle2); // Nome do ID no XML
        tvSenhaLabel = view.findViewById(R.id.tvSenhaLabel); // Novo ID do XML
        tvConfirmSenhaLabel = view.findViewById(R.id.tvConfirmSenhaLabel); // Novo ID do XML
    }

    private void setupRegisterMode() {
        tvToolbarTitle.setText("Inscreva-se");
        btnInscrever.setText("Inscrever-se");
        btnInscrever.setOnClickListener(v -> validarEProcessarRegistro());
    }

    private void setupEditMode() {
        tvToolbarTitle.setText("Editar Perfil");
        btnInscrever.setText("Salvar Alterações");

        // Ocultar campos e rótulos de senha
        etSenha.setVisibility(View.GONE);
        tvSenhaLabel.setVisibility(View.GONE);
        etConfirmSenha.setVisibility(View.GONE);
        tvConfirmSenhaLabel.setVisibility(View.GONE);

        // Desabilitar edição de e-mail
        etEmail.setEnabled(false);
        etEmail.setFocusable(false);
        etEmail.setAlpha(0.7f); // Deixa o campo visualmente desabilitado

        // Carregar dados do usuário
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
        }

        btnInscrever.setOnClickListener(v -> atualizarUsuario());
    }

    private void setupListeners() {
        etDataNasc.addTextChangedListener(new TextWatcher() {
            // ... seu código de TextWatcher existente (sem alterações) ...
            private boolean isFormatting;
            private boolean deletingHyphen;
            private int hyphenStart;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                if (count == 1 && (s.charAt(start) == '/' )) {
                    deletingHyphen = true;
                    hyphenStart = start;
                } else {
                    deletingHyphen = false;
                }
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;
                if (deletingHyphen && hyphenStart > 0 && hyphenStart < s.length()) {
                    if (Character.isDigit(s.charAt(hyphenStart -1))) {
                        s.delete(hyphenStart - 1, hyphenStart);
                    }
                }
                deletingHyphen = false;
                String originalText = s.toString();
                String cleanText = originalText.replaceAll("[^\\d]", "");
                StringBuilder formattedText = new StringBuilder();
                int cleanLength = cleanText.length();
                int cursorPos = etDataNasc.getSelectionStart();
                try {
                    if (cleanLength >= 1) {
                        formattedText.append(cleanText.substring(0, Math.min(2, cleanLength)));
                    }
                    if (cleanLength >= 3) {
                        formattedText.append("/");
                        formattedText.append(cleanText.substring(2, Math.min(4, cleanLength)));
                    }
                    if (cleanLength >= 5) {
                        formattedText.append("/");
                        formattedText.append(cleanText.substring(4, Math.min(8, cleanLength)));
                    }
                    if (cleanLength == 8) {
                        int day = Integer.parseInt(cleanText.substring(0, 2));
                        int month = Integer.parseInt(cleanText.substring(2, 4));
                        int year = Integer.parseInt(cleanText.substring(4, 8));
                        if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR) + 10) {
                        }
                    }
                } catch (NumberFormatException e) {
                    Log.e(TAG, "NumberFormatException in date formatter", e);
                }
                currentDataNasc = formattedText.toString();
                s.replace(0, s.length(), currentDataNasc);
                if (currentDataNasc.length() > originalText.length() && cursorPos == originalText.length()) {
                    etDataNasc.setSelection(currentDataNasc.length());
                } else if (cursorPos <= currentDataNasc.length()) {
                    etDataNasc.setSelection(Math.min(cursorPos, currentDataNasc.length()));
                } else {
                    etDataNasc.setSelection(currentDataNasc.length());
                }
                isFormatting = false;
            }
        });

        btnVoltar.setOnClickListener(v -> {
            if (isAdded()) {
                navController.navigateUp();
            }
        });
    }

    private void loadUserData(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && isAdded()) {
                    // Use os nomes das chaves que você salvou no Firebase
                    String nome = snapshot.child("nomeCompleto").getValue(String.class);
                    String dataNasc = snapshot.child("dataNascimento").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    etNome.setText(nome);
                    etDataNasc.setText(dataNasc);
                    etEmail.setText(email);
                } else if(isAdded()) {
                    Toast.makeText(getContext(), "Não foi possível carregar os dados do perfil.", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(isAdded()) Toast.makeText(getContext(), "Falha ao carregar dados: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void atualizarUsuario() {
        String nome = etNome.getText().toString().trim();
        String dataNascimento = etDataNasc.getText().toString().trim();

        if (TextUtils.isEmpty(nome) || !dataNascimento.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(getContext(), "Verifique se o nome e a data (DD/MM/AAAA) estão preenchidos corretamente.", Toast.LENGTH_LONG).show();
            return;
        }

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            btnInscrever.setEnabled(false);
            String userId = currentUser.getUid();
            Map<String, Object> updates = new HashMap<>();
            // Use as mesmas chaves do banco de dados
            updates.put("nomeCompleto", nome);
            updates.put("dataNascimento", dataNascimento);

            mDatabase.child(userId).updateChildren(updates)
                    .addOnCompleteListener(task -> {
                        btnInscrever.setEnabled(true);
                        if (task.isSuccessful() && isAdded()) {
                            Toast.makeText(getContext(), "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                            navController.popBackStack(); // Volta para a tela de configurações
                        } else if(isAdded()) {
                            String errorMessage = task.getException() != null ? task.getException().getMessage() : "Erro desconhecido.";
                            Toast.makeText(getContext(), "Falha ao atualizar perfil: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


    private void validarEProcessarRegistro() {
        String nome = etNome.getText().toString().trim();
        String dataNascimentoInput = etDataNasc.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String senha = etSenha.getText().toString().trim();
        String confirmarSenha = etConfirmSenha.getText().toString().trim();

        etNome.setError(null);
        etDataNasc.setError(null);
        etEmail.setError(null);
        etSenha.setError(null);
        etConfirmSenha.setError(null);

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(nome)) {
            etNome.setError(getString(R.string.erro_campo_obrigatorio));
            focusView = etNome;
            cancel = true;
        }

        if (TextUtils.isEmpty(dataNascimentoInput)) {
            etDataNasc.setError(getString(R.string.erro_campo_obrigatorio));
            if (focusView == null) focusView = etDataNasc;
            cancel = true;
        } else if (!dataNascimentoInput.matches("\\d{2}/\\d{2}/\\d{4}")) {
            etDataNasc.setError("Formato de data inválido (DD/MM/AAAA)");
            if (focusView == null) focusView = etDataNasc;
            cancel = true;
        } else {
            try {
                String[] parts = dataNascimentoInput.split("/");
                int day = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                int year = Integer.parseInt(parts[2]);
                if (month < 1 || month > 12) {
                    etDataNasc.setError("Mês inválido");
                    if (focusView == null) focusView = etDataNasc;
                    cancel = true;
                } else {
                    cal.set(Calendar.YEAR, year);
                    cal.set(Calendar.MONTH, month - 1);
                    int maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                    if (day < 1 || day > maxDay) {
                        etDataNasc.setError("Dia inválido para o mês/ano");
                        if (focusView == null) focusView = etDataNasc;
                        cancel = true;
                    }
                }
                if (year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR)) {
                    etDataNasc.setError("Ano inválido");
                    if (focusView == null) focusView = etDataNasc;
                    cancel = true;
                }
            } catch (NumberFormatException e) {
                etDataNasc.setError("Data contém caracteres inválidos");
                if (focusView == null) focusView = etDataNasc;
                cancel = true;
            }
        }

        // A lógica de validação de email, senha, etc., permanece a mesma
        if (TextUtils.isEmpty(email)) {
            etEmail.setError(getString(R.string.erro_campo_obrigatorio));
            if (focusView == null) focusView = etEmail;
            cancel = true;
        } else if (!isValidEmail(email)) {
            etEmail.setError(getString(R.string.erro_email_invalido));
            if (focusView == null) focusView = etEmail;
            cancel = true;
        }

        if (TextUtils.isEmpty(senha)) {
            etSenha.setError(getString(R.string.erro_campo_obrigatorio));
            if (focusView == null) focusView = etSenha;
            cancel = true;
        } else if (senha.length() < 6) {
            etSenha.setError(getString(R.string.erro_senha_curta));
            if (focusView == null) focusView = etSenha;
            cancel = true;
        }

        if (TextUtils.isEmpty(confirmarSenha)) {
            etConfirmSenha.setError(getString(R.string.erro_campo_obrigatorio));
            if (focusView == null) focusView = etConfirmSenha;
            cancel = true;
        } else if (!senha.equals(confirmarSenha)) {
            etConfirmSenha.setError(getString(R.string.erro_senhas_nao_coincidem));
            if (focusView == null) focusView = etConfirmSenha;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            registrarUsuarioComEmailESenha(email, senha, nome, dataNascimentoInput);
        }
    }

    private boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void registrarUsuarioComEmailESenha(String email, String senha, String nomeCompleto, String dataNascimento) {
        btnInscrever.setEnabled(false);
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(requireActivity(), authTask -> {
                    if (authTask.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("nomeCompleto", nomeCompleto);
                            userData.put("email", email);
                            userData.put("dataNascimento", dataNascimento);

                            mDatabase.child(uid).setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        btnInscrever.setEnabled(true);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Registro e perfil salvos com sucesso!", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "Perfil do usuário salvo no Realtime Database para UID: " + uid);
                                            if (isAdded()) {
                                                navController.navigate(R.id.action_registerFragment_to_loginFragment);
                                            }
                                        } else {
                                            String dbErrorMessage = dbTask.getException() != null ? dbTask.getException().getMessage() : "Erro desconhecido ao salvar perfil.";
                                            Log.w(TAG, "Falha ao salvar perfil do usuário: " + dbErrorMessage, dbTask.getException());
                                            Toast.makeText(getContext(), "Registro parcial: Erro ao salvar perfil. " + dbErrorMessage, Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else {
                            btnInscrever.setEnabled(true);
                            Toast.makeText(getContext(), "Erro: Usuário autenticado é nulo após registro.", Toast.LENGTH_LONG).show();
                            Log.e(TAG, "FirebaseUser is null after successful authentication.");
                        }
                    } else {
                        btnInscrever.setEnabled(true);
                        String errorMessage = authTask.getException() != null ? authTask.getException().getMessage() : "Erro desconhecido.";
                        Log.w(TAG, "createUserWithEmail:failure", authTask.getException());
                        Toast.makeText(getContext(), "Falha no registro: " + errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }
}
