package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

// Imports necessários
import com.example.nutri3.R;
import com.example.nutri3.databinding.FragmentHomeBinding; // Import do ViewBinding
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    // MUDANÇA 1: Usar ViewBinding para acessar as views
    private FragmentHomeBinding binding;
    private NavController navController;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // MUDANÇA 2: Configurar o "ouvinte" para receber o paciente selecionado
        setupFragmentResultListener();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // MUDANÇA 3: Inflar o layout usando ViewBinding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        navController = NavHostFragment.findNavController(this);

        if (currentUser != null) {
            loadUserName(currentUser.getUid());
        } else {
            Log.w(TAG, "Usuário atual é nulo no HomeFragment.");
            binding.tvGreeting.setText("Olá!");
        }

        // MUDANÇA 4: Configurar o clique do botão "Nova Consulta"
        // (Assumindo que o botão no seu fragment_home.xml tem o ID 'btnNovaConsulta')
        binding.btnNovaConsulta.setOnClickListener(v -> {
            // Navega para a tela de seleção de paciente
            navController.navigate(R.id.action_menu_home_to_selecionarPaciente);
        });
    }

    private void setupFragmentResultListener() {
        // Este listener fica ativo e espera o SelecionarPacienteFragment enviar um resultado
        getParentFragmentManager().setFragmentResultListener("pacienteSelecionadoRequest", this, (requestKey, bundle) -> {
            // Este código é executado QUANDO o usuário seleciona um paciente e volta
            String pacienteId = bundle.getString("pacienteId");
            String pacienteNome = bundle.getString("pacienteNome");

            if (pacienteId != null && pacienteNome != null) {
                // Paciente recebido com sucesso, agora mostre o diálogo de opções
                showOpcoesConsultaDialog(pacienteId, pacienteNome);
            }
        });
    }

    // Dentro da classe HomeFragment.java

    private void showOpcoesConsultaDialog(String pacienteId, String pacienteNome) {
        // Infla o layout do diálogo que você criou
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialogo_nova_consulta, null);
        final CheckBox cbAvaliacao = dialogView.findViewById(R.id.cbAvaliacao);
        final CheckBox cbDieta = dialogView.findViewById(R.id.cbDieta);

        new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setTitle("Nova Consulta para " + pacienteNome) // Título dinâmico
                .setPositiveButton("Continuar", (dialog, which) -> {
                    boolean incluiAvaliacao = cbAvaliacao.isChecked();
                    boolean incluiDieta = cbDieta.isChecked();

                    if (!incluiAvaliacao && !incluiDieta) {
                        Toast.makeText(getContext(), "Selecione ao menos uma opção.", Toast.LENGTH_SHORT).show();
                        // Usamos um truque para não fechar o diálogo se a validação falhar,
                        // mas a forma mais simples é reabri-lo ou simplesmente deixar fechar.
                        // Por agora, o `return` impede a navegação.
                        return;
                    }

                    // =======================================================
                    // == NOVA LÓGICA DE NAVEGAÇÃO SEQUENCIAL
                    // =======================================================

                    if (incluiAvaliacao && incluiDieta) {
                        // CENÁRIO 1: Ambos marcados -> Vai para Avaliação, com instrução para seguir para Dieta
                        Log.d(TAG, "Navegando para Avaliação, depois para Dieta.");
                        Toast.makeText(getContext(), "Iniciando Avaliação Física...", Toast.LENGTH_SHORT).show();

                        // Supondo que você tem Safe Args configurado para a ação
                        HomeFragmentDirections.ActionMenuHomeToAvaliacaoFragment action =
                                HomeFragmentDirections.actionMenuHomeToAvaliacaoFragment(pacienteId);

                        // Passa o argumento extra para navegação sequencial
                        action.setNavegarParaDietaAposConcluir(true);

                        navController.navigate(action);

                    } else if (incluiAvaliacao) {
                        // CENÁRIO 2: Apenas Avaliação marcada -> Vai para Avaliação e para por aí
                        Log.d(TAG, "Navegando apenas para Avaliação.");
                        Toast.makeText(getContext(), "Iniciando Avaliação Física...", Toast.LENGTH_SHORT).show();

                        HomeFragmentDirections.ActionMenuHomeToAvaliacaoFragment action =
                                HomeFragmentDirections.actionMenuHomeToAvaliacaoFragment(pacienteId);

                        // O valor padrão de 'navegarParaDietaAposConcluir' é false, então não precisa setar
                        // action.setNavegarParaDietaAposConcluir(false); // Opcional, já é o padrão

                        navController.navigate(action);

                    } else { // incluiDieta é verdadeiro
                        // CENÁRIO 3: Apenas Dieta marcada -> Vai direto para Dieta
                        Log.d(TAG, "Navegando apenas para Dieta.");
                        Toast.makeText(getContext(), "Iniciando Plano Alimentar...", Toast.LENGTH_SHORT).show();

                        // Navega para a dieta passando o ID do paciente
                        HomeFragmentDirections.ActionMenuHomeToDietaFragment action =
                                HomeFragmentDirections.actionMenuHomeToDietaFragment(pacienteId);
                        navController.navigate(action);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
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
                } else if (isAdded()) {
                    Log.w(TAG, "Dados do usuário não encontrados para o UID: " + userId);
                    binding.tvGreeting.setText("Olá!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded()) {
                    Log.e(TAG, "Falha ao carregar dados do usuário.", databaseError.toException());
                    binding.tvGreeting.setText("Olá!");
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Limpa a referência ao binding para evitar memory leaks
    }
}
