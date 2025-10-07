package com.example.nutri3.fragments.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.text.Editable; // Import Editable
import android.text.TextWatcher; // Import TextWatcher
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.nutri3.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar; // Import Calendar
import java.util.HashMap;
import java.util.Map;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";

    private EditText etNome;
    private EditText etDataNasc;
    private EditText etEmail;
    private EditText etSenha;
    private EditText etConfirmSenha;
    private Button btnInscrever;
    private ImageView btnVoltar;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    // Variáveis para o TextWatcher da data
    private String currentDataNasc = "";
    private final String ddmmyyyy = "DDMMYYYY"; // Tornar final
    private final Calendar cal = Calendar.getInstance(); // Tornar final

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "Usuário já logado: " + currentUser.getUid());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://nutri-c79a2-default-rtdb.firebaseio.com").getReference();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etNome = view.findViewById(R.id.etNome);
        etDataNasc = view.findViewById(R.id.etDataNasc);
        etEmail = view.findViewById(R.id.etEmail);
        etSenha = view.findViewById(R.id.etSenha);
        etConfirmSenha = view.findViewById(R.id.etConfirmSenha);
        btnInscrever = view.findViewById(R.id.btnInscrever);
        btnVoltar = view.findViewById(R.id.btnVoltar);

        // Adiciona o TextWatcher ao EditText da Data de Nascimento
        etDataNasc.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting; // Flag para evitar recursão
            private boolean deletingHyphen;
            private int hyphenStart;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (isFormatting) return;
                // Lógica para lidar com a exclusão de hífens/barras
                if (count == 1 && (s.charAt(start) == '/' )) {
                    deletingHyphen = true;
                    hyphenStart = start;
                } else {
                    deletingHyphen = false;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não é necessário implementar aqui na maioria dos casos com afterTextChanged
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;

                isFormatting = true;

                if (deletingHyphen && hyphenStart > 0 && hyphenStart < s.length()) {
                    // Se um hífen foi deletado, deleta também o número anterior a ele
                    // para um comportamento mais natural de backspace.
                    // Isso é opcional e pode precisar de ajuste fino.
                    if (Character.isDigit(s.charAt(hyphenStart -1))) {
                        s.delete(hyphenStart - 1, hyphenStart);
                    }
                }
                deletingHyphen = false; // Reset flag

                String originalText = s.toString();
                String cleanText = originalText.replaceAll("[^\\d]", ""); // Mantém apenas dígitos

                StringBuilder formattedText = new StringBuilder();
                int cleanLength = cleanText.length();
                int cursorPos = etDataNasc.getSelectionStart(); // Salva a posição original do cursor

                try {
                    if (cleanLength >= 1) { // Dia
                        formattedText.append(cleanText.substring(0, Math.min(2, cleanLength)));
                    }
                    if (cleanLength >= 3) { // Mês
                        formattedText.append("/");
                        formattedText.append(cleanText.substring(2, Math.min(4, cleanLength)));
                    }
                    if (cleanLength >= 5) { // Ano
                        formattedText.append("/");
                        formattedText.append(cleanText.substring(4, Math.min(8, cleanLength)));
                    }

                    // Validação de dia, mês, ano (simples, pode ser melhorada)
                    if (cleanLength == 8) {
                        int day = Integer.parseInt(cleanText.substring(0, 2));
                        int month = Integer.parseInt(cleanText.substring(2, 4));
                        int year = Integer.parseInt(cleanText.substring(4, 8));

                        if (month < 1 || month > 12 || day < 1 || day > 31 || year < 1900 || year > Calendar.getInstance().get(Calendar.YEAR) + 10) {
                            // Data inválida, mas não limpa automaticamente, apenas não formata com barras além do necessário
                            // Você pode adicionar um setError aqui se quiser feedback imediato de data inválida
                        }
                    }

                } catch (NumberFormatException e) {
                    Log.e(TAG, "NumberFormatException in date formatter", e);
                }

                currentDataNasc = formattedText.toString();
                s.replace(0, s.length(), currentDataNasc);

                // Lógica para restaurar a posição do cursor
                // Isso é complexo e pode precisar de ajustes finos
                // Se o texto ficou mais longo (barra adicionada), e o cursor estava no final, move para o novo final
                if (currentDataNasc.length() > originalText.length() && cursorPos == originalText.length()) {
                    etDataNasc.setSelection(currentDataNasc.length());
                } else if (cursorPos <= currentDataNasc.length()) {
                    // Tenta manter a posição relativa, mas não é perfeito.
                    // Para uma melhor experiência do cursor, bibliotecas de máscara são geralmente superiores.
                    etDataNasc.setSelection(Math.min(cursorPos, currentDataNasc.length()));
                } else {
                    etDataNasc.setSelection(currentDataNasc.length());
                }


                isFormatting = false;
            }
        });


        btnInscrever.setOnClickListener(v -> validarEProcessarRegistro());
        btnVoltar.setOnClickListener(v -> {
            if (isAdded() && getActivity() != null) {
                NavHostFragment.findNavController(RegisterFragment.this).navigateUp();
            }
        });
    }

    private void validarEProcessarRegistro() {
        String nome = etNome.getText().toString().trim();
        // Usar currentDataNasc que já está (ou deveria estar) formatado ou limpo de caracteres não numéricos.
        // Ou pegar de etDataNasc.getText() e validar o formato dd/MM/yyyy.
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

        // Validação da Data de Nascimento (formato dd/MM/yyyy)
        if (TextUtils.isEmpty(dataNascimentoInput)) {
            etDataNasc.setError(getString(R.string.erro_campo_obrigatorio));
            if (focusView == null) focusView = etDataNasc;
            cancel = true;
        } else if (!dataNascimentoInput.matches("\\d{2}/\\d{2}/\\d{4}")) { // Verifica o formato DD/MM/YYYY
            etDataNasc.setError("Formato de data inválido (DD/MM/AAAA)");
            if (focusView == null) focusView = etDataNasc;
            cancel = true;
        } else {
            // Validação lógica da data (ex: se é uma data válida no calendário)
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
                    cal.set(Calendar.MONTH, month - 1); // Calendar.MONTH é 0-indexed
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

                            mDatabase.child("usuarios").child(uid).setValue(userData)
                                    .addOnCompleteListener(dbTask -> {
                                        btnInscrever.setEnabled(true);
                                        if (dbTask.isSuccessful()) {
                                            Toast.makeText(getContext(), "Registro e perfil salvos com sucesso!", Toast.LENGTH_LONG).show();
                                            Log.d(TAG, "Perfil do usuário salvo no Realtime Database para UID: " + uid);
                                            if (isAdded()) {
                                                NavHostFragment.findNavController(RegisterFragment.this)
                                                        .navigate(R.id.action_registerFragment_to_loginFragment);
                                            }
                                        } else {
                                            String dbErrorMessage = dbTask.getException() != null ? dbTask.getException().getMessage() : "Erro desconhecido ao salvar perfil.";
                                            Log.w(TAG, "Falha ao salvar perfil do usuário: " + dbErrorMessage, dbTask.getException());
                                            Toast.makeText(getContext(), "Registro parcial: Erro ao salvar perfil. " + dbErrorMessage, Toast.LENGTH_LONG).show();
                                            // Opcional: Desfazer criação do usuário no Auth
                                            // user.delete().addOnCompleteListener(deleteTask -> Log.d(TAG, "Auth user deleted: " + deleteTask.isSuccessful()));
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
