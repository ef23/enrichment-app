package btao.com.quintessencelearning;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseRef;
    private DatabaseReference mQuestionRef;
    private DatabaseReference mUserRef;
    private DatabaseReference mQuestion;
    private DatabaseReference mUser;
    Long currentQuestion;

    private final String TAG = "MainActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    questionNav();
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }

    };
    public void 
    public void questionNav(){
        mTextMessage = (TextView) findViewById(R.id.message);
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(this, SignIn.class);
            startActivity(intent);
            finish();
        }
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mQuestionRef = mDatabaseRef.child("Questions");
        mUserRef = mDatabaseRef.child("Users");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
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
                    if (count==currentQuestion) {
                        mTextMessage.setText(text);
                        Log.d(TAG, text);
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_main);


        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        questionNav();
    }

    public void signOut(){
        auth.signOut();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            startActivity(new Intent(MainActivity.this, SignIn.class));
            finish();
        }
    }

    /*public void readQuestions() {

        JsonObject json = new JsonObject();
        try {
            json.addProperty("ascending", true);
        } catch (JsonParseException e) {
            e.printStackTrace();
        }

        Ion.with(getApplicationContext())
                .load("http://172.25.201.218:3001/signup")
                .setHeader("Accept","application/json")
                .setHeader("Content-Type","application/json")
                .setJsonObjectBody(json)
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String result) {
                        try {
                            JSONObject json = new JSONObject(result);    // Converts the string "result" to a JSONObject
                            String json_result = json.getString("questions"); // Get the string "result" inside the Json-object
                            Log.d(TAG,json_result);
                            mTextMessage.setText(json_result);
                        } catch (JSONException err){
                            // This method will run if something goes wrong with the json, like a typo to the json-key or a broken JSON.
                            err.printStackTrace();
                        }
                    }
                });
    }*/
}
