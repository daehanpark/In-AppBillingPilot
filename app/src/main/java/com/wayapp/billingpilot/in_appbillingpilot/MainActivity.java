package com.wayapp.billingpilot.in_appbillingpilot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.wayapp.billingpilot.in_appbillingpilot.util.IabHelper;
import com.wayapp.billingpilot.in_appbillingpilot.util.IabResult;
import com.wayapp.billingpilot.in_appbillingpilot.util.Inventory;
import com.wayapp.billingpilot.in_appbillingpilot.util.Purchase;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Debug tag, for logging
    static final String TAG = "In-AppBillingPilot";

    static final String SKU_100 = "test_buy_100";
    static final String SKU_500 = "test_buy_500";

    private TextView cost100Credits;
    private TextView cost500Credits;
    private TextView actualCredits;
    private Button checkPrices;
    private Button buy100Credits;
    private Button buy500Credits;
    private  int credits = 0;

    private IabHelper mHelper;

    private IabHelper.QueryInventoryFinishedListener
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

    private IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d(TAG, "Error purchasing: " + result);
                return;
            }
            else if (purchase.getSku().equals(SKU_100)) {
                Toast.makeText(getApplicationContext(), "You were charged for 100 credits ", Toast.LENGTH_LONG).show();
                credits += 100;
                actualCredits.setText(String.valueOf(credits));
            }
            else if (purchase.getSku().equals(SKU_500)) {
                Toast.makeText(getApplicationContext(), "You were charged for 500 credits ", Toast.LENGTH_LONG).show();
                credits += 500;
                actualCredits.setText(String.valueOf(credits));
            }
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cost100Credits = (TextView) findViewById(R.id.cost100Credits);
        cost500Credits = (TextView) findViewById(R.id.cost500Credits);
        actualCredits = (TextView) findViewById(R.id.actualCredits);

        checkPrices = (Button) findViewById(R.id.checkPricesButton);
        buy100Credits = (Button) findViewById(R.id.buy100Button);
        buy500Credits = (Button) findViewById(R.id.buy500Button);

        checkPrices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPrices();
            }
        });

        buy100Credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy100CreditsAction();
            }
        });

        buy500Credits.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buy500CreditsAction();
            }
        });

        String base64EncodedPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEArxCSiBvvQK3iK0p7Px0G8nW2lMPM3pBweAJpugtbvbsAlfWozIJis8mYuXv4DTPSVHaRktVUzg15mzlZPKRs+87oWcTN1J2NkaLeU5Wkpl8/6IdDIkBlDsg3h7DveJdt6tMinZOSHsN566IuUSzo5O1MyCkTxUA3O7jAfDYQZwqFeIHy3w9RB91N8T8Bl7dImhlAD68hnxSpL96fLPulDmFS/iEV7K1PGONos2eDEY+rOSaqY0zbIx/qvi4e1yC19RpzzdnFKLRmoVEqsESX3p2haBnYHi84q36ezKGW7qnz9G0m7ZzkobrgCOYJjYZhhuycLJYAj7rZeoa8Jt/lowIDAQAB";
        // compute your public key and store it in base64EncodedPublicKey

        mHelper = new IabHelper(this, base64EncodedPublicKey);
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    // Oh no, there was a problem
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                    return;
                }

                // Hooray, IAB is fully set up!
            }
        });
    }

    private void buy100CreditsAction() {
        try {
            mHelper.launchPurchaseFlow(this, SKU_100, 10001, mPurchaseFinishedListener, "100-credits-buy");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void buy500CreditsAction() {
        try {
            mHelper.launchPurchaseFlow(this, SKU_500, 10001, mPurchaseFinishedListener, "500-credits-buy");
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    private void checkPrices() {
        List skuList = new ArrayList<String>();
        skuList.add(SKU_100);
        skuList.add(SKU_500);

        try {
            mHelper.queryInventoryAsync(true, skuList, null, mQueryFinishedListener);
        } catch (IabHelper.IabAsyncInProgressException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Pass on the activity result to the helper for handling
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            // not handled, so handle it ourselves (here's where you'd
            // perform any handling of activity results not related to in-app
            // billing...
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.i(TAG, "onActivityResult handled by IABUtil.");
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
