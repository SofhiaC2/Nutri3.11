package com.example.nutri3.fragments.menu;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView; // Importar TextView
import android.widget.Toast;   // Importar Toast para feedback

import com.example.nutri3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private TextView tvGreeting;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;

    // Remova os ARG_PARAM e newInstance se não estiver usando para passar dados
    // private static final String ARG_PARAM1 = "param1";
    // private static final String ARG_PARAM2 = "param2";
    // private String mParam1;
    // private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        // Certifique-se de usar a URL correta do seu Realtime Database se não for a padrão
        mDatabase = FirebaseDatabase.getInstance("https://nutri-c79a2-default-rtdb.firebaseio.com").getReference();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvGreeting = view.findViewById(R.id.tvGreeting);

        if (currentUser != null) {
            loadUserName(currentUser.getUid());
        } else {
            // Isso não deveria acontecer se o AuthStateListener estiver funcionando corretamente
            // e o usuário só chega aqui logado.
            Log.w(TAG, "Usuário atual é nulo no HomeFragment.");
            if (getContext() != null) {
                Toast.makeText(getContext(), "Erro: Usuário não identificado.", Toast.LENGTH_SHORT).show();
            }
            tvGreeting.setText("Olá!");
        }

        // Aqui você adicionará a lógica para a agenda
    }

    private void loadUserName(String userId) {
        DatabaseReference userRef = mDatabase.child("usuarios").child(userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isAdded() && dataSnapshot.exists()) { // Verifica se o fragmento está anexado e se os dados existem
                    // Supondo que você salvou o nome como "nomeCompleto" no Realtime Database
                    // como no seu RegisterFragment
                    String nomeCompleto = dataSnapshot.child("nomeCompleto").getValue(String.class);

                    if (nomeCompleto != null && !nomeCompleto.isEmpty()) {
                        String[] partesNome = nomeCompleto.split(" "); // Pega o primeiro nome
                        String primeiroNome = partesNome.length > 0 ? partesNome[0] : nomeCompleto;
                        tvGreeting.setText("Olá,\n" + primeiroNome);
                    } else {
                        // Se o nome não foi encontrado, use o email ou um placeholder
                        String email = currentUser.getEmail();
                        if (email != null && !email.isEmpty()) {
                            tvGreeting.setText("Olá,\n" + email.split("@")[0]); // Usa a parte antes do @
                        } else {
                            tvGreeting.setText("Olá!");
                        }
                        Log.w(TAG, "Nome completo não encontrado para o usuário: " + userId);
                    }
                } else if (isAdded()) {
                    Log.w(TAG, "Dados do usuário não encontrados para o UID: " + userId);
                    // Se o nome não foi encontrado, use o email ou um placeholder
                    String email = currentUser.getEmail();
                    if (email != null && !email.isEmpty()) {
                        tvGreeting.setText("Olá,\n" + email.split("@")[0]); // Usa a parte antes do @
                    } else {
                        tvGreeting.setText("Olá!");
                    }
                    if (getContext() != null) {
                        // Toast.makeText(getContext(), "Não foi possível carregar o nome do perfil.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isAdded()) { // Verifica se o fragmento está anexado
                    Log.e(TAG, "Falha ao carregar dados do usuário.", databaseError.toException());
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Erro ao carregar perfil.", Toast.LENGTH_SHORT).show();
                    }
                    String email = currentUser.getEmail();
                    if (email != null && !email.isEmpty()) {
                        tvGreeting.setText("Olá,\n" + email.split("@")[0]); // Usa a parte antes do @
                    } else {
                        tvGreeting.setText("Olá!");
                    }
                }
            }
        });
    }
}
