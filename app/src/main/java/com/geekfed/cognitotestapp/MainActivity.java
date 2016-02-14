package com.geekfed.cognitotestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.regions.Regions;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mFacebookCallbackManager;

    private CognitoCachingCredentialsProvider mCredentialsProvider;

    private CognitoSyncManager mSyncClient;

    private GoogleApiClient mGoogleApiClient;

    private GoogleSignInOptions mGoogleSignInOptions;

    private SignInButton gmsLogin;

    private static final int RC_SIGN_IN = 9001;

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCognitoCredentialsProvider();
        initCognitoSyncClient();
        initFacebookLogin();
        initGoogleLogin();

        outputCognitoCredentials();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleGoogleSignInResult(result);
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    // -- AWS Cognito Related Methods

    private void initCognitoCredentialsProvider() {
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                getString(R.string.AWS_COGNITO_IDENTITY_POOL_ID), // YOUR Identity Pool ID from AWS Cognito
                Regions.US_EAST_1 // Region
        );

        Log.i(TAG, "mCredentialsProvider: " + mCredentialsProvider.toString());
    }

    private void initCognitoSyncClient() {
        mSyncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1,
                mCredentialsProvider
        );

        Log.i(TAG, "mSyncClient: " + mSyncClient.toString());
    }

    private void outputCognitoCredentials() {
        Log.i(TAG, "outputCognitoCredentials");
        new Thread(new Runnable() {
           @Override
            public void run() {
               Log.i(TAG, "getCachedIdentityId: " + mCredentialsProvider.getCachedIdentityId());
               Log.i(TAG, "getIdentityId: " + mCredentialsProvider.getIdentityId());
           }
        }).start();
    }

    private void addDataToSampleDataset(String key, String value) {
        Dataset dataset = mSyncClient.openOrCreateDataset("SampleDataset");
        dataset.put(key, value);
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                Log.i(TAG, "addDataToSampleDataset onSuccess");
                Log.i(TAG, dataset.toString());
            }
        });
    }

    /**
     * <h2>refreshCredentialsProvider</h2>
     * <p>This calls the refresh() method for the AWS Credentials Provider on a background thread.
     * This should be called after updating login information (such as calling setLogins()).</p>
     */
    private void refreshCredentialsProvider() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCredentialsProvider.refresh();
            }
        }).start();
    }

    // -- Facebook SDK Related Methods
    private void initFacebookLogin() {
        if (mFacebookCallbackManager == null) {
            mFacebookCallbackManager = CallbackManager.Factory.create();
        }

        LoginButton fbLogin = (LoginButton) findViewById(R.id.fbLogin);
        fbLogin.setReadPermissions("user_friends");

        fbLogin.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.i(TAG, "fbLogin onSuccess");
                Log.i(TAG, "accessToken: " + loginResult.getAccessToken().getToken());
                addFacebookLoginToCognito(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "fbLogin onCancel");
            }

            @Override
            public void onError(FacebookException e) {
                Log.i(TAG, "FacebookException: " + e.toString());
            }
        });

    }

    private void addFacebookLoginToCognito(AccessToken facebookAccessToken) {
        Log.i(TAG, "addFacebookLoginToCognito");
        Log.i(TAG, "AccessToken: " + facebookAccessToken.getToken());

        addDataToSampleDataset("facebook_token", facebookAccessToken.getToken()); // please don't do this in a production app...

        Map<String, String> logins = mCredentialsProvider.getLogins();
        logins.put("graph.facebook.com", facebookAccessToken.getToken());
        Log.i(TAG, "logins: " + logins.toString());

        mCredentialsProvider.setLogins(logins);
        refreshCredentialsProvider();
    }

    // -- Google Sign-In Related Methods
    private void initGoogleLogin() {
        if(mGoogleSignInOptions == null) {
            mGoogleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.GOOGLE_SERVER_CLIENT_ID))
                    .build();
        }

        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {

                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, mGoogleSignInOptions)
                .build();
        }

        gmsLogin = (SignInButton) findViewById(R.id.gmsLogin);

        gmsLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    private void handleGoogleSignInResult(GoogleSignInResult result) {
        Log.i(TAG, "handleSignInResult: " + result.isSuccess());
        if(result.isSuccess()) {
            try {
                if(result.getSignInAccount() != null) {
                    addGoogleLoginToCognito(result.getSignInAccount().getIdToken());
                } else {
                    Log.i(TAG, "result.getSignInAccount is null.");
                }
            } catch (GoogleAuthException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addGoogleLoginToCognito(String token) throws GoogleAuthException, IOException {
        Log.i(TAG, "addGoogleLoginToCognito");
        Log.i(TAG, "token: " + token);

        addDataToSampleDataset("google_token", token); // please don't do this in a production app...

        Map<String, String> logins = mCredentialsProvider.getLogins();
        logins.put("accounts.google.com", token);
        Log.i(TAG, "logins: " + logins.toString());

        mCredentialsProvider.setLogins(logins);
        refreshCredentialsProvider();
    }

}
