package btao.com.quintessencelearning;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.icu.util.Calendar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.Format;
import java.text.SimpleDateFormat;

public class WelcomeScreen extends AppCompatActivity {
    public static EditText time;

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker t, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            String a="AM";
            if(hourOfDay>=12){
                hourOfDay=hourOfDay-12;
                a="PM";
            }
            String s;
            Format formatter;
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, t.getHour());
            calendar.set(Calendar.MINUTE, t.getMinute());
            calendar.clear(Calendar.SECOND); //reset seconds to zero

            formatter = new SimpleDateFormat("hh:mm a");
            s = formatter.format(calendar.getTime()); // 08:00:00

            String timeString = s;
                    //Integer.toString(hourOfDay) + " : " + Integer.toString(minute) + "  " + a;
            WelcomeScreen.time.setText(timeString);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_screen);

    }

    public void pickTime(View view){
        time = (EditText) findViewById(R.id.time_edit);
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(),"TimePicker");
    }

    public void finish(View view){
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();
    }
}


