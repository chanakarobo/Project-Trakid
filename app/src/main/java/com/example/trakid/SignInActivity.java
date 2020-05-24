package com.example.trakid;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class SignInActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private static final String TAG = "GoogleActivity";
    ConnectivityManager connectivityManager;

    ProgressDialog dialog;

    String username,email;
    String personPhoto;
    String uid;

    TextInputLayout textusername;
    TextInputLayout textpassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        connectivityManager = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Please wait....!");
        dialog.setMessage("Signin operation on progressing.!");
        dialog.setCanceledOnTouchOutside(false);

        textusername = findViewById(R.id.username);
        textpassword = findViewById(R.id.password);


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

    SignInButton signInButton=(SignInButton)findViewById(R.id.signInButton);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(connectivityManager.getActiveNetworkInfo()!=null) {
                  dialog.show();
                  signIn();
              }else{
                  showAlert();
              }
            }
        });

        Button loginbtn=findViewById(R.id.login_button);


        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(connectivityManager.getActiveNetworkInfo()!=null) {

                    if (!validateEmail() | !validatePassword()) {
                        return;
                    } else {
                        dialog.show();
                        String username = textusername.getEditText().getText().toString().trim();
                        String password = textpassword.getEditText().getText().toString().trim();

                        Login(username, password);
                    }
                }else{
                    showAlert();
                }
            }
        });



    }

    private boolean validateEmail(){

        String emailInput=textusername.getEditText().getText().toString().trim();
        if(emailInput.isEmpty()){
            textusername.setError("Fild can't be empty");
            return false;
        }else{
            textusername.setError(null);
            return true;
        }


    }

    private boolean validatePassword(){
        String passwordInput=textpassword.getEditText().getText().toString().trim();

        if(passwordInput.isEmpty()){
            textpassword.setError("Fild can't be empty");
            return false;
        }else if(passwordInput.length()>6){
            textpassword.setError("Username too long");
            return false;
        }else{
            textpassword.setError(null);
            return true;
        }

    }





    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void signIn() {

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]

        // [END_EXCLUDE]
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }


                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
    }


    private void Login(String email, String password){

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }


                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            Toast.makeText(SignInActivity.this, "Signing failed.", Toast.LENGTH_SHORT).show();
                        }

                        // [END_EXCLUDE]
                    }
                });



    }

    private void updateUI(FirebaseUser user) throws NullPointerException{

        try{

            if (user != null) {

                String check=user.getPhotoUrl().toString();


                    email = user.getEmail();
                    uid = user.getUid();
                    username = user.getDisplayName();
                    email = user.getEmail();
                    personPhoto = user.getPhotoUrl().toString();
                    uid = user.getUid();


//       Log.e("username is : ",username);
                Log.e("email is : ",email);
//       Log.e("photo uri is : ",personPhoto.toString());
                Log.e("personUid : ",uid);
                Intent intent=new Intent(this,MainActivity.class);
                intent.putExtra(MainActivity. EXTRA_URL,personPhoto);
                intent.putExtra(MainActivity.EXTRA_USERNAME,username);
                intent.putExtra(MainActivity.EXTRA_EMAIL,email);
                intent.putExtra(MainActivity.EXTRA_UID,uid);
                startActivity(intent);


            } else {
               return;
            }

        }catch(NullPointerException ex){
            email = user.getEmail();
            uid = user.getUid();

            Intent intent=new Intent(this,MainActivity.class);
            intent.putExtra(MainActivity. EXTRA_URL,personPhoto);
            intent.putExtra(MainActivity.EXTRA_USERNAME,username);
            intent.putExtra(MainActivity.EXTRA_EMAIL,email);
            intent.putExtra(MainActivity.EXTRA_UID,uid);
            startActivity(intent);

        }


    }

    public void signOut() {
        FirebaseAuth.getInstance().signOut();
    }

    public void showAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(SignInActivity.this).create();
        alertDialog.setTitle("Internet connection...");
        alertDialog.setMessage("Your network connection is lost..!");
        alertDialog.setIcon(R.drawable.wifi_signal);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivity(callGPSSettingIntent);
                    }
                });

        alertDialog.show();
        alertDialog.setCanceledOnTouchOutside(false);
    }

}
