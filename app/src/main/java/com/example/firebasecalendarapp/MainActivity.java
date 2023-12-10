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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        calendarView = findViewById(R.id.calendarView);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int i, int i1, int i2) {
                stringDateSelected = formatDate(i, i1, i2);
                updateEventList();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("Calendar");

        // Initialize and set up the RecyclerView and adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.EventClickListener() {
            @Override
            public void onEditClick(int position) {
                // Implement edit action
                // You can open a dialog or another activity for editing the event
                // For simplicity, let's just show a log message
                Log.d("EditEvent", "Edit button clicked for position: " + position);
            }

            @Override
            public void onDeleteClick(int position) {
                // Implement delete action
                // You may want to show a confirmation dialog before deleting
                deleteEvent(position);
            }
        });

        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateEventList() {
        if (stringDateSelected != null && !stringDateSelected.isEmpty()) {
            // Fetch and update the eventList from Firebase
            databaseReference.child("Event").child(stringDateSelected).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    eventList.clear();

                    // Pastikan snapshot tidak null sebelum memproses
                    if (snapshot.exists()) {
                        for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                            String eventName = eventSnapshot.getValue(String.class);
                            String eventKey = eventSnapshot.getKey();
                            eventList.add(new EventModel(eventKey, stringDateSelected, eventName));
                        }
                        eventAdapter.notifyDataSetChanged();
                    } else {
                        // Data tidak ditemukan, mungkin karena belum ada data di tanggal ini
                        // Tambahkan log atau pesan untuk membantu pemecahan masalah
                        Log.d("FirebaseData", "Data not found for date: " + stringDateSelected);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle database error
                    Log.e("FirebaseData", "Error fetching data", error.toException());
                }
            });
        }
    }

    public void buttonSaveEvent(View view) {
        String eventName = editText.getText().toString();
        if (!eventName.isEmpty()) {
            // Save event under the selected date
            String eventKey = databaseReference.child("Event").child(stringDateSelected).push().getKey();
            databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(eventName);
            updateEventList(); // Update the RecyclerView after saving
        }
    }

    // Method to delete an event
    public void deleteEvent(int position) {
        if (position >= 0 && position < eventList.size()) {
            // Get the selected event
            EventModel event = eventList.get(position);

            // Get the key of the selected event
            String eventKey = event.getEventKey();

            // Remove the event from the list
            eventList.remove(position);

            // Notify the adapter of the change
            eventAdapter.notifyItemRemoved(position);

            // Remove the event from Firebase
            databaseReference.child("Event").child(stringDateSelected).child(eventKey).removeValue();
        } else {
            Log.e("DeleteEvent", "Invalid position: " + position);
        }
    }


    private String formatDate(int year, int month, int day) {
        // Adjust month by adding 1, as months are zero-based in Calendar
        return String.format(Locale.getDefault(), "%04d%02d%02d", year, month + 1, day);
    }
}
