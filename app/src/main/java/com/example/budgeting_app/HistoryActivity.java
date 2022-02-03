package com.example.budgeting_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private RecyclerView recyclerView;

    private TodayItemsAdapter todayItemsAdapter2;
    private List<Data> myDataList;

    private FirebaseAuth mAuth;
    private String onlineUserId = "";
    private DatabaseReference expensesRef, personalRef;

    private Toolbar settingsToolbar;

    private Button search;
    private TextView historyTotalAmountSpent;

    private TextView displayError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        displayError = findViewById(R.id.displayError);

        settingsToolbar = findViewById(R.id.my_Feed_Toolbar);
        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("History");

        settingsToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistoryActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
            }
        });


        search = findViewById(R.id.search);
        historyTotalAmountSpent = findViewById(R.id.historyTotalAmountSpent);

        mAuth = FirebaseAuth.getInstance();
        onlineUserId = mAuth.getCurrentUser().getUid();

        recyclerView = findViewById(R.id.recycler_View_Id_Feed);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        myDataList = new ArrayList<>();
        todayItemsAdapter2 = new TodayItemsAdapter(HistoryActivity.this, myDataList);
        recyclerView.setAdapter(todayItemsAdapter2);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDatePickerDialog();
            }
        });
    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

        int months = month + 1;
        String padded = String.format("%02d", dayOfMonth);
        String date = padded + "-" + "0" + months + "-" + year;

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("expenses").child(onlineUserId);
        Query query = reference.orderByChild("date").equalTo(date);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myDataList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Data data = snapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                todayItemsAdapter2.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    historyTotalAmountSpent.setVisibility(View.GONE);
                    displayError.setText("No data was entered on this day\nKindly pick another day");
                    displayError.setVisibility(View.VISIBLE);
                } else {
                    displayError.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);

                    int totalAmount = 0;
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;
                        if (totalAmount > 0) {
                            historyTotalAmountSpent.setText("This day you spent Ksh." + totalAmount);
                            historyTotalAmountSpent.setVisibility(View.VISIBLE);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(HistoryActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
