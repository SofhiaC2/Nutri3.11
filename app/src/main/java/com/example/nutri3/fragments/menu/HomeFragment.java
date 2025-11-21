package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.nutri3.adapters.ConsultaAdapter;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeFragment extends Fragment implements ConsultaAdapter.OnConsultaListener {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private NavController navController;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    private ValueEventListener proximaConsultaListener;
    private Query proximaConsultaQuery;

    private ConsultaAdapter consultaAdapter;
    private final List<DataSnapshot> listaConsultas = new ArrayList<>();

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

        setupRecyclerView();

        if (currentUser != null) {
            loadUserName(currentUser.getUid());
            carregarProximaConsulta(currentUser.getUid());
        } else {
            Log.w(TAG, "Usuário não autenticado.");
            binding.tvGreeting.setText("Olá!");
            binding.tvSemConsultas.setText("Faça login para ver seus agendamentos.");
            binding.tvSemConsultas.setVisibility(View.VISIBLE);
        }

        binding.btnAgendarConsulta.setOnClickListener(v -> exibirDialogoAgendamento());
    }

    private void setupRecyclerView() {
        consultaAdapter = new ConsultaAdapter(getContext(), listaConsultas, this);
        binding.rvConsultas.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvConsultas.setAdapter(consultaAdapter);
    }

    private void carregarProximaConsulta(String userId) {
        binding.progressBarConsultas.setVisibility(View.VISIBLE);
        binding.rvConsultas.setVisibility(View.GONE);
        binding.tvSemConsultas.setVisibility(View.GONE);

        DatabaseReference agendamentosRef = mDatabase.child("agendamentos").child(userId);
        long agora = System.currentTimeMillis();
        proximaConsultaQuery = agendamentosRef.orderByChild("timestamp").startAt(agora);

        proximaConsultaListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || binding == null) return;

                binding.progressBarConsultas.setVisibility(View.GONE);
                listaConsultas.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    listaConsultas.add(snap);
                }

                // Notifica o adapter que os dados mudaram
                consultaAdapter.notifyDataSetChanged();

                if (listaConsultas.isEmpty()) {
                    binding.tvSemConsultas.setVisibility(View.VISIBLE);
                    binding.rvConsultas.setVisibility(View.GONE);
                } else {
                    binding.tvSemConsultas.setVisibility(View.GONE);
                    binding.rvConsultas.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && binding != null) {
                    binding.progressBarConsultas.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Erro ao carregar consultas.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Falha ao carregar consulta", error.toException());
                }
            }
        };

        proximaConsultaQuery.addValueEventListener(proximaConsultaListener);
    }

    @Override
    public void onConsultaDelete(String consultaId, String nomePaciente) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Excluir Agendamento")
                .setMessage("Tem certeza que deseja excluir a consulta de \"" + nomePaciente + "\"?")
                .setPositiveButton("Excluir", (dialog, which) -> {
                    mDatabase.child("agendamentos").child(currentUser.getUid()).child(consultaId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) Toast.makeText(getContext(), "Consulta excluída.", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) Toast.makeText(getContext(), "Falha ao excluir.", Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // O resto do seu código (exibirDialogoAgendamento, salvarConsultaNoFirebase, loadUserName, onDestroyView) continua aqui...
    // Vou colar eles abaixo para garantir.

    private void exibirDialogoAgendamento() {
        if (getContext() == null || currentUser == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agendar_consulta, null);
        final EditText etNome = dialogView.findViewById(R.id.etNomePacienteConsulta);
        final EditText etTelefone = dialogView.findViewById(R.id.etTelefonePacienteConsulta);
        final EditText etData = dialogView.findViewById(R.id.etDataConsulta);
        final EditText etHora = dialogView.findViewById(R.id.etHoraConsulta);

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
                String formatted;
                if (str.length() <= 2) {
                    formatted = str;
                } else if (str.length() <= 4) {
                    formatted = str.substring(0, 2) + "/" + str.substring(2);
                } else {
                    formatted = str.substring(0, 2) + "/" + str.substring(2, 4) + "/" + str.substring(4, Math.min(str.length(), 8));
                }
                isUpdating = true;
                old = formatted;
                etData.setText(formatted);
                etData.setSelection(formatted.length());
            }
        });

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

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setPositiveButton("Agendar", (dialog, which) -> {
                    String nome = etNome.getText().toString().trim();
                    String dataStr = etData.getText().toString().trim();
                    String horaStr = etHora.getText().toString().trim();

                    if (TextUtils.isEmpty(nome) || !dataStr.matches("\\d{2}/\\d{2}/\\d{4}") || !horaStr.matches("\\d{2}:\\d{2}")) {
                        Toast.makeText(getContext(), "Preencha todos os campos corretamente (Nome, Data e Hora).", Toast.LENGTH_LONG).show();
                        return;
                    }
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        sdf.setLenient(false);
                        long timestamp = sdf.parse(dataStr + " " + horaStr).getTime();
                        salvarConsultaNoFirebase(nome, etTelefone.getText().toString().trim(), timestamp);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Data ou hora inválida.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao fazer parse da data/hora digitada", e);
                    }
                })
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (proximaConsultaQuery != null && proximaConsultaListener != null) {
            proximaConsultaQuery.removeEventListener(proximaConsultaListener);
        }
        binding = null;
    }
}
