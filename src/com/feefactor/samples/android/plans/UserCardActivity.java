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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.feefactor.FeefactorCheckedException;
import com.feefactor.paymentsystems.CardPayments;
import com.feefactor.paymentsystems.UserCard;
import com.feefactor.samples.android.BaseActivity;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class UserCardActivity extends BaseActivity {
    private static final String TAG = "UCA";
    
    public static final int ACTIVITY_CREDITCARDPAYMENTACTIVITY = 1001213;
    
    private Button submitBtn;
    private Spinner creditCardType;
    private Spinner expirationMonth;
    private Spinner expirationYear;
    private EditText creditCardNumber;
    private EditText emailEt;
    private EditText firstName;
    private EditText lastName;
    private EditText street;
    private EditText city;
    private Spinner country;
    private EditText state;
    private EditText zipCode;
    private EditText phone;
    private ProgressableRunnable viewCreditCard;
    
    protected void initDisplay() {
        setContentView(R.layout.usercard);
        
        submitBtn = (Button) findViewById(R.id.cc_submit_button);
        
        creditCardType = (Spinner) findViewById(R.id.cc_type);
        creditCardNumber = (EditText) findViewById(R.id.cc_creditcardnumber);
        expirationMonth = (Spinner) findViewById(R.id.cc_expiration_month);
        expirationYear = (Spinner) findViewById(R.id.cc_expiration_year);
        emailEt = (EditText) findViewById(R.id.cc_email);
        firstName = (EditText) findViewById(R.id.cc_firstname);
        lastName = (EditText) findViewById(R.id.cc_lastname);
        street = (EditText) findViewById(R.id.cc_street);
        city = (EditText) findViewById(R.id.cc_city);
        country = (Spinner) findViewById(R.id.cc_country);
        state = (EditText) findViewById(R.id.cc_state);
        zipCode = (EditText) findViewById(R.id.cc_zipcode);
        phone = (EditText) findViewById(R.id.cc_phone);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.cc_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        creditCardType.setAdapter(adapter);
        
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(
                this, R.array.months_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expirationMonth.setAdapter(adapter2);
        
        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(
                this, R.array.exp_year_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        expirationYear.setAdapter(adapter3);
        
        ArrayAdapter<CharSequence> adapter4 = ArrayAdapter.createFromResource(
                this, R.array.countries_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        country.setAdapter(adapter4);
        
        // Set Click Listener
        submitBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                submitClicked();
            }
        });
        
        creditCardNumber.setText("9999"+System.currentTimeMillis()+9);
    }
        
    private void submitClicked() {
        String creditCardNumber;
        String creditCardType;
        String creditCardExpirationMonth;
        String creditCardExpirationYear;
        String email;
        String firstName;
        String lastName;
        String street;
        String city;
        String country;
        String state;
        String zipCode;
        String phone;
        
        creditCardNumber = this.creditCardNumber.getText().toString().trim();
        creditCardType = this.creditCardType.getSelectedItem().toString().trim();
        creditCardExpirationMonth = this.expirationMonth.getSelectedItem().toString().trim();
        creditCardExpirationYear = this.expirationYear.getSelectedItem().toString().trim();
        email = this.emailEt.getText().toString().trim();
        firstName = this.firstName.getText().toString().trim();
        lastName = this.lastName.getText().toString().trim();
        street = this.street.getText().toString().trim();
        city = this.city.getText().toString().trim();
        phone = this.phone.getText().toString().trim();
        country = this.country.getSelectedItem().toString().trim();
        state = this.state.getText().toString().trim();
        zipCode = this.zipCode.getText().toString().trim();
        
        if (creditCardNumber.length() == 0 
                || creditCardExpirationMonth.length() == 0 || creditCardExpirationYear.length() == 0
                || email.length() == 0 || firstName.length() == 0 || lastName.length() == 0
                || street.length() == 0 || city.length() == 0 || country.length() == 0
                || state.length() == 0 || zipCode.length() == 0 || phone.length() == 0
                || creditCardType.length() == 0) {
            // TODO GC
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Required fields are missing!")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                   }
               });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        
        try {
            viewCreditCard = new ProgressableRunnable() {
                public void run() {
                    createRecord();
                }

                public void onCancel() {
                }
            };
            ProgressableTask task = new ProgressableTask(this, viewCreditCard,
                    R.string.progress_save);
            task.start();
            
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("There was in error in saving record. Please try again later! " + e.getMessage())
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                   }
               });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
    }
    
    public void createRecord() {
        String creditCardNumber;
        String creditCardType;
        String creditCardExpirationMonth;
        String creditCardExpirationYear;
        String email;
        String firstName;
        String lastName;
        String street;
        String city;
        String country;
        String state;
        String zipCode;
        String phone;
        
        creditCardNumber = this.creditCardNumber.getText().toString().trim();
        creditCardType = this.creditCardType.getSelectedItem().toString().trim();
        creditCardExpirationMonth = this.expirationMonth.getSelectedItemPosition()+1+"";
        creditCardExpirationYear = this.expirationYear.getSelectedItem().toString().trim();
        email = this.emailEt.getText().toString().trim();
        firstName = this.firstName.getText().toString().trim();
        lastName = this.lastName.getText().toString().trim();
        street = this.street.getText().toString().trim();
        city = this.city.getText().toString().trim();
        phone = this.phone.getText().toString().trim();
        country = this.country.getSelectedItem().toString().trim();
        state = this.state.getText().toString().trim();
        zipCode = this.zipCode.getText().toString().trim();
        
        QuickstartApplication qsApp = (QuickstartApplication) getApplication();
        
        CardPayments cpUtility = qsApp.getCardPaymentUtility();
        Log.d(TAG, "Saving...");
        
        UserCard card = new UserCard();
        card.setAddress1(street);
        card.setCardNumber(creditCardNumber);
        card.setIssuer(creditCardType);
        card.setExpiryMonth(creditCardExpirationMonth);
        card.setExpiryYear(creditCardExpirationYear);
        card.setEmailAddress(email);
        card.setFirstName(firstName);
        card.setLastName(lastName);
        card.setCity(city);
        card.setCountry(country);
        card.setState(state);
        card.setZipcode(zipCode);
        card.setPhoneNumber(phone);
        
        card.setUserID(qsApp.getLoggedUser().getUserID());
        card.setBrandID(qsApp.getLoggedUser().getBrandID());
        
        try {
            cpUtility.insertUserCard(card, "created via mobile");
            sendMessage(SUCCESS);
        } catch (FeefactorCheckedException e) {
            e.printStackTrace();
            sendMessage(FAILED);
        }
        finish();
    }
}
