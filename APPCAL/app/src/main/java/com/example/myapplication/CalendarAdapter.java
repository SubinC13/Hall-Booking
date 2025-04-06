package com.example.myapplication;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.ViewHolder> {
    List<String> dates;
    private int selectedPosition = -1;
    private int initialSelectedPosition = -1; // Initially selected date
    private boolean userSelectedDate = false;
    private OnDateSelectedListener dateSelectedListener;

    public void setInitialSelectedPosition(int position) {
        initialSelectedPosition = position;
        // Mark that the user hasn't manually selected a date yet
        userSelectedDate = false;
        notifyDataSetChanged(); // Refresh the view to apply the initial selection
    }

    public CalendarAdapter(List<String> dates, OnDateSelectedListener listener) {
        this.dates = dates;
        this.dateSelectedListener = listener;
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged(); // Refresh the view to apply the selection
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_layout, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String date = dates.get(holder.getAdapterPosition());
        holder.dateTextView.setText(date);

        // Highlight the currently selected day
        if (holder.getAdapterPosition() == selectedPosition) {
            holder.calender_selected_BAck.setBackgroundResource(R.drawable.selected_date_background);
        } else {
            holder.calender_selected_BAck.setBackgroundResource(R.drawable.rectangle_outline);
        }

        // Handle date selection
        holder.dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userSelectedDate) {
                    // If the user hasn't manually selected a date yet, set the user-selected date
                    userSelectedDate = true;
                }
                dateSelectedListener.onDateSelected(date);
                setSelectedPosition(holder.getAdapterPosition());
            }
        });
    }



    @Override
    public int getItemCount() {
        return dates.size();
    }

    // Add this method to update the dates when the month changes
    public void updateDates(List<String> newDates) {
        dates.clear();
        dates.addAll(newDates);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        LinearLayout calender_selected_BAck;

        public ViewHolder(View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            calender_selected_BAck = itemView.findViewById(R.id.linear_calendar);
        }
    }
}