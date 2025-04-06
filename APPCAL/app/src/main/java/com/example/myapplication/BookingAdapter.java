package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingHolder> {
    private Context context;
    private ArrayList<BookingsDisplayModels> list;
    private ArrayList<BookingsDisplayModels> filteredList;
    private String selectedDate;


    public BookingAdapter(Context context, ArrayList<BookingsDisplayModels> list, String selectedDate) {
        this.context = context;
        this.list = list;
        this.filteredList = new ArrayList<>();
        this.selectedDate = selectedDate;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public BookingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.slot_layout, parent, false);
        return new BookingHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingHolder holder, int position) {

        BookingsDisplayModels bookingsDisplayModels = filteredList.get(position);
        holder.company_NAME.setText(bookingsDisplayModels.getCompanyName());
        holder.from_TIME.setText(bookingsDisplayModels.getFromTime());
        holder.to_TIME.setText(bookingsDisplayModels.getToTime());



        // Get the email of the currently logged-in user
        String loggedInUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Check if the booked slot's email matches the current user's email
        if (loggedInUserEmail != null && loggedInUserEmail.equals(bookingsDisplayModels.getEmail())) {
            // If the emails match, show the delete button
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.bookedImageIndicator.setVisibility(View.GONE);

            // Set an OnClickListener for the delete button
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Call the deleteBooking function with the corresponding booking
                    String bookingKey = bookingsDisplayModels.getBookingKey();
                    int adapterPosition = holder.getAdapterPosition();
                    deleteBooking(bookingKey, adapterPosition);
                }
            });
        } else {
            // If the emails don't match, hide the delete button
            holder.deleteButton.setVisibility(View.GONE);
            holder.bookedImageIndicator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return filteredList.size();
    }

    public void updateSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
        filterBookingsByDate(selectedDate);
        notifyDataSetChanged();
    }

    private void filterBookingsByDate(String date) {
        filteredList.clear();
        for (BookingsDisplayModels booking : list) {
            String formattedBookingDate = formatBookingDate(booking.getBookingDate());
            if (date.equals(formattedBookingDate)) {
                filteredList.add(booking);
            }
        }
    }

    public boolean hasBookingsForSelectedDate() {
        return !filteredList.isEmpty();
    }


    private String formatBookingDate(String originalDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd EE", Locale.getDefault());

            Date date = inputFormat.parse(originalDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return originalDate; // Return the original date in case of an error
    }
    public static class BookingHolder extends RecyclerView.ViewHolder {
        TextView company_NAME, from_TIME, to_TIME;
        ImageView deleteButton,bookedImageIndicator;

        private ImageView imageView;

        public BookingHolder(@NonNull View itemView) {
            super(itemView);
            company_NAME = itemView.findViewById(R.id.company_NAME);
            from_TIME = itemView.findViewById(R.id.from_TIME);
            to_TIME = itemView.findViewById(R.id.to_TIME);
            deleteButton = itemView.findViewById(R.id.DeleteButton);
            bookedImageIndicator = itemView.findViewById(R.id.bookedViewIMage);


        }
    }


    // Define the deleteBooking function here
    private void deleteBooking(String bookingKey, int position) {
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
        list.remove(filteredList.get(position));
        filteredList.remove(position);
        notifyDataSetChanged();

        // Remove the booking from the list
        if (position >= 0) {
            list.remove(position);
            notifyDataSetChanged();
        }

        // Delete the booking from the database using its key
        if (bookingKey != null) {
            DatabaseReference specificBookingRef = bookingsRef.child(bookingKey);
            specificBookingRef.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Booking deleted successfully", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


}