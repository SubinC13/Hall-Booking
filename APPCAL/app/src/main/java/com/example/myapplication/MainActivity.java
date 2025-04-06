package com.example.myapplication;

import static com.example.myapplication.R.color.black;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ktx.Firebase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnDateSelectedListener {

    private CalendarAdapter adapter;
    private RecyclerView calendarRecyclerView,recyclerView;
    private TextView monthTextView;

    private Button showBookingForm;


    FirebaseAuth auth;

    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    BookingAdapter bookingAdapter;
    ArrayList<BookingsDisplayModels> list;
    String selectedDate;
    String newselectedDate;

    ImageView noImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = this.getWindow();
            window.setStatusBarColor(this.getResources().getColor(R.color.primaryMain));
        }
        setContentView(R.layout.activity_main);


        noImage = findViewById(R.id.noSlotsImageView);

        recyclerView = findViewById(R.id.bookedData);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        calendarRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        monthTextView = findViewById(R.id.text_date_month);
        showBookingForm = findViewById(R.id.buttonPopUPMeetBook);

        // Set click listeners for the previous and next arrows
        ImageView previousButton = findViewById(R.id.iv_calendar_previous);
        ImageView nextButton = findViewById(R.id.iv_calendar_next);



        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMonth(-1);  // Move to the previous month
            }
        });


        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMonth(1);  // Move to the next month
            }
        });

        Calendar currentCalendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthTextView.setText(sdf.format(currentCalendar.getTime()));

        List<String> dates = generateCalendarDates(currentCalendar);

        adapter = new CalendarAdapter(dates, this);
        calendarRecyclerView.setAdapter(adapter);


        // Find and select the current day
        selectCurrentDay();

        showBookingForm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bookingFormPopUp();
            }
        });

        recyclerView = findViewById(R.id.bookedData);
        databaseReference = FirebaseDatabase.getInstance().getReference("Bookings");
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        bookingAdapter = new BookingAdapter(this, list,selectedDate);
        recyclerView.setAdapter(bookingAdapter);


        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear(); // Clear the list before adding new items
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    BookingModels bookingModel = dataSnapshot.getValue(BookingModels.class);

                    if (bookingModel != null) {
                        String bookingKey = dataSnapshot.getKey();

                        BookingsDisplayModels bookingsDisplayModel = new BookingsDisplayModels();
                        bookingsDisplayModel.setBookingKey(bookingKey);

                        // Set other properties like companyName, fromTime, toTime, bookingDate, etc.
                        bookingsDisplayModel.setEmail(bookingModel.getEmail());
                        bookingsDisplayModel.setCompanyName(bookingModel.getCompany_name());
                        bookingsDisplayModel.setFromTime(bookingModel.getFromtime());
                        bookingsDisplayModel.setToTime(bookingModel.getTotime());
                        bookingsDisplayModel.setBookingDate(bookingModel.getBookin_date());

                        list.add(bookingsDisplayModel);
                    }
                }

                // Notify the adapter that the data set has changed
                bookingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle errors
            }
        });

        bookingAdapter.updateSelectedDate(selectedDate);

        onDateSelected(selectedDate);

    }

    public boolean isFilteredListEmpty() {
        return isFilteredListEmpty();
    }


    void selectInitialDate() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Find the position of today's date in your dataset
        for (int i = 0; i < adapter.getItemCount(); i++) {
            String dateInAdapter = adapter.dates.get(i);
            String todayDate = new SimpleDateFormat("dd EE", Locale.getDefault()).format(calendar.getTime());

            if (dateInAdapter.equals(todayDate)) {
                adapter.setSelectedPosition(i);
                break;
            }
        }
    }

    private void bookingFormPopUp() {
        LinearLayoutCompat bookingScreen = new LinearLayoutCompat(MainActivity.this);
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                LinearLayoutCompat.LayoutParams.WRAP_CONTENT
        );
        int marginInPixels = getResources().getDimensionPixelSize(R.dimen.pop_up_margin); // Define your margin value in resources
        layoutParams.setMargins(marginInPixels, marginInPixels, marginInPixels, marginInPixels);
        bookingScreen.setLayoutParams(layoutParams);

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.pop_up_activity_booking, bookingScreen);

        Button close = view.findViewById(R.id.buttonCloseForm);
        Button submit =  view.findViewById(R.id.buttonSubmitForm);
        EditText companyNameEditText = view.findViewById(R.id.companyNameEditText);

        Button dateSelectorBTN = view.findViewById(R.id.dateSelectorBtn);
        Button fromTimeSelectorBTN = view.findViewById(R.id.fromTimeSelectorBtn);
        Button toTimeSelectorBTN = view.findViewById(R.id.toTimeSelectorBtn);

        TextView dateTextView = view.findViewById(R.id.dateTextViewBookingForm);
        TextView fromTimeTextView = view.findViewById(R.id.fromTimeTextViewBookingForm);
        TextView toTimeTextView = view.findViewById(R.id.toTimeTextViewBookingForm);






        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(bookingScreen); // Set the parent layout
        final AlertDialog alertDialog = builder.create();


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        dateSelectorBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);


                        // Check if the selected date is before today
                        if (selectedDate.before(calendar)) {
                            // Set the selected date to today's date
                            selectedDate = calendar;
                            Toast.makeText(MainActivity.this, "You can't select the date less than today date", Toast.LENGTH_SHORT).show();
                        }

                        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
                        dateTextView.setText(dateFormat.format(selectedDate.getTime()));
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });


        fromTimeSelectorBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current time
                final Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                // Create a TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Check if the selected time is valid (less than "to time")
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                        // Check if the selected time is valid (less than "to time")
                        if (isTimeValid(selectedTime, toTimeTextView.getText().toString())) {
                            fromTimeTextView.setText(selectedTime);
                        } else {
                            // Display an error message
                            Toast.makeText(MainActivity.this, "From time must be before the to time", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });

        toTimeSelectorBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current time
                final Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                // Create a TimePickerDialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);

                        // Check if the selected time is valid (greater than "from time")
                        if (isTimeValid(fromTimeTextView.getText().toString(), selectedTime)) {
                            toTimeTextView.setText(selectedTime);
                        } else {
                            // Display an error message and set the time to "00:00"
                            Toast.makeText(MainActivity.this, "To time must be after the from time", Toast.LENGTH_SHORT).show();
                            toTimeTextView.setText("00:00");
                        }
                    }
                }, hour, minute, true);

                timePickerDialog.show();
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String companyName = companyNameEditText.getText().toString().trim();
                String toTime = toTimeTextView.getText().toString();
                String fromTime = fromTimeTextView.getText().toString();
                String bookingDate = dateTextView.getText().toString(); // Get the selected date

                if (TextUtils.isEmpty(companyName)) {
                    // The EditText is empty, display an error message
                    companyNameEditText.setError("Company name is required");
                    companyNameEditText.requestFocus(); // Set focus to the EditText
                } else if (bookingDate.equals("DD-MM-YYYY")) {
                    dateTextView.setError("");
                    Toast.makeText(MainActivity.this, "Select the date ", Toast.LENGTH_SHORT).show();
                } else if (fromTime.equals("00:00") || toTime.equals("00:00")) {
                    Toast.makeText(MainActivity.this, "Select valid from and to times", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if the selected time slot is available
                    if (isTimeSlotAvailable(bookingDate, fromTime, toTime)) {
                        // Following lines to store the data in Firebase
                        auth = FirebaseAuth.getInstance();
                        user = auth.getCurrentUser();
                        firebaseDatabase = FirebaseDatabase.getInstance();
                        databaseReference = firebaseDatabase.getReference("Bookings");

                        String email, company_name, bookin_date;

                        email = user.getEmail();
                        company_name = companyNameEditText.getText().toString();
                        bookin_date = bookingDate;

                        // Generate a unique key for each new entry
                        String key = databaseReference.push().getKey();

                        BookingModels bookingModels = new BookingModels(email, company_name, bookin_date, fromTime, toTime);

                        // Set the data at the unique key
                        databaseReference.child(key).setValue(bookingModels);

                        bookingAdapter.notifyDataSetChanged();

                        alertDialog.dismiss();
                    } else {
                        // Display an error message if the time slot is not available
                        Toast.makeText(MainActivity.this, "Selected time slot is not available", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        if (alertDialog.getWindow()!=null){
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        alertDialog.show();
    }

    private boolean isTimeSlotAvailable(String selectedDate, String fromTime, String toTime) {
        for (BookingsDisplayModels existingBooking : list) {
            if (existingBooking.getBookingDate().equals(selectedDate)) {
                String existingFromTime = existingBooking.getFromTime();
                String existingToTime = existingBooking.getToTime();

                if (isTimeOverlap(fromTime, toTime, existingFromTime, existingToTime)) {
                    // There is an overlap, so the time slot is not available
                    return false;
                }
            }
        }

        // No overlap found, so the time slot is available
        return true;
    }


    private boolean isTimeOverlap(String fromTime1, String toTime1, String fromTime2, String toTime2) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());

        try {
            Date from1 = sdf.parse(fromTime1);
            Date to1 = sdf.parse(toTime1);
            Date from2 = sdf.parse(fromTime2);
            Date to2 = sdf.parse(toTime2);

            // Two time slots do not overlap if:
            // - Time 1 ends before Time 2 starts, OR
            // - Time 1 starts after Time 2 ends
            return !(to1.before(from2) || from1.after(to2));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }



    // Helper method to check if the selected time is valid
    private boolean isTimeValid(String fromTime, String toTime) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date from = sdf.parse(fromTime);
            Date to = sdf.parse(toTime);
            return from.before(to);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }



    private void updateMonth(int monthChange) {
        String currentMonth = monthTextView.getText().toString();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        Calendar today = Calendar.getInstance();
        try {
            calendar.setTime(sdf.parse(currentMonth));
            int currentYear = calendar.get(Calendar.YEAR);
            int currentMonthIndex = calendar.get(Calendar.MONTH);

            // Check if the user is trying to go to the previous month
            if (monthChange < 0) {
                // If the selected month is before the ongoing month, display an error message
                if ((currentYear < today.get(Calendar.YEAR)) ||
                        (currentYear == today.get(Calendar.YEAR) && currentMonthIndex + monthChange < today.get(Calendar.MONTH))) {
                    Toast.makeText(this, "Cannot go to previous month", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            calendar.add(Calendar.MONTH, monthChange);
            monthTextView.setText(sdf.format(calendar.getTime()));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<String> newDates = generateCalendarDates(calendar);
        adapter.updateDates(newDates);
    }


    private List<String> generateCalendarDates(Calendar selectedMonth) {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd EE", Locale.getDefault());
        Calendar today = Calendar.getInstance();
        selectedMonth.set(Calendar.DAY_OF_MONTH, 1);
        int daysInMonth = selectedMonth.getActualMaximum(Calendar.DAY_OF_MONTH);

        for (int i = 1; i <= daysInMonth; i++) {
            selectedMonth.set(Calendar.DAY_OF_MONTH, i);

            if (selectedMonth.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selectedMonth.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selectedMonth.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)) {
                // Display the date as text for today
                dates.add(sdf.format(selectedMonth.getTime()));
            } else if (!isDateBeforeToday(selectedMonth)) {
                dates.add(sdf.format(selectedMonth.getTime()));
            }
        }


        return dates;
    }


    private boolean isDateBeforeToday(Calendar date) {
        Calendar today = Calendar.getInstance();
        return date.before(today);
    }



    private void selectCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Find the position of today's date in your dataset
        for (int i = 0; i < adapter.getItemCount(); i++) {
            String dateInAdapter = adapter.dates.get(i);
            String todayDate = new SimpleDateFormat("dd EE", Locale.getDefault()).format(calendar.getTime());

            if (dateInAdapter.equals(todayDate)) {
                adapter.setSelectedPosition(i);
                calendarRecyclerView.scrollToPosition(i);
                onDateSelected(todayDate);
                break;
            }
        }
    }


    @Override
    public void onDateSelected(String selectedDate) {
        this.selectedDate = selectedDate;



        if (bookingAdapter != null) {
            bookingAdapter.updateSelectedDate(selectedDate);
            bookingAdapter.notifyDataSetChanged();
            bookingAdapter.hasBookingsForSelectedDate();// Move this line here
            if (bookingAdapter.hasBookingsForSelectedDate()){
                noImage.setVisibility(View.GONE);
            }
            else {
                noImage.setVisibility(View.VISIBLE);
            }
        }

    }


}