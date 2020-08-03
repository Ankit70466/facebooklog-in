package com.example.mycode;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.inputmethod.InputConnectionCompat;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mcallbackManager;
    private FirebaseAuth mfirebaseAuth;
    private TextView mtag;
    private ImageView mimg;
    private Button loginbutton;

    private  static final  String TAG ="FacebookAuthentication";
    private FirebaseAuth.AuthStateListener authStateListener ;//log in to log out
    private AccessTokenTracker accessTokenTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //fire base instance sdk -fb
        mfirebaseAuth=FirebaseAuth.getInstance();
        FacebookSdk.sdkInitialize(getApplicationContext());

        //find
        mtag=findViewById(R.id.tagline);
        mimg=findViewById(R.id.logo);
        loginbutton=findViewById(R.id.login_button);


        mcallbackManager=CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(mcallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "onSuccess:" + loginResult);
                handleFacebookToken(loginResult.getAccessToken());

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "oncancle");

            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "error"+error);

            }
        });

        //change log in to log out
        authStateListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user=firebaseAuth.getCurrentUser();
                if(user!=null){
                    updateUi(user);//log inscreen

                }
                else {
                    updateUi(null);
                }
            }
        };

        accessTokenTracker=new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if(currentAccessToken ==null){
                    mfirebaseAuth.signOut();


                }
            }
        };

    }

    private void  handleFacebookToken(AccessToken token){
        Log.d(TAG, "handleFacebookToken"+token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mfirebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.d(TAG, "login sucessfull");
                    FirebaseUser user=mfirebaseAuth.getCurrentUser();
                    updateUi(user);

                }
                else {
                    Log.d(TAG, "login unsucessfull",task.getException());
                    Toast.makeText(MainActivity.this, "login faild", Toast.LENGTH_SHORT).show();
                    updateUi(null);


                }

            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mcallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private  void updateUi(FirebaseUser user){

        if(user !=null){
            mtag.setText(user.getDisplayName());
            if(user.getPhoneNumber() !=null){
                String photourl =user.getPhotoUrl().toString();
                photourl=photourl +"?tyep=large";
                Picasso.get().load(photourl).into(mimg);


            }

        }
        else{
            mtag.setText("");
            mimg.setImageResource(R.drawable.an);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mfirebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener !=null){

            mfirebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}