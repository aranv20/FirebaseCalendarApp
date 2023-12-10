package com.example.firebasecalendarapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private EditText editText;
    private String stringDateSelected;
    private DatabaseReference databaseReference;
    private RecyclerView recyclerView;
    private EventAdapter eventAdapter;
    private List<EventModel> eventList;
    private int lastEditedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        calendarView.setOnDateChangeListener((calendarView, i, i1, i2) -> {
            stringDateSelected = formatDate(i, i1, i2);
            updateEventList();
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Calendar");

        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.EventClickListener() {
            @Override
            public void onEditClick(int position) {
                EventModel event = eventList.get(position);
                editText.setText(event.getEventName());
                lastEditedPosition = position;
            }

            @Override
            public void onDeleteClick(int position) {
                deleteEvent(position);
            }
        });

        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateEventList() {
        if (stringDateSelected != null && !stringDateSelected.isEmpty()) {
            databaseReference.child("Event").child(stringDateSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    eventList.clear();

                    if (snapshot.exists()) {
                        for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                            String eventName = eventSnapshot.getValue(String.class);
                            String eventKey = eventSnapshot.getKey();
                            eventList.add(new EventModel(eventKey, stringDateSelected, eventName));
                        }
                    }

                    // Tambahkan item null jika tidak ada event
                    if (eventList.isEmpty()) {
                        eventList.add(null);
                    }

                    eventAdapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("FirebaseData", "Error fetching data", error.toException());
                }
            });
        }
    }


    public void buttonSaveEvent(View view) {
        String eventName = editText.getText().toString();
        if (!eventName.isEmpty()) {
            if (lastEditedPosition != -1) {
                updateEvent(lastEditedPosition, eventName);
                lastEditedPosition = -1;
            } else {
                String eventKey = databaseReference.child("Event").child(stringDateSelected).push().getKey();
                databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(eventName);
            }
            updateEventList();
            clearEditText();
        }
    }

    public void deleteEvent(int position) {
        if (position >= 0 && position < eventList.size()) {
            EventModel event = eventList.get(position);
            String eventKey = event.getEventKey();
            eventList.remove(position);
            eventAdapter.notifyItemRemoved(position);
            databaseReference.child("Event").child(stringDateSelected).child(eventKey).removeValue();
        } else {
            Log.e("DeleteEvent", "Invalid position: " + position);
        }
    }

    private void updateEvent(int position, String newName) {
        EventModel event = eventList.get(position);
        event.setEventName(newName);
        String eventKey = event.getEventKey();
        databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(newName);
        eventAdapter.notifyItemChanged(position);
    }

    private void clearEditText() {
        editText.setText("");
    }

    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d%02d%02d", year, month + 1, day);
    }
}