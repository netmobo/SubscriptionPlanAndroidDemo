/*-
 * Copyright (c) 2010, NETMOBO LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *     i.   Redistributions of source code must retain the above copyright 
 *          notice, this list of conditions and the following disclaimer.
 *     ii.  Redistributions in binary form must reproduce the above copyright 
 *          notice, this list of conditions and the following disclaimer in the 
 *          documentation and/or other materials provided with the 
 *          distribution.
 *     iii. Neither the name of NETMOBO LLC nor the names of its contributors 
 *          may be used to endorse or promote products derived from this 
 *          software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.feefactor.samples.android.plans;

import com.feefactor.accounts.Account;
import com.feefactor.paymentsystems.UserCard;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.subscriber.User;
import com.feefactor.subscriber.Users;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * @author netmobo
 */
public class InputAmountActivity extends Activity {
    private static final String TAG = "INPUTAMOUNTACTIVITY";

    public static final int ACTIVITY_RECHARGE_ACCOUNT = 1;
    public static final int ACTIVITY_SETUP_ACCOUNT = 2;

    private Account activeAccount;
    private EditText amountInput;
    private Spinner mPaymentMode;
    private UserCard activeCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.amount_input);
        initDisplay();
    }

    public void initDisplay() {
        TextView screenTitle = (TextView) findViewById(R.id.screen_title);
        TextView textView1 = (TextView) findViewById(R.id.textview1);
        TextView textView1B = (TextView) findViewById(R.id.textview1b);

        TextView textView2 = (TextView) findViewById(R.id.textview2);
        amountInput = (EditText) findViewById(R.id.edittext2);

        TextView accountOwner = (TextView) findViewById(R.id.amount_input_username);
        TextView accountSerialNumber = (TextView) findViewById(R.id.amount_input_serialnumber);

        Button backBtn = (Button) findViewById(R.id.back_button);
        Button googleCheckout = (Button) findViewById(R.id.google_checkout);
        mPaymentMode = (Spinner) findViewById(R.id.payment_modes);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.recharge_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPaymentMode.setAdapter(adapter);

        backBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                backBtnClicked();
            }
        });
        googleCheckout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                googleCheckoutBtnClicked();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        int layout = extras.getInt(InputAmountActivity.class.getCanonicalName()+".layout");
        
        activeAccount = (Account) extras.getSerializable(InputAmountActivity.class.getCanonicalName()+".selectedAccount");
        activeCard = (UserCard) extras.getSerializable(InputAmountActivity.class.getCanonicalName()+".selectedCard");
        Users userUtil = QuickstartApplication.getApplication().getUserUtility();
        User owner;
        try {
            owner = userUtil.getUser(activeAccount.getUserID());
        } catch (Exception e) {
            owner = null;
        }
        if(owner!=null){
            accountOwner.setText(owner.getUsername());
        }
        accountSerialNumber.setText(String.valueOf(activeAccount.getSerialNumber()));
        
        //TODO: USD is not always the case... fix symbol
        switch (layout) {
            // externalize
            case ACTIVITY_RECHARGE_ACCOUNT:
                screenTitle.setText("Add Money to Balance");
                textView1.setText("Current Balance:");
                textView2.setText("Amount to Add:");
                textView1B.setText("$ " + (activeAccount.getBalance()+activeAccount.getCreditLimit()));
                break;
            case ACTIVITY_SETUP_ACCOUNT:
                screenTitle.setText("Load Balance to Account");
                textView1.setText("Current Balance:");
                textView2.setText("Amount to Pay:");
                textView1B.setText("$ " + (activeAccount.getBalance()+activeAccount.getCreditLimit()));
                break;
        }
    }

    public void backBtnClicked() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        int layout = extras.getInt("layout");
        if (layout == ACTIVITY_SETUP_ACCOUNT) {
            QuickstartApplication qsApp = QuickstartApplication.getApplication();
            // need to logout
            qsApp.logout();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (layout == ACTIVITY_RECHARGE_ACCOUNT) {
            finish();
        }
    }

    public void googleCheckoutBtnClicked() {
        if (amountInput.getText() == null || amountInput.getText().toString().trim().length()<1) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Please Enter A Valid Amount!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                   }
               });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        Log.d(TAG, "Payment Mode: " + mPaymentMode.getSelectedItem().toString());
        
        if (mPaymentMode.getSelectedItem().toString().equals("Google Checkout")) {
            Intent intent = new Intent(this, GoogleCheckoutActivity.class);
            Log.d(TAG, "Amount entered: " + amountInput.getText());
            // need to get String. It seems getText returns a SpannableString
            intent.putExtra(GoogleCheckoutActivity.class.getCanonicalName()+".unitPriceAmount", amountInput.getText().toString());
            intent.putExtra(GoogleCheckoutActivity.class.getCanonicalName()+".selectedAccount", activeAccount);

            // pass through
            intent.putExtras(getIntent().getExtras());
            startActivityForResult(intent, GoogleCheckoutActivity.ACTIVITY_GOOGLECHECKOUT);
        } else if (mPaymentMode.getSelectedItem().toString().equals("Credit Card")) {
            Intent intent = new Intent(this, CreditCardPaymentActivity.class);
            Log.d(TAG, "Amount entered: " + amountInput.getText());
            // need to get String. It seems getText returns a SpannableString
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".unitPriceAmount", amountInput.getText().toString());
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedAccount", activeAccount);
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedCard", activeCard);
    
            // pass through
            intent.putExtras(getIntent().getExtras());
            startActivityForResult(intent, CreditCardPaymentActivity.ACTIVITY_CREDITCARDPAYMENTACTIVITY);
        }
    }
    
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
            case CreditCardPaymentActivity.ACTIVITY_CREDITCARDPAYMENTACTIVITY:
                finish();
                break;
            case GoogleCheckoutActivity.ACTIVITY_GOOGLECHECKOUT:
                handleGoogle(resultCode, data);
                break;
            default:
                break;
        }
    }
    
    private void handleGoogle(int resultCode, Intent data) {
        switch (resultCode) {
        case RESULT_CANCELED:
            // do nothing. just go back inputting amount
            Log.d(TAG, "Got back");
            break;
        case RESULT_OK:
            finish();
            break;
        }
    }
}
