package com.example.kshitijsuri.sentiments;

import android.app.LoaderManager;
import android.content.Intent;

import android.content.Loader;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser mFirebaseUser;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;
    private FirebaseListAdapter<ChatMessage> adapter;
    private FirebaseAuth.AuthStateListener authStateListener;
    private int RC_SIGN_IN = 200;
    private String url = "http://192.168.43.1:5000/";
    private static final String TAG = "AndroidCameraApi";
    private String msg = "";
    private int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build()))
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);

                } else {
                    mFirebaseAuth = FirebaseAuth.getInstance();
                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    firebaseDatabase = FirebaseDatabase.getInstance();
                    Toast.makeText(MainActivity.this, "Welcome " + mFirebaseUser.getDisplayName(), Toast.LENGTH_LONG).show();
                    displayChatMessages();

                    ImageButton fab = findViewById(R.id.fab);

                    fab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            EditText input = findViewById(R.id.input);
                            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
                            // Read the input field and push a new instance
                            // of ChatMessage to the Firebase database
                            if (input.getText().toString().isEmpty()) {
                                Toast.makeText(MainActivity.this, "Enter a text to send", Toast.LENGTH_SHORT).show();
                            } else {
                                databaseReference.push()
                                        .setValue(new ChatMessage(input.getText().toString(),
                                                mFirebaseUser.getDisplayName())
                                        );
                            }

                            // Clear the input
                            input.setText("");
                        }
                    });
                }
            }
        };
        databaseReference = firebaseDatabase.getReference().child("messages");
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(authStateListener);
    }

    private void displayChatMessages() {
        ListView listOfMessages = findViewById(R.id.list_of_messages);
        FirebaseListOptions<ChatMessage> options = new FirebaseListOptions.Builder<ChatMessage>()
                .setLifecycleOwner(this)
                .setLayout(R.layout.message)
                .setQuery(databaseReference, ChatMessage.class)
                .build();
        adapter = new FirebaseListAdapter<ChatMessage>(options) {
            @Override
            protected void populateView(@NonNull View v, @NonNull ChatMessage model, int position) {
                // Get references to the views of message.xml
                TextView messageText = v.findViewById(R.id.message_text);
                TextView messageUser = v.findViewById(R.id.message_user);
                TextView messageTime = v.findViewById(R.id.message_time);
                //sendRequest(model.getMessageText());
                messageText.setText(model.getMessageText());
                messageUser.setText(model.getMessageUser());
                msg = model.getMessageText();
                LoaderManager loaderManager = getLoaderManager();
                loaderManager.initLoader(0, null, MainActivity.this);
                LinearLayout review_layout = v.findViewById(R.id.review_layout);
                if(flag==0){
                    review_layout.setVisibility(View.GONE);
                }
                else {
                    review_layout.setVisibility(View.VISIBLE);
                }
                // Format the date before showing it
                messageTime.setText(DateFormat.format("HH:mm",
                        model.getMessageTime()));
                RelativeLayout layout = v.findViewById(R.id.message_layout);

                // Set their text
                if(model.getMessageUser().equals(mFirebaseAuth.getCurrentUser().getDisplayName())){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        layout.setBackground(getDrawable(R.drawable.round_1));
                        messageText.setTextColor(getColor(R.color.white));
                        messageUser.setTextColor(getColor(R.color.white));
                        messageTime.setTextColor(getColor(R.color.white));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layout.getLayoutParams();
                        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        layout.setLayoutParams(params);
                    }
                }
                else {
                    layout.setBackground(getDrawable(R.drawable.round_2));
                    messageText.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    messageUser.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        messageText.setTextColor(getColor(R.color.black));
                    }
                }
            }
        };

        listOfMessages.setAdapter(adapter);
        listOfMessages.setSelection(adapter.getCount() - 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this,
                        "Successfully signed in. Welcome!",
                        Toast.LENGTH_LONG)
                        .show();
                displayChatMessages();
            } else {
                Toast.makeText(this,
                        "We couldn't sign you in. Please try again later.",
                        Toast.LENGTH_LONG)
                        .show();

                // Close the app
                finish();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.sign_out:
                mFirebaseAuth.signOut();
                Toast.makeText(MainActivity.this, "You have been signed out.", Toast.LENGTH_LONG).show();
                recreate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new ChatLoader(MainActivity.this, url, msg);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if(!data.isEmpty()){
            if(Integer.parseInt(data)<5){
                flag = 1;
            }
            else {
                flag = 0;
            }
            displayChatMessages();
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }
}
