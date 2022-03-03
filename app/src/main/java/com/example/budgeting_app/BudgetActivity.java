package com.example.budgeting_app;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.MutableDateTime;
import org.joda.time.Weeks;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BudgetActivity extends AppCompatActivity {
    public static final String TAG = BudgetActivity.class.getSimpleName();


    @BindView(R.id.fab1)
    FloatingActionButton fab;
    @BindView(R.id.totalBudgetAmountTextView)
    TextView totalBudgetAmountTextView;

    private DatabaseReference budgetRef, personalRef;
    private FirebaseAuth mAuth;
    private ProgressDialog loader;
    private RecyclerView recyclerView;

    private String post_key = "";
    private String item = "";
    private int amount = 0;

    private Toolbar budgetToolbar;

    private TextView display;

    private List<Data> myDataList;

    private BudgetItemsAdapter budgetItemsAdapter;

    private ProgressBar progressBar;

    int dayTransRatio = 0;
    private int dayFoodRatio = 0;
    private int dayHouseRatio = 0;
    private int dayEntRatio = 0;
    private int dayEduRatio = 0;
    private int dayCharRatio = 0;
    private int dayAppRatio = 0;
    private int dayHealthRatio = 0;
    private int dayPerRatio = 0;
    private int dayOtherRatio = 0;

    int dayTransport = 0;
    int dayFood = 0;
    int dayHouse = 0;
    int dayEntertainment = 0;
    int dayEducation = 0;
    int dayCharity = 0;
    int dayApparel = 0;
    int dayHealth = 0;
    int dayPersonal = 0;
    int dayOther = 0;
    int dailyTotal = 0;

    int weekTransport = 0;
    int weekFood = 0;
    int weekHouse = 0;
    int weekEntertainment = 0;
    int weekEducation = 0;
    int weekCharity = 0;
    int weekApparel = 0;
    int weekHealth = 0;
    int weekPersonal = 0;
    int weekOther = 0;
    int weeklyTotal = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        ButterKnife.bind(this);

        display = findViewById(R.id.disp);
        progressBar = findViewById(R.id.progressBar2);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        myDataList = new ArrayList<>();
        budgetItemsAdapter = new BudgetItemsAdapter(BudgetActivity.this, myDataList);
        recyclerView.setAdapter(budgetItemsAdapter);


        budgetToolbar = findViewById(R.id.budgetToolbar);
        setSupportActionBar(budgetToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Budget");

        budgetToolbar.setNavigationOnClickListener(view -> {
            Intent intent = new Intent(BudgetActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, R.anim.stay);
        });

        mAuth = FirebaseAuth.getInstance();
        budgetRef = FirebaseDatabase.getInstance().getReference().child("budget").child(mAuth.getCurrentUser().getUid());
        personalRef = FirebaseDatabase.getInstance().getReference("personal").child(mAuth.getCurrentUser().getUid());
        loader = new ProgressDialog(this);

        readBudgetItems();


        fab.setOnClickListener(v -> addItems());

        getTransportBudgetRatios();
        getFoodBudgetRatios();
        getHouseBudgetRatios();
        getEntertainmentBudgetRatios();
        getEducationBudgetRatios();
        getCharityBudgetRatios();
        getApparelBudgetRatios();
        getHealthBudgetRatios();
        getPersonalBudgetRatios();
        getOtherBudgetRatios();
        getTotalBudgetRatios();
    }

    private void readBudgetItems() {
        budgetRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                myDataList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Data data = dataSnapshot.getValue(Data.class);
                    myDataList.add(data);
                }

                budgetItemsAdapter.notifyDataSetChanged();

                if (myDataList.isEmpty()) {
                    totalBudgetAmountTextView.setVisibility(View.GONE);
                    display.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } else {
                    progressBar.setVisibility(View.GONE);
                    display.setVisibility(View.GONE);

                    int totalAmount = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        int pTotal = Integer.parseInt(String.valueOf(total));
                        totalAmount += pTotal;

                        totalBudgetAmountTextView.setText("Total Budget: Ksh." + totalAmount);
                        totalBudgetAmountTextView.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                display.setVisibility(View.VISIBLE);
            }
        });
    }

    private void addItems() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View myView = inflater.inflate(R.layout.input_layout, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);
        dialog.show();

        final Spinner itemSpinner = myView.findViewById(R.id.itemsspinner);
        final EditText amount = myView.findViewById(R.id.amount);
        final Button cancel = myView.findViewById(R.id.cancel);
        final Button save = myView.findViewById(R.id.save);

        save.setOnClickListener(v -> {

            String budgetAmount = amount.getText().toString();
            String budgetItem = itemSpinner.getSelectedItem().toString();

            if (TextUtils.isEmpty(budgetAmount)) {
                amount.setError("Amount is Required");
                return;
            }

            if (budgetItem.equals("Select Item")) {
                Toast.makeText(BudgetActivity.this, "Select a valid item", Toast.LENGTH_SHORT).show();
                return;

            } else {
                loader.setMessage("Adding a budget item");
                loader.setCanceledOnTouchOutside(false);
                loader.show();

                String id = budgetRef.push().getKey();
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                Calendar cal = Calendar.getInstance();
                String date = dateFormat.format(cal.getTime());

                MutableDateTime epoch = new MutableDateTime();
                epoch.setDate(0);
                DateTime now = new DateTime();
                Weeks weeks = Weeks.weeksBetween(epoch, now);
                Months months = Months.monthsBetween(epoch, now);

                String itemNday = budgetItem + date;
                String itemNweek = budgetItem + weeks.getWeeks();
                String itemNmonth = budgetItem + months.getMonths();

                Data data = new Data(budgetItem, date, id, itemNday, itemNweek, itemNmonth, Integer.parseInt(budgetAmount), weeks.getWeeks(), months.getMonths(), null);
                budgetRef.child(id).setValue(data).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(BudgetActivity.this, "Budget Item added successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(BudgetActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }

                    loader.dismiss();
                });
            }
            dialog.dismiss();
        });

        cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void getTransportBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Transport");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayTransRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayTransRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayTransRatio = pTotal / 28;
                    }
                    int weekTransRatio = pTotal / 4;
                    int monthTransRatio = pTotal;

                    personalRef.child("dayTransRatio").setValue(dayTransRatio);
                    personalRef.child("weekTransRatio").setValue(weekTransRatio);
                    personalRef.child("monthTransRatio").setValue(monthTransRatio);

                } else {
                    personalRef.child("dayTransRatio").setValue(0);
                    personalRef.child("weekTransRatio").setValue(0);
                    personalRef.child("monthTransRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getFoodBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Food");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayFoodRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayFoodRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayFoodRatio = pTotal / 28;
                    }
                    int weekFoodRatio = pTotal / 4;
                    int monthFoodRatio = pTotal;

                    personalRef.child("dayFoodRatio").setValue(dayFoodRatio);
                    personalRef.child("weekFoodRatio").setValue(weekFoodRatio);
                    personalRef.child("monthFoodRatio").setValue(monthFoodRatio);

                } else {
                    personalRef.child("dayFoodRatio").setValue(0);
                    personalRef.child("weekFoodRatio").setValue(0);
                    personalRef.child("monthFoodRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHouseBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("House");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayHouseRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayHouseRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayHouseRatio = pTotal / 28;
                    }
                    int weekHouseRatio = pTotal / 4;
                    int monthHouseRatio = pTotal;

                    personalRef.child("dayHouseRatio").setValue(dayHouseRatio);
                    personalRef.child("weekHouseRatio").setValue(weekHouseRatio);
                    personalRef.child("monthHouseRatio").setValue(monthHouseRatio);

                } else {
                    personalRef.child("dayHouseRatio").setValue(0);
                    personalRef.child("weekHouseRatio").setValue(0);
                    personalRef.child("monthHouseRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getEntertainmentBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Entertainment");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayEntRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayEntRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayEntRatio = pTotal / 28;
                    }
                    int weekEntRatio = pTotal / 4;
                    int monthEntRatio = pTotal;

                    personalRef.child("dayEntRatio").setValue(dayEntRatio);
                    personalRef.child("weekEntRatio").setValue(weekEntRatio);
                    personalRef.child("monthEntRatio").setValue(monthEntRatio);

                } else {
                    personalRef.child("dayEntRatio").setValue(0);
                    personalRef.child("weekEntRatio").setValue(0);
                    personalRef.child("monthEntRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getEducationBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Education");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayEduRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayEduRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayEduRatio = pTotal / 28;
                    }
                    int weekEduRatio = pTotal / 4;
                    int monthEduRatio = pTotal;

                    personalRef.child("dayEduRatio").setValue(dayEduRatio);
                    personalRef.child("weekEduRatio").setValue(weekEduRatio);
                    personalRef.child("monthEduRatio").setValue(monthEduRatio);

                } else {
                    personalRef.child("dayEduRatio").setValue(0);
                    personalRef.child("weekEduRatio").setValue(0);
                    personalRef.child("monthEduRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getCharityBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Charity");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayCharRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayCharRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayCharRatio = pTotal / 28;
                    }
                    int weekCharRatio = pTotal / 4;
                    int monthCharRatio = pTotal;

                    personalRef.child("dayCharRatio").setValue(dayCharRatio);
                    personalRef.child("weekCharRatio").setValue(weekCharRatio);
                    personalRef.child("monthCharRatio").setValue(monthCharRatio);

                } else {
                    personalRef.child("dayCharRatio").setValue(0);
                    personalRef.child("weekCharRatio").setValue(0);
                    personalRef.child("monthCharRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getApparelBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Apparel");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayAppRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayAppRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayAppRatio = pTotal / 28;
                    }
                    int weekAppRatio = pTotal / 4;
                    int monthAppRatio = pTotal;

                    personalRef.child("dayAppRatio").setValue(dayAppRatio);
                    personalRef.child("weekAppRatio").setValue(weekAppRatio);
                    personalRef.child("monthAppRatio").setValue(monthAppRatio);

                } else {
                    personalRef.child("dayAppRatio").setValue(0);
                    personalRef.child("weekAppRatio").setValue(0);
                    personalRef.child("monthAppRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getHealthBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Health");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayHealthRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayHealthRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayHealthRatio = pTotal / 28;
                    }
                    int weekHealthRatio = pTotal / 4;
                    int monthHealthRatio = pTotal;

                    personalRef.child("dayHealthRatio").setValue(dayHealthRatio);
                    personalRef.child("weekHealthRatio").setValue(weekHealthRatio);
                    personalRef.child("monthHealthRatio").setValue(monthHealthRatio);

                } else {
                    personalRef.child("dayHealthRatio").setValue(0);
                    personalRef.child("weekHealthRatio").setValue(0);
                    personalRef.child("monthHealthRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getPersonalBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Personal");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayPerRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayPerRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayPerRatio = pTotal / 28;
                    }
                    int weekPerRatio = pTotal / 4;
                    int monthPerRatio = pTotal;

                    personalRef.child("dayPerRatio").setValue(dayPerRatio);
                    personalRef.child("weekPerRatio").setValue(weekPerRatio);
                    personalRef.child("monthPerRatio").setValue(monthPerRatio);

                } else {
                    personalRef.child("dayPerRatio").setValue(0);
                    personalRef.child("weekPerRatio").setValue(0);
                    personalRef.child("monthPerRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getOtherBudgetRatios() {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        int currentMonth = calendar.get(Calendar.MONTH);

        Query query = budgetRef.orderByChild("item").equalTo("Other");
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int pTotal = 0;
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Map<String, Object> map = (Map<String, Object>) ds.getValue();
                        Object total = map.get("amount");
                        pTotal = Integer.parseInt(String.valueOf(total));
                    }

                    if (currentMonth == 0 || currentMonth == 2 || currentMonth == 4 || currentMonth == 6 || currentMonth == 7 || currentMonth == 9 || currentMonth == 11) {
                        dayOtherRatio = pTotal / 30;
                    } else if (currentMonth == 3 || currentMonth == 5 || currentMonth == 8 || currentMonth == 10) {
                        dayOtherRatio = pTotal / 30;
                    } else if (currentMonth == 1) {
                        dayOtherRatio = pTotal / 28;
                    }
                    int weekOtherRatio = pTotal / 4;
                    int monthOtherRatio = pTotal;

                    personalRef.child("dayOtherRatio").setValue(dayOtherRatio);
                    personalRef.child("weekOtherRatio").setValue(weekOtherRatio);
                    personalRef.child("monthOtherRatio").setValue(monthOtherRatio);

                } else {
                    personalRef.child("dayOtherRatio").setValue(0);
                    personalRef.child("weekOtherRatio").setValue(0);
                    personalRef.child("monthOtherRatio").setValue(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getTotalBudgetRatios() {

        //Getting Totals for Day Ratios
        personalRef.child("dayTransRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayTransport += tTotal;
                } else {
                    dayTransport = 0;
                }
                dailyTotal += dayTransport;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayFoodRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayFood += tTotal;
                } else {
                    dayFood = 0;
                }
                dailyTotal += dayFood;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayHouseRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayHouse += tTotal;
                } else {
                    dayHouse = 0;
                }
                dailyTotal += dayHouse;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayEntRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayEntertainment += tTotal;
                } else {
                    dayEntertainment = 0;
                }
                dailyTotal += dayEntertainment;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayEduRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayEducation += tTotal;
                } else {
                    dayEducation = 0;
                }
                dailyTotal += dayEducation;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayCharRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayCharity += tTotal;
                } else {
                    dayCharity = 0;
                }
                dailyTotal += dayCharity;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayAppRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayApparel += tTotal;
                } else {
                    dayApparel = 0;
                }
                dailyTotal += dayApparel;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayHealthRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayHealth += tTotal;
                } else {
                    dayHealth = 0;
                }
                dailyTotal += dayHealth;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayPerRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayPersonal += tTotal;
                } else {
                    dayPersonal = 0;
                }
                dailyTotal += dayPersonal;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("dayOtherRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    dayOther += tTotal;
                } else {
                    dayOther = 0;
                }
                dailyTotal += dayOther;
                personalRef.child("dailyBudget").setValue(dailyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //Getting Totals for Week Ratios
        personalRef.child("weekTransRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekTransport += tTotal;
                } else {
                    weekTransport = 0;
                }
                weeklyTotal += weekTransport;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekFoodRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekFood += tTotal;
                } else {
                    weekFood = 0;
                }
                weeklyTotal += weekFood;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekHouseRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekHouse += tTotal;
                } else {
                    weekHouse = 0;
                }
                weeklyTotal += weekHouse;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekEntRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekEntertainment += tTotal;
                } else {
                    weekEntertainment = 0;
                }
                weeklyTotal += weekEntertainment;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekEduRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekEducation += tTotal;
                } else {
                    weekEducation = 0;
                }
                weeklyTotal += weekEducation;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekCharRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekCharity += tTotal;
                } else {
                    weekCharity = 0;
                }
                weeklyTotal += weekCharity;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekAppRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekApparel += tTotal;
                } else {
                    weekApparel = 0;
                }
                weeklyTotal += weekApparel;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekHealthRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekHealth += tTotal;
                } else {
                    weekHealth = 0;
                }
                weeklyTotal += weekHealth;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekPerRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekPersonal += tTotal;
                } else {
                    weekPersonal = 0;
                }
                weeklyTotal += weekPersonal;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        personalRef.child("weekOtherRatio").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    int tTotal = snapshot.getValue(Integer.class);
                    weekOther += tTotal;
                } else {
                    weekOther = 0;
                }
                weeklyTotal += weekOther;
                personalRef.child("weeklyBudget").setValue(weeklyTotal);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}