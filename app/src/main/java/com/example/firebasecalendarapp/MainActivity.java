package com.example.firebasecalendarapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

        // Inisialisasi elemen UI
        calendarView = findViewById(R.id.calendarView);
        editText = findViewById(R.id.editText);
        recyclerView = findViewById(R.id.recyclerView);

        // Set listener untuk perubahan tanggal pada CalendarView
        calendarView.setOnDateChangeListener((calendarView, i, i1, i2) -> {
            stringDateSelected = formatDate(i, i1, i2);
            updateEventList();
        });

        // Inisialisasi tombol "About"
        ImageButton aboutButton = findViewById(R.id.aboutButton);
        aboutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mengarahkan ke AboutActivity saat tombol About diklik
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });

        // Inisialisasi referensi Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Calendar");

        // Inisialisasi RecyclerView dan Adapter
        eventList = new ArrayList<>();
        eventAdapter = new EventAdapter(eventList, new EventAdapter.EventClickListener() {
            @Override
            public void onEditClick(int position) {
                // Handle klik tombol edit pada item RecyclerView
                EventModel event = eventList.get(position);
                editText.setText(event.getEventName());
                lastEditedPosition = position;
            }

            @Override
            public void onDeleteClick(int position) {
                // Handle klik tombol delete pada item RecyclerView dengan konfirmasi
                showDeleteConfirmationDialog(position);
            }
        });

        // Konfigurasi RecyclerView
        recyclerView.setAdapter(eventAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }




    private void showDeleteConfirmationDialog(int position) {
        // Membuat AlertDialog untuk konfirmasi penghapusan
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Hapus");
        builder.setMessage("Yakin ingin menghapus data?");

        // Tombol OK (Hapus)
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteEvent(position);
            }
        });

        // Tombol Batal
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); // Tutup dialog tanpa menghapus
            }
        });

        // Menampilkan AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // Mengambil data dari Firebase berdasarkan tanggal yang dipilih
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
                    // Handle kesalahan saat mengambil data dari Firebase
                    Log.e("FirebaseData", "Error fetching data", error.toException());
                }
            });
        }
    }

    // Menyimpan atau memperbarui event ke Firebase
    public void buttonSaveEvent(View view) {
        String eventName = editText.getText().toString();
        if (!eventName.isEmpty()) {
            if (lastEditedPosition != -1) {
                // Jika sedang mengedit, update event yang sudah ada
                updateEvent(lastEditedPosition, eventName);
                lastEditedPosition = -1;
            } else {
                // Jika tidak sedang mengedit, tambahkan event baru
                String eventKey = databaseReference.child("Event").child(stringDateSelected).push().getKey();
                databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(eventName);
            }
            updateEventList();
            clearEditText();
        }
    }

    // Menghapus event dari Firebase
    public void deleteEvent(int position) {
        if (position >= 0 && position < eventList.size()) {
            EventModel event = eventList.get(position);
            String eventKey = event.getEventKey();
            eventList.remove(position);
            eventAdapter.notifyItemRemoved(position);
            databaseReference.child("Event").child(stringDateSelected).child(eventKey).removeValue();
        } else {
            // Log jika posisi yang dihapus tidak valid
            Log.e("DeleteEvent", "Invalid position: " + position);
        }
    }

    // Memperbarui nama event di Firebase
    private void updateEvent(int position, String newName) {
        EventModel event = eventList.get(position);
        event.setEventName(newName);
        String eventKey = event.getEventKey();
        databaseReference.child("Event").child(stringDateSelected).child(eventKey).setValue(newName);
        eventAdapter.notifyItemChanged(position);
    }

    // Membersihkan EditText setelah menyimpan event
    private void clearEditText() {
        editText.setText("");
    }

    // Mengubah format tanggal menjadi string dengan format tertentu
    private String formatDate(int year, int month, int day) {
        return String.format(Locale.getDefault(), "%04d%02d%02d", year, month + 1, day);
    }
}
