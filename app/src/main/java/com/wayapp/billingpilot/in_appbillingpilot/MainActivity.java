package com.wayapp.billingpilot.in_appbillingpilot;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import com.wayapp.billingpilot.in_appbillingpilot.util.IabHelper;
import com.wayapp.billingpilot.in_appbillingpilot.util.IabResult;
import com.wayapp.billingpilot.in_appbillingpilot.util.Inventory;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Debug tag, for logging
    static final String TAG = "In-AppBillingPilot";

    static final String SKU_100 = "test_buy_100";
    static final String SKU_500 = "test_buy_500";

    TextView cost100Credits;
    TextView cost500Credits;
    TextView actualCredits;

    IabHelper mHelper;

    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        @Override
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                // handle error
                return;
            }

            String price100Credits =
                    inventory.getSkuDetails(SKU_100).getPrice() + " EUR";
            String price500Credits =
                    inventory.getSkuDetails(SKU_500).getPrice() + " EUR";

            cost100Credits.setText(price100Credits);
            cost500Credits.setText(price500Credits);

        }
    };
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cost100Credits = (TextView) findViewById(R.id.cost100Credits);
        cost500Credits = (TextView) findViewById(R.id.cost500Credits);

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArxCSiBvvQK3iK0p7Px0G8nW2lMPM3pBweAJpugtbvbsAlfWozIJis8mYuXv4DTPSVHaRktVUzg15mzlZPKRs+87oWcTN1J2NkaLeU5Wkpl8/6IdDIkBlDsg3h7DveJdt6tMinZOSHsN566IuUSzo5O1MyCkTxUA3O7jAfDYQZwqFeIHy3w9RB91N8T8Bl7dImhlAD68hnxSpL96fLPulDmFS/iEV7K1PGONos2eDEY+rOSaqY0zbIx/qvi4e1yC19RpzzdnFKLRmoVEqsESX3p2haBnYHi84q36ezKGW7qnz9G0m7ZzkobrgCOYJjYZhhuycLJYAj7rZeoa8Jt/lowIDAQAB";

        // compute your public key and store it in base64EncodedPublicKey

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem.
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                // Hooray, IAB is fully set up!
            }
        });

        List additionalSkuList = new ArrayList();
        additionalSkuList.add(SKU_100);
        additionalSkuList.add(SKU_500);

        try {
            mHelper.queryInventoryAsync(true, additionalSkuList, null, mQueryFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHelper != null) {
            try {
                mHelper.dispose();
            } catch (IabHelper.IabAsyncInProgressException e) {
                e.printStackTrace();
            }
        }
        mHelper = null;
    }
}
