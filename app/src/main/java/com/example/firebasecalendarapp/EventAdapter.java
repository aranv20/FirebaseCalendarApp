package com.example.firebasecalendarapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventModel> eventList;
    private EventClickListener eventClickListener;

    public EventAdapter(List<EventModel> eventList, EventClickListener eventClickListener) {
        this.eventList = eventList;
        this.eventClickListener = eventClickListener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.textEventName.setText(event.getEventName());
        holder.btnEdit.setOnClickListener(v -> eventClickListener.onEditClick(position));
        holder.btnDelete.setOnClickListener(v -> eventClickListener.onDeleteClick(position));
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEventName;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventName = itemView.findViewById(R.id.textEventName);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    public interface EventClickListener {
        void onEditClick(int position);
        void onDeleteClick(int position);
    }
}
