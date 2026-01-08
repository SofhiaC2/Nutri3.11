package com.example.nutri3;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DeleteAlimentosActivity extends AppCompatActivity {

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        DatabaseReference alimentosRef =
//                FirebaseDatabase.getInstance().getReference("alimentos");
//
//        alimentosRef.removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    Log.d("DELETE", "Todos os alimentos removidos com sucesso");
//                    finish(); // fecha a activity
//                })
//                .addOnFailureListener(e ->
//                        Log.e("DELETE", "Erro ao remover alimentos", e)
//                );
//    }
}
