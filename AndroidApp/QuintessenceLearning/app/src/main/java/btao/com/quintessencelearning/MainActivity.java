package btao.com.quintessencelearning;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;



public class MainActivity extends AppCompatActivity{

    private static FirebaseAuth auth;
    private static DatabaseReference mDatabaseRef = FirebaseDatabase.getInstance().getReference();
    private static DatabaseReference mQuestionRef = mDatabaseRef.child("Questions");
    private static DatabaseReference mUserRef = mDatabaseRef.child("Users");

    private static DatabaseReference mUser;
    Long currentQuestion;

    private QuestionsFragment qFrag;
    private AccountFragment aFrag;
    private Fragment fragment;
    private FragmentManager fragmentManager;

    private final String TAG = "MainActivity";

    static Integer notification_hour = 0;
    static Integer notification_minute = 0;

    Long user_current_question;
    String user_email;
    Long user_join_date;
    String user_name;
    Boolean user_trial;
    String user_type;
    String user_uid;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_questions:

                    qFrag = new QuestionsFragment();
                    fragment = qFrag;

                    transaction.replace(R.id.main_container, fragment).commit();
                    fragmentManager.executePendingTransactions();

                    questionNav();

                    break;
                case R.id.navigation_account:
                    aFrag = new AccountFragment();
                    fragment = aFrag;
                    transaction.replace(R.id.main_container, fragment).commit();
                    fragmentManager.executePendingTransactions();

                    aFrag.setName(user_name);
                    aFrag.setEmail(user_email);

                    fragment = new AccountFragment();

