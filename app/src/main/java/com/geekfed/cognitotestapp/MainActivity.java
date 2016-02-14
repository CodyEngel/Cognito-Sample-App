package com.geekfed.cognitotestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private CallbackManager mFacebookCallbackManager;

    private CognitoCachingCredentialsProvider mCredentialsProvider;

    private CognitoSyncManager mSyncClient;

    private LoginButton fbLogin;

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCognitoCredentialsProvider();
        initCognitoSyncClient();
        initFacebookLogin();

        outputCognitoCredentials();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    // -- AWS Cognito Related Methods

    private void initCognitoCredentialsProvider() {
        mCredentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                getString(R.string.AWS_COGNITO_IDENTITY_POOL_ID), // YOUR Identity Pool ID from AWS Cognito
                Regions.US_EAST_1 // Region
        );
    }

    private void initCognitoSyncClient() {
        mSyncClient = new CognitoSyncManager(
                getApplicationContext(),
                Regions.US_EAST_1,
                mCredentialsProvider
        );
    }

    private void outputCognitoCredentials() {
        Log.i(TAG, "outputCognitoCredentials");
        Log.i(TAG, "getCachedIdentityId: " + mCredentialsProvider.getCachedIdentityId());
        Log.i(TAG, "getIdentityId: " + mCredentialsProvider.getIdentityId());
    }

    private void createSampleRecordSetAndSyncWithCognito() {
        Dataset dataset = mSyncClient.openOrCreateDataset("myDataset");
        dataset.put("myKey", "myValue");
        dataset.synchronize(new DefaultSyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List newRecords) {
                Log.i(TAG, "createSampleRecordSetAndSyncWithCognito onSuccess");
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

        fbLogin = (LoginButton) findViewById(R.id.fbLogin);
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

        Map<String, String> logins = new HashMap<>();
        logins.put("graph.facebook.com", facebookAccessToken.getToken());

        Log.i(TAG, "logins: " + logins.toString());

        mCredentialsProvider.setLogins(logins);
        refreshCredentialsProvider();
    }
}
