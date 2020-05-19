package com.myapp.fieldsbs;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class ManageFieldsActivity extends AppCompatActivity {

    String id, key, date;
    TextView dateTxt, lblScreen;
    DatePickerDialog.OnDateSetListener mDateListener;
    FirebaseAuth fAuth;
    Button btnRemove, btnFootball, btnBasketball, btnGym;
    Spinner chooseFieldSpinner;
    DatabaseReference managementRef, fieldRef, rootRef;
    ArrayList<String> fieldsList, filteredFieldsList, idList, keyList, filteredKeysList, nameList, typeList, showList, hoursList, statusList, numofPlayersList;
    ListView myList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ctivity_manage_fields);

        myList = findViewById(R.id.listView);
        keyList = new ArrayList<>();
        filteredKeysList = new ArrayList<>();
        nameList = new ArrayList<>();
        typeList = new ArrayList<>();
        fieldsList = new ArrayList<>();
        filteredFieldsList = new ArrayList<>();


        // REMOVE ALL?!
        showList = new ArrayList<>();
        idList = new ArrayList<>();
        hoursList = new ArrayList<>();
        statusList = new ArrayList<>();
        numofPlayersList = new ArrayList<>();



        managementRef = FirebaseDatabase.getInstance().getReference().child("Management");
        rootRef = FirebaseDatabase.getInstance().getReference();
        dateTxt = findViewById(R.id.dateTxtt);
        fAuth = FirebaseAuth.getInstance();
        //id = "RvaYU2iMhXX76WrnMg55BkHE0g23";
        key = "1";//findViewById(R.id.spinnerChooseField).toString();

        btnRemove = findViewById(R.id.btnRemove);
        btnFootball = findViewById(R.id.btnFootball);
        btnBasketball = findViewById(R.id.btnBasketball);
        btnGym = findViewById(R.id.btnGym);

        chooseFieldSpinner = (Spinner) findViewById(R.id.spinnerChooseField);
        lblScreen = findViewById(R.id.lblScreen);

        // Field picker
        id= fAuth.getUid();

        fieldRef = FirebaseDatabase.getInstance().getReference().child("Fields");
        fieldRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    keyList.add(Objects.requireNonNull(ds.getKey()));
                    typeList.add(Objects.requireNonNull(ds.child("Type").getValue()).toString());
                    fieldsList.add(Objects.requireNonNull(ds.child("Name").getValue()).toString());
                }

                // Spinner initialize (fields select)
                filteredFieldsList = fieldsList;
                filteredKeysList = keyList;
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManageFieldsActivity.this, android.R.layout.simple_spinner_item, filteredFieldsList);
                //selected item will look like a spinner set from XML
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                chooseFieldSpinner.setAdapter(spinnerArrayAdapter);
                //setView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Field filtering
        btnFootball.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // TODO: filter to Football
                filterFields("כדורגל");
            }
        });
        btnBasketball.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // TODO: filter to Basketball
                filterFields("כדורסל");
            }
        });
        btnGym.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                // TODO: filter to GYM
                filterFields("כושר");
            }
        });

        // Date related
        dateTxt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int thisYear = cal.get(Calendar.YEAR);
                int thisMonth = cal.get(Calendar.MONTH);
                int thisDay = cal.get(Calendar.DAY_OF_MONTH);

                // TODO: TEST?
                //key =

                DatePickerDialog dialog = new DatePickerDialog(
                        ManageFieldsActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateListener, thisYear, thisMonth, thisDay);
                Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                if (year < cal.get(Calendar.YEAR) || (year == cal.get(Calendar.YEAR) && month < cal.get(Calendar.MONTH)) || (year == cal.get(Calendar.YEAR) && month == cal.get(Calendar.MONTH)) && day < cal.get(Calendar.DAY_OF_MONTH)){
                    Toast.makeText(ManageFieldsActivity.this, "התאריך המבוקש כבר עבר.", Toast.LENGTH_SHORT).show();
                }
                else{
                    month = month + 1;
                    date = day + "-" + month + "-" + year;
                    dateTxt.setText(date);
                    checkEmptyDate();
                }
            }
        };

        // Management related
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,long arg3) {
                // TODO: make this really change the sleected field ID
                // Update the field KEY
                key = filteredKeysList.get(chooseFieldSpinner.getSelectedItemPosition());
                Object test = chooseFieldSpinner.getSelectedItem();
                view.setSelected(true);
                lblScreen.setText(showList.get(position).substring(23,34));
                //name = nameList.get(position);
            }
        });

        btnRemove.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (lblScreen.getText().toString().equals("בחר שעה")){
                    Toast.makeText(ManageFieldsActivity.this, "בבקשה תבחר תאריך ואז שעה פנויה מתוך הרשימה.", Toast.LENGTH_SHORT).show();
                }
                else {
                    managementRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            removeWholeField();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
        });
    }



    // Choosing field time
    public void setView(){

        managementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearLists();

                //  update the current field key
                key = filteredKeysList.get(chooseFieldSpinner.getSelectedItemPosition());

                for(DataSnapshot ds : dataSnapshot.child(key).child(date).getChildren()) {
                    hoursList.add(Objects.requireNonNull(ds.getKey()));
                    statusList.add(Objects.requireNonNull(ds.child("status").getValue()).toString());
                    if(ds.child("numofPlayers").getValue().toString() != "0"){
                        numofPlayersList.add(Objects.requireNonNull("1"));
                    }
                    else{
                        numofPlayersList.add(Objects.requireNonNull("0"));
                    }
                    typeList.add(Objects.requireNonNull(ds.child("type").getValue()).toString());
                    //idList.add(Objects.requireNonNull(ds.child("id").getValue()).toString());
                    nameList.add(Objects.requireNonNull(ds.child("creator").getValue()).toString());

                    //int numberofPlayers = Integer.valueOf(Objects.requireNonNull(ds.child("numofPlayers").getValue()).toString());
                    //for (int i = 0; i < numberofPlayers; i++)
                    //    namesList[j].add(Objects.requireNonNull(ds.child("namesList").child(String.valueOf(i)).getValue()).toString());



                }
                for (int i = 0; i < statusList.size(); i++){
                    if (statusList.get(i).equals("פנוי")){
                        System.out.println("available");
                        //showList.add("|שעות:  " + hoursList.get(i) + "\n|זמינות: " + statusList.get(i));
                    }
                    else{
                        System.out.println("not available");
                        showList.add("|שעות:                 " + hoursList.get(i) + "\n|זמינות:               " + statusList.get(i) + "\n|מספר שחקנים: " + numofPlayersList.get(i) + "\n|סוג:                    " + typeList.get(i) + "\n|שם האחראי:    " + nameList.get(i));
                        //System.out.println(showList.get(i));
                    }
                }
                showView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void showView(){
        ListAdapter listAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_activated_1, showList);
        myList.setAdapter(listAdapter);
    }

    public void removeWholeField(){
        managementRef.child(key).child(date).child(lblScreen.getText().toString()).child("participantList").setValue(null);
        managementRef.child(key).child(date).child(lblScreen.getText().toString()).child("creator").setValue("ריק");
        managementRef.child(key).child(date).child(lblScreen.getText().toString()).child("status").setValue("פנוי");
        managementRef.child(key).child(date).child(lblScreen.getText().toString()).child("numofPlayers").setValue("0");
        managementRef.child(key).child(date).child(lblScreen.getText().toString()).child("type").setValue("ריק");
        
        setView();
        Toast.makeText(ManageFieldsActivity.this, "המגרש רוקן ממשתתפים בהצלחה.", Toast.LENGTH_SHORT).show();
    }

    public void clearLists(){
        showList.clear();
        idList.clear();
        hoursList.clear();
        statusList.clear();
        //typeList.clear();
        nameList.clear();
        numofPlayersList.clear();
    }

    public void filterFields(String type)
    {
        // TODO: FILTER
        filteredFieldsList = new ArrayList<>();
        filteredKeysList = new ArrayList<>();
        for (int currFieldId = 0; currFieldId < fieldsList.size(); currFieldId++){
            // Check if this field the same type as requested
            if(currFieldId < typeList.size())
            {
                if(typeList.get(currFieldId).contains(type) || ((type=="כדורסל" || type=="כדורגל")&&typeList.get(currFieldId).contains("משולב"))) {
                    filteredFieldsList.add(fieldsList.get(currFieldId));
                    filteredKeysList.add(keyList.get(currFieldId));
                }
            }
        }

        // Update the spinner optoins values
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(ManageFieldsActivity.this, android.R.layout.simple_spinner_item, filteredFieldsList);
        //selected item will look like a spinner set from XML
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        chooseFieldSpinner.setAdapter(spinnerArrayAdapter);
    }

    public void checkEmptyDate(){
        //managementRef = FirebaseDatabase.getInstance().getReference().child("Management");
        managementRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Choosing field time
                if (dataSnapshot.child(key).hasChild(date)){
                    System.out.println("inside has child");

                }
                else{
                    System.out.println("it does not have childs");
                    //if its empty we will make the date in the database with empty hours.
                    // Delete all this - not needed - but make sure
                    for (int i = 7; i < 24; i++){
                        if (i < 9){
                            managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("id").setValue("ריק");
                            managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("status").setValue("פנוי");
                            managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("numofPlayers").setValue("0");
                            managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("type").setValue("ריק");
                            managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("creator").setValue("ריק");
                            //managementRef.child(key).child(date).child("0" + i + ":00-0" + (i + 1) + ":00").child("namesList").child("0").setValue("ריק");
                        }
                        else if (i == 9){
                            managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("id").setValue("ריק");
                            managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("status").setValue("פנוי");
                            managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("numofPlayers").setValue("0");
                            managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("type").setValue("ריק");
                            managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("creator").setValue("ריק");
                            //managementRef.child(key).child(date).child("0" + i + ":00-" + (i + 1) + ":00").child("namesList").child("0").setValue("ריק");
                        }
                        else{
                            managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("id").setValue("ריק");
                            managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("status").setValue("פנוי");
                            managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("numofPlayers").setValue("0");
                            managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("type").setValue("ריק");
                            managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("creator").setValue("ריק");
                            //managementRef.child(key).child(date).child(i + ":00-" + (i + 1) + ":00").child("namesList").child("0").setValue("ריק");
                        }
                    }
                }

                // SET?!
                setView();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void backClick(View view){
        startActivity(new Intent(getApplicationContext(), AdminMainActivity.class));
    }
}
