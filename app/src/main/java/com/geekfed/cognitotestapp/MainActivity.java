package com.geekfed.cognitotestapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.regions.Regions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CognitoCachingCredentialsProvider mCredentialsProvider;

    private CognitoSyncManager mSyncClient;

    private static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCognitoCredentialsProvider();
        initCognitoSyncClient();
        createSampleRecordSetAndSyncWithCognito();
    }

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
}
