package com.example.nutri3.fragments.menu;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentHomeBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    // Para o diálogo de agendamento
    private final Calendar calendar = Calendar.getInstance();
    private ValueEventListener proximaConsultaListener;
    private Query proximaConsultaQuery;
    private int limiteConsultas = 3;
    private final java.util.List<DataSnapshot> listaConsultas = new java.util.ArrayList<>();


    public HomeFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        if (currentUser != null) {
            loadUserName(currentUser.getUid());
            // Anexa o listener para carregar a consulta
            carregarProximaConsulta(currentUser.getUid());
        } else {
            Log.w(TAG, "Usuário não autenticado.");
            binding.tvGreeting.setText("Olá!");
            binding.tvSemConsultas.setText("Faça login para ver seus agendamentos.");
            binding.tvSemConsultas.setVisibility(View.VISIBLE);
        }

        // Ação correta: Botão de agendamento rápido abre o diálogo.
        // O ID do botão no XML agora é 'btnAgendarConsulta'.
        binding.btnAgendarConsulta.setOnClickListener(v -> exibirDialogoAgendamento());
    }


    // Em HomeFragment.java

    private void exibirDialogoAgendamento() {
        if (getContext() == null || currentUser == null) return;

        // Infla o layout do diálogo (você precisa criar dialog_agendar_consulta.xml)
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agendar_consulta, null);
        final EditText etNome = dialogView.findViewById(R.id.etNomePacienteConsulta);
        final EditText etTelefone = dialogView.findViewById(R.id.etTelefonePacienteConsulta);
        final EditText etData = dialogView.findViewById(R.id.etDataConsulta);
        final EditText etHora = dialogView.findViewById(R.id.etHoraConsulta);

        // MÁSCARA AUTOMÁTICA PARA DATA (DD/MM/AAAA)
        etData.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating;
            private String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().replaceAll("[^\\d]", "");
                String fmt = old.replaceAll("[^\\d]", "");
                if (isUpdating || str.equals(fmt)) {
                    isUpdating = false;
                    return;
                }

                String formatted = "";
                if (str.length() > 0) {
                    if (str.length() <= 2) {
                        formatted = str;
                    } else if (str.length() <= 4) {
                        formatted = str.substring(0, 2) + "/" + str.substring(2);
                    } else {
                        formatted = str.substring(0, 2) + "/" + str.substring(2, 4) + "/" + str.substring(4, Math.min(str.length(), 8));
                    }
                }

                isUpdating = true;
                old = formatted;
                etData.setText(formatted);
                etData.setSelection(formatted.length());
            }
        });

        // MÁSCARA AUTOMÁTICA PARA HORA (HH:MM)
        etHora.addTextChangedListener(new TextWatcher() {
            private boolean isUpdating;
            private String old = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().replaceAll("[^\\d]", "");
                if (isUpdating || str.equals(old) || str.length() > 4) {
                    isUpdating = false;
                    return;
                }

                String formatted = str;
                if (str.length() >= 3) {
                    formatted = str.substring(0, 2) + ":" + str.substring(2);
                }

                isUpdating = true;
                old = str;
                etHora.setText(formatted);
                etHora.setSelection(formatted.length());
            }
        });

        // Constrói e exibe o diálogo
        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                // ================== BOTÃO ADICIONADO DE VOLTA ==================
                .setPositiveButton("Agendar", (dialog, which) -> {
                    String nome = etNome.getText().toString().trim();
                    String dataStr = etData.getText().toString().trim();
                    String horaStr = etHora.getText().toString().trim();

                    // Validação dos campos para os formatos esperados
                    if (TextUtils.isEmpty(nome) || !dataStr.matches("\\d{2}/\\d{2}/\\d{4}") || !horaStr.matches("\\d{2}:\\d{2}")) {
                        Toast.makeText(getContext(), "Preencha todos os campos corretamente (Nome, Data e Hora).", Toast.LENGTH_LONG).show();
                        return;
                    }

                    // Tenta converter a data e hora para um timestamp
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        sdf.setLenient(false); // Impede datas inválidas como "32/02/2025"

                        long timestamp = sdf.parse(dataStr + " " + horaStr).getTime();

                        // Se a conversão deu certo, salva no Firebase
                        salvarConsultaNoFirebase(nome, etTelefone.getText().toString().trim(), timestamp);

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Data ou hora inválida.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao fazer parse da data/hora digitada", e);
                    }
                })
                // ===============================================================
                .setNegativeButton("Cancelar", null)
                .show();
    }


    private void salvarConsultaNoFirebase(String nome, String telefone, long timestamp) {
        String uid = currentUser.getUid();
        String consultaId = mDatabase.child("agendamentos").child(uid).push().getKey();
        if (consultaId == null) {
            Toast.makeText(getContext(), "Erro ao criar agendamento.", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> dadosConsulta = new HashMap<>();
        dadosConsulta.put("nomePaciente", nome);
        dadosConsulta.put("telefonePaciente", telefone);
        dadosConsulta.put("timestamp", timestamp);

        mDatabase.child("agendamentos").child(uid).child(consultaId).setValue(dadosConsulta)
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Consulta agendada com sucesso!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Falha ao agendar consulta.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void carregarProximaConsulta(String userId) {

        binding.progressBarConsultas.setVisibility(View.VISIBLE);
        binding.tvProximaConsultaInfo.setVisibility(View.GONE);
        binding.tvSemConsultas.setVisibility(View.GONE);
        binding.btnVerMaisConsultas.setVisibility(View.GONE);

        DatabaseReference agendamentosRef = mDatabase.child("agendamentos").child(userId);
        long agora = System.currentTimeMillis();

        proximaConsultaQuery = agendamentosRef.orderByChild("timestamp").startAt(agora);

        proximaConsultaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!isAdded()) return;

                binding.progressBarConsultas.setVisibility(View.GONE);

                listaConsultas.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    listaConsultas.add(snap);
                }

                if (listaConsultas.isEmpty()) {
                    binding.tvSemConsultas.setVisibility(View.VISIBLE);
                    binding.tvProximaConsultaInfo.setVisibility(View.GONE);
                    return;
                }

                mostrarConsultasNaTela();

                if (listaConsultas.size() > limiteConsultas) {
                    binding.btnVerMaisConsultas.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (!isAdded()) return;
                binding.progressBarConsultas.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Erro ao carregar consultas.", Toast.LENGTH_SHORT).show();
            }
        };

        proximaConsultaQuery.addValueEventListener(proximaConsultaListener);

        binding.btnVerMaisConsultas.setOnClickListener(v -> {

            limiteConsultas += 3;

            mostrarConsultasNaTela();

            if (limiteConsultas >= listaConsultas.size()) {
                binding.btnVerMaisConsultas.setVisibility(View.GONE);
            }
        });
    }


    private void loadUserName(String userId) {
        DatabaseReference userRef = mDatabase.child("usuarios").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded() && dataSnapshot.exists()) {
                    String nomeCompleto = dataSnapshot.child("nomeCompleto").getValue(String.class);
                    if (nomeCompleto != null && !nomeCompleto.isEmpty()) {
                        String primeiroNome = nomeCompleto.split(" ")[0];
                        binding.tvGreeting.setText("Olá,\n" + primeiroNome);
                    } else {
                        binding.tvGreeting.setText("Olá!");
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded()) Log.e(TAG, "Falha ao carregar nome do usuário.", databaseError.toException());
            }
        });
    }
private void mostrarConsultasNaTela() {

    StringBuilder builder = new StringBuilder();

    int max = Math.min(limiteConsultas, listaConsultas.size());

    SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", Locale.getDefault());

    for (int i = 0; i < max; i++) {

        DataSnapshot snap = listaConsultas.get(i);

        String nome = snap.child("nomePaciente").getValue(String.class);
        Long timestamp = snap.child("timestamp").getValue(Long.class);

        if (nome != null && timestamp != null) {

            String dataF = sdfData.format(timestamp);
            String horaF = sdfHora.format(timestamp);

            builder.append("• ").append(nome)
                    .append("\n  Data: ").append(dataF)
                    .append("  Hora: ").append(horaF)
                    .append("\n\n");
        }
    }

    binding.tvProximaConsultaInfo.setText(builder.toString().trim());

    binding.tvProximaConsultaInfo.setVisibility(View.VISIBLE);
    binding.tvSemConsultas.setVisibility(View.GONE);
}


@Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove o listener para evitar memory leaks quando o fragmento for destruído
        if (proximaConsultaQuery != null && proximaConsultaListener != null) {
            proximaConsultaQuery.removeEventListener(proximaConsultaListener);
        }
        binding = null;
    }
}
