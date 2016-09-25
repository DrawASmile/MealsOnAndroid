package org.drawsmile.mealsonandroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Map;

public class AuthenticationActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private Context conte;

    private View currentView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        Firebase.setAndroidContext(this);
        conte = this;

        currentView = findViewById(android.R.id.content);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("911728225506-u3pp4nlmqaplqohvnrug49l4sfqbuoq8.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("drawsmileauth", "onAuthStateChanged:signed_in:" + user.getUid());
                    MainActivity.signedIn = true;
                    MainActivity.uid = user.getUid();

                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
                    editor.putString("firebaseUID", user.getUid());

                    editor.apply();


                    //Reopen the main activity
                    Intent myIntent = new Intent(conte, MainActivity.class);
                    conte.startActivity(myIntent);


                    FirebaseAuth.getInstance().signOut();
                } else {
               /*     // User is signed out
                    MainActivity.signedIn = false;
                    MainActivity.uid = "";*/
                    Log.d("drawsmileauth", "onAuthStateChanged:signed_out");
                }

            }
        };

        findViewById(R.id.sign_in_button).setOnClickListener(this);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        sayToast("Google APIs connection failed (unresolvable error)");
        Log.d("drawsmileauth", "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    public void signInPhone(View view)
    {
        EditText phone = (EditText) currentView.findViewById(R.id.field_phoneNumberE);

        String phoneStr = phone.getText().toString().trim().replace(" ", "").replace("-", "");

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.putString("phoneNumber", phoneStr);

        editor.apply();

        //Reopen the main activity
        Intent myIntent = new Intent(conte, MainActivity.class);
        conte.startActivity(myIntent);
    }

    public void signInEmail(View view)
    {
        EditText email = (EditText) currentView.findViewById(R.id.field_email);
        EditText password = (EditText) currentView.findViewById(R.id.field_password);

        String emailStr = email.getText().toString();
        String passStr = password.getText().toString();

        mAuth.signInWithEmailAndPassword(emailStr, passStr)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("drawsmileauth", "signInWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("drawsmileauth", "signInWithEmail:failed", task.getException());
                            Toast.makeText(AuthenticationActivity.this, "Login failed!",
                                    Toast.LENGTH_SHORT).show();
                        }


                    }
                });

    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
//        Log.d("drawsmileauth", "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("drawsmileauth", "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w("drawsmileauth", "signInWithCredential", task.getException());
                            sayToast("Firebase authentication failed");
                        }

                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    private void signIn() {
        mGoogleApiClient.clearDefaultAccountAndReconnect();
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);

            GoogleSignInAccount account = result.getSignInAccount();
            if(account == null)
            {
                sayToast("GoogleSignInAccount is null");
                return;
            }
            else
            {
                sayToast("Attempting authentication with google");
            }
            firebaseAuthWithGoogle(account);
        }
    }

    public static void googleSignOut()
    {
        FirebaseAuth.getInstance().signOut();
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("drawsmiledebug", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            sayToast("Google sign-in was successful");

        } else {

            sayToast("Google sign-in failed!");

        }
    }


    public void sayToast(String message)
    {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void registerEmail(View view)
    {
        EditText email = (EditText) currentView.findViewById(R.id.field_email);
        EditText password = (EditText) currentView.findViewById(R.id.field_password);

        String emailText = email.getText().toString().trim();
        emailText.replace(" ", "");

        String passwordText = password.getText().toString().trim();



        if(emailText.equals("") || !emailText.contains("@") || !emailText.contains("."))
        {
            sayToast("Invalid email entered");
            return;
        }
        if(passwordText.equals(""))
        {
            sayToast("Invalid password entered");
            return;
        }

        mAuth.createUserWithEmailAndPassword(emailText, passwordText)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d("drawsmileauth", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(AuthenticationActivity.this, "Authentication failed!",
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });


    }


}