                    break;
                case R.id.navigation_feedback:
                    fragment = new FeedbackFragment();
                    transaction.replace(R.id.main_container, fragment).commit();
                    fragmentManager.executePendingTransactions();
                    break;
            }
            return true;
        }

    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(getApplicationContext(), SignIn.class);
            startActivity(intent);
            finish();
        }

        if( getIntent().getExtras()!=null && getIntent().getExtras().get("sender").equals("WelcomeScreen"))
        {
            //do here
            notification_hour = getIntent().getExtras().getInt("setHour");
            notification_minute = getIntent().getExtras().getInt("setMinute");
            Log.d(TAG, notification_hour.toString());
            Log.d(TAG,notification_minute.toString());

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                            .setContentTitle("My notification")
                            .setContentText("Hello World!")
                            .setAutoCancel(true);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            mBuilder.setPriority(1);
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mBuilder.setSound(alarmSound);
            mBuilder.setVibrate(new long[] {500});
            mBuilder.setLights(Color.RED, 3000, 3000);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);

            // mNotificationId is a unique integer your app uses to identify the
            // notification. For example, to cancel the notification, you can pass its ID
            // number to NotificationManager.cancel().
            mNotificationManager.notify(1, mBuilder.build());

        }
        if (auth.getCurrentUser()!=null) {
            final String userUID = user.getUid();
            mUser = mUserRef.child(userUID);


            mUser.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    user_current_question = (Long) dataSnapshot.child("Current_Question").getValue();
                    user_email = (String) dataSnapshot.child("Email").getValue();
                    user_join_date = (Long) dataSnapshot.child("Join_Date").getValue();
                    user_name = (String) dataSnapshot.child("Name").getValue();
                    user_trial = (Boolean) dataSnapshot.child("Trial").getValue();
                    user_type = (String) dataSnapshot.child("Type").getValue();
                    user_uid = (String) dataSnapshot.child("UID").getValue();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d(TAG, "read failed");
                }
            });
            fragmentManager = getSupportFragmentManager();
            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            qFrag = new QuestionsFragment();
            fragment = qFrag;

            transaction.replace(R.id.main_container, fragment).commit();
            fragmentManager.executePendingTransactions();

            questionNav();
        }



        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);


    }
    public void signOut(View view){
        auth.signOut();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(getApplicationContext(), SignIn.class));
            finish();
        }
    }

    public void questionNav(){

        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        }


        final String userUID = user.getUid();
        mUser = mUserRef.child(userUID);


        final ValueEventListener questionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Log.d(TAG, "Inside class");
                    String text = (String) child.child("Text").getValue();
                    Long count = (Long) child.child("count").getValue();
                    if (count.equals(currentQuestion)) {
                        Log.d(TAG, text);
                        qFrag.setQuestion(text);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Toast.makeText(getApplicationContext(),databaseError.toString(),Toast.LENGTH_SHORT).show();
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };

        mUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentQuestion = (Long) dataSnapshot.child("Current_Question").getValue();
                mQuestionRef.addListenerForSingleValueEvent(questionListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG,"Couldn't get user ref");
            }
        });

    }

    public void changeEmail(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.change_email);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.email_dialog,null));
        builder.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG,"Positive click");
                Dialog dialogD = (Dialog) dialog;
                EditText et = (EditText) dialogD.findViewById(R.id.text_change_email);
                String val = et.getText().toString();
                try {
                    user.updateEmail(val)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");
                                        Toast.makeText(MainActivity.this, "Email Updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.w(TAG, "Did not update", task.getException());
                                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch(IllegalArgumentException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"You cannot leave any fields blank",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(TAG,"Negative click");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void changePassword(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.change_password);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.password_dialog,null));
        builder.setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG,"Positive click");
                Dialog dialogD = (Dialog) dialog;
                EditText et = (EditText) dialogD.findViewById(R.id.text_change_password);
                String val = et.getText().toString();
                //((InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE))
                //        .showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
                try {
                    user.updatePassword(val)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User Password updated.");
                                        Toast.makeText(MainActivity.this, "Password Updated", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.w(TAG, "Did not update", task.getException());
                                        Toast.makeText(MainActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch(IllegalArgumentException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(),"You cannot leave any fields blank",Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(TAG,"Negative click");
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void aboutUs(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.about_us);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.about_us_dialog,null));
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void submitQuestion(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.submit_question);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.submit_question_dialog,null));
        builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG,"Positive click");
                Dialog dialogD = (Dialog) dialog;
                EditText question_edit = (EditText) dialogD.findViewById(R.id.text_question);
                EditText question_tags = (EditText) dialogD.findViewById(R.id.text_tags);
                String question;
                String tags;
                question = question_edit.getText().toString();
                tags = question_tags.getText().toString();
                //Toast.makeText(getApplicationContext(),"You cannot leave fields blank",Toast.LENGTH_SHORT).show();

                JsonObject params = new JsonObject();
                String email = auth.getCurrentUser().getEmail();
                String subject = "New question from " + email;

                String content = "<p>"+user_name+","+email+" submitted a question:</p>";
                content+="<p>Question:"+question+"</p>";
                content+="<p>Tags:"+tags+"</p>";
                Log.d(TAG,content);

                try {
                    params.addProperty("subject", subject);
                    params.addProperty("content", content);
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
                Ion.with(getApplicationContext())
                        .load(R.string.ip+"/email")
                        .setHeader("Accept","application/json")
                        .setHeader("Content-Type","application/json")
                        .setJsonObjectBody(params)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Toast.makeText(getApplicationContext(),"Question Submitted",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(TAG,"Negative click");
            }
        });
        builder.show();
    }

    public void submitFeedback(View view){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.submit_feedback);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.submit_feedback_dialog,null));
        builder.setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                Log.d(TAG,"Positive click");
                Dialog dialogD = (Dialog) dialog;
                EditText mYour_name = (EditText) dialogD.findViewById(R.id.text_your_name);
                EditText mYour_email = (EditText) dialogD.findViewById(R.id.text_your_email);
                EditText mYour_subject = (EditText) dialogD.findViewById(R.id.text_your_subject);
                EditText mYour_content = (EditText) dialogD.findViewById(R.id.text_your_content);

                String your_name = mYour_name.getText().toString();
                String your_email = mYour_email.getText().toString();
                String your_subject = mYour_subject.getText().toString();
                String your_content = mYour_content.getText().toString();

                JsonObject params = new JsonObject();
                String subject = "Feedback from:  " + your_name+ " ("+your_email+") "+your_subject ;

                String content = "<p>"+your_name+","+your_email+" submitted feedback:</p>";
                content+="<p>Feedback:" + your_content+"</p>";
                Log.d(TAG,content);

                try {
                    params.addProperty("subject", subject);
                    params.addProperty("content", content);
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
                Ion.with(getApplicationContext())
                        .load("http://192.168.1.252:3000/email")
                        .setHeader("Accept","application/json")
                        .setHeader("Content-Type","application/json")
                        .setJsonObjectBody(params)
                        .asString()
                        .setCallback(new FutureCallback<String>() {
                            @Override
                            public void onCompleted(Exception e, String result) {
                                Toast.makeText(getApplicationContext(),"Feedback Submitted",Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                Log.d(TAG,"Negative click");
            }
        });
        builder.show();
    }

    public void fAQ(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.FAQ_title);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.faq_dialog,null));
        builder.show();

    }

    public void eBook(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.ebook_title);
        LayoutInflater inflater = getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.ebook_dialog,null));
        builder.show();

    }

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
            mUser = mUserRef.child(auth.getCurrentUser().getUid());
            Calendar new_time = Calendar.getInstance();
            new_time.set(Calendar.HOUR_OF_DAY, t.getHour());
            new_time.set(Calendar.MINUTE, t.getMinute());
            new_time.clear(Calendar.SECOND); //reset seconds to zero
            Long new_time_sec = new_time.getTimeInMillis()/1000;
            mUser.child("Time").setValue(new_time_sec);
            Toast.makeText(getActivity(),R.string.time_updated,Toast.LENGTH_SHORT).show();

        }
    }

    public void setTime(View view){
        Integer current_hour=notification_hour;
        Integer current_minute = notification_minute;
        Calendar old_time = Calendar.getInstance();
        old_time.set(Calendar.HOUR_OF_DAY, current_hour);
        old_time.set(Calendar.MINUTE, current_minute);
        old_time.clear(Calendar.SECOND); //reset seconds to zero
        Long old_time_sec = old_time.getTimeInMillis()/1000;



        mUser = mUserRef.child(auth.getCurrentUser().getUid());

        mUser.child("Old_Time").setValue(old_time_sec);

        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(),"TimePicker");




        /*
        set current time = old time
        set new time = time
        set notification = to time if old time is null, otherwise = old,
        when notification is fired, set old time = null;

         */
    }
}
