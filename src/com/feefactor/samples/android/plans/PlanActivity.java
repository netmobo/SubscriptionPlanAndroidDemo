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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.feefactor.FeefactorCheckedException;
import com.feefactor.accounts.Account;
import com.feefactor.accounts.AccountPlan;
import com.feefactor.accounts.Accounts;
import com.feefactor.accounts.Plan;
import com.feefactor.accounts.PlanRC;
import com.feefactor.accounts.Plans;
import com.feefactor.paymentsystems.CardPayments;
import com.feefactor.paymentsystems.CardTransactionHistory;
import com.feefactor.paymentsystems.UserCard;
import com.feefactor.samples.android.BaseActivity;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class PlanActivity extends BaseActivity{
    private EditText schedule;
    private TextView description, planCharge, freeValue, recurringCharges;
    private Plan selectedPlan;
    private static Plan trial;
    private Account selectedAccount;
    private NumberFormat nf = NumberFormat.getInstance();
    
    private View pickerLabel;
    private TextView activeCardDisplay;
    private UserCard activeCard;
    private volatile boolean paid;
    private Calendar lastSelectedTime = Calendar.getInstance();

    private CardPayments cardUtil = QuickstartApplication.getApplication().getCardPaymentUtility();
    
    private static final int ADDCARD = 0xF00F00;
    
    @Override
    protected void initDisplay() {
        setContentView(R.layout.plan);
        description = (TextView)findViewById(R.id.plan_description);
        planCharge = (TextView)findViewById(R.id.plan_planCharge);
        freeValue = (TextView)findViewById(R.id.plan_freeValue);
        schedule = (EditText)findViewById(R.id.plan_schedule);
        recurringCharges = (TextView)findViewById(R.id.plan_recurringCharges);
        
        pickerLabel = findViewById(R.id.label1);
        activeCardDisplay = (TextView)findViewById(R.id.plan_usercard);
        
        final Activity act = this;
        Button now = (Button)findViewById(R.id.plan_now);
        now.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ProgressableRunnable runnable = new ProgressableRunnable() {
                    public void run() {
                        try {
                            subscribe();
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(FAILED);
                        }
                    }

                    public void onCancel() {
                    }
                };
                
                ProgressableTask task = new ProgressableTask(act, runnable
                        , R.string.progress_plan, R.string.progress_wait, -1);
                task.start();
            }
        });
        Button later = (Button)findViewById(R.id.plan_later);
        later.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.HOUR_OF_DAY, 0);

                try {
                    String date = schedule.getText().toString();
                    
                    if(date==null || date.length()<1){
                        throw new Exception("Date is empty.");
                    }
                    String[] parts = date.split("/");
                    cal.set(Calendar.MONTH, new Integer(parts[0]));
                    cal.set(Calendar.DAY_OF_MONTH, new Integer(parts[1]));
                    cal.set(Calendar.YEAR, new Integer(parts[2]));
                } catch (Exception e){
                    AlertDialog.Builder builder = new AlertDialog.Builder(act);
                    builder.setMessage("Please specify a target date.")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                           }
                       });
                    AlertDialog alert = builder.create();
                    alert.show();
                    return;
                }
                
                final Calendar f_cal = cal;
                ProgressableRunnable runnable = new ProgressableRunnable() {
                    public void run() {
                        try {
                            subscribe(f_cal);
                        } catch (Exception e) {
                            e.printStackTrace();
                            sendMessage(FAILED);
                        }
                    }

                    public void onCancel() {
                    }
                };
                
                ProgressableTask task = new ProgressableTask(act, runnable
                        , R.string.progress_plan, R.string.progress_wait, -1);
                task.start();
            }
        });
        
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(6);
        nf.setMinimumIntegerDigits(1);
        
        addDatePicker(schedule);
        addListPicker(activeCardDisplay, SelectCardActivity.class);
        
        ImageButton addCard = (ImageButton) findViewById(R.id.plan_addcard);
        addCard.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(act, UserCardActivity.class);
                startActivityForResult(intent, ADDCARD);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();
        selectedPlan = (Plan)intent.getSerializableExtra(PlanActivity.class.getCanonicalName()+".selectedPlan");
        selectedAccount = (Account)intent.getSerializableExtra(PlanActivity.class.getCanonicalName()+".selectedAccount");
        
        if(selectedPlan==null){
            Log.i("PA","No plan selected?!");
            finish();
            return;
        } else {
            description.setText(selectedPlan.getDescription());
            planCharge.setText(nf.format(selectedPlan.getPlanCharge()));
            
            double monthly = 0;
            try {
                QuickstartApplication qa = QuickstartApplication.getApplication();
                List<PlanRC> prcs = qa.getPlanUtility().getPlanRCs(selectedPlan.getPlanID(), "", "", 10, 1);
                for(PlanRC prc : prcs){
                    monthly += prc.getAmount();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            recurringCharges.setText(nf.format(monthly));
            
            switch(new Long(selectedPlan.getPlanType()).intValue()){
                case 1 :
                    freeValue.setText(nf.format(selectedPlan.getFreeValue()));
                    break;
                case 4 :
                default:
                    freeValue.setText(String.valueOf(selectedPlan.getFreeValue()));
                    break;
            }
        }
        
        ProgressableTask task = new ProgressableTask(this, new ProgressableRunnable() {
            public void run() {
                updateCardDisplay();
            }
            
            public void onCancel() {
            }
        }, R.string.progress_userhome_fill, R.string.progress_wait, -1);
        task.start();
    }
    
    private void subscribe(){
        subscribe(Calendar.getInstance());
    }
    
    private void subscribe(final Calendar time){
        lastSelectedTime = time;
        try {
            /*-
             * 1. check if there is a selectedCard. should be. d na dapat umabot dito eh.
             * 2. check if able to recharge using the card with amount totalling plan.signup and rcs
             * 3. subscribe if #2 succeeds..
             * 4. limit trial to one
             */
            QuickstartApplication pqa = QuickstartApplication.getApplication();
            Plans planUtility = pqa.getPlanUtility();
            if(trial==null){
                List<Plan> plans = planUtility.getPlans("upper(description) like '%TRIAL%'", "", 1, 1);
                if(plans!=null && !plans.isEmpty()){
                    trial = plans.get(0);
                } else {
                    trial = new Plan();
                }
            }
            
            //checking previous trial attempts
            StringBuffer sb = new StringBuffer();
            sb.append("planid=").append(trial.getPlanID());
            
            Accounts accountUtil = pqa.getAccountUtility();
            if(trial.getPlanID()==selectedPlan.getPlanID() && accountUtil.getAccountPlansCount(selectedAccount.getSerialNumber(), sb.toString())>0){
                sendMessage(FAILED);
                Log.i("PA","No more trial!");
                finish();
                return;
            }
            
            if(!paid){
                double finalAmount = selectedPlan.getPlanCharge();
                
                //NOTE: use count to make sure all to loop across all RCs. Current assumption is there is less than 10.
                List<PlanRC> rcs = planUtility.getPlanRCs(selectedPlan.getPlanID(), "", "", 10, 1);
                if(rcs!=null && !rcs.isEmpty()){
                    for(PlanRC rc : rcs){
                        finalAmount += rc.getAmount();
                    }
                }
                
                if(finalAmount>0){
                    Intent intent = new Intent(this, CreditCardPaymentActivity.class);
                    /*-
                     * NOTE: There are several assumptions here:
                     * 
                     * 1. Quickstart plans are all of type UNIT (plan.planType=2)
                     * 2. Once account.freeValueBal is depleted, regular prices are used
                     * 3. Scheduled prices are NOT considered here.
                     */
                    intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".unitPriceAmount", ""+finalAmount);
                    intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedCard", activeCard);
                    intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedAccount", selectedAccount);
                    startActivityForResult(intent, RECHARGE);
                    return;
                }
            }
            
            AccountPlan ap = new AccountPlan();
            ap.setAmount(selectedPlan.getPlanCharge());
            ap.setDescription(selectedPlan.getDescription());
            ap.setEffectivityDate(time);
            ap.setSerialNumber(selectedAccount.getSerialNumber());
            ap.setPlanID(selectedPlan.getPlanID());
            
            accountUtil.scheduleSubscription(ap, Accounts.MODE_PARTIAL, true, true, "subscibed via mobile.");
            
            sendMessage(SUCCESS);
            setResult(RESULT_OK);
        } catch (Exception e) {
            e.printStackTrace();
            sendMessage(FAILED);
        }
        Log.i("PA","end of subscribe method.");
        finish();
    }
    
    private static final int RECHARGE = 0xF00FA;
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                switch (requestCode) {
                    case ADDCARD:
                        updateCardDisplay();
                        break;
                    case R.id.plan_usercard:
                        long pickedCardID = 0;
                        try {
                            pickedCardID = data.getLongExtra(SelectCardActivity.class.getCanonicalName()+".returnValue", 0);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        if(pickedCardID>0){
                            try {
                                activeCard = cardUtil.getUserCard(pickedCardID);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case RECHARGE:
                        long cchid = 0; 
                        try {
                            cchid = data.getLongExtra(CreditCardPaymentActivity.class.getCanonicalName()+".returnValue", 0);
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        try {
                            CardTransactionHistory cth = cardUtil.getCardTransactionHistory(selectedAccount.getSerialNumber(), cchid);
                            if(cth.getResult().equalsIgnoreCase("accepted")){
                                paid = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        if(paid){
                            Log.i("PA","Issuing subscribe after recharge...");
                            subscribe(lastSelectedTime);
                        }
                    default:
                        break;
                }
                if(activeCard!=null){
                    runOnUiThread(new Runnable() {
                        public void run() {
                            activeCardDisplay.setText(activeCard.getCardNumber());
                        }
                    });
                }
            }

            public void onCancel() {
            }
        };
        
        ProgressableTask task = new ProgressableTask(this, runnable
                , R.string.progress_userhome_fill, R.string.progress_wait, -1);
        task.start();
    }
    private void updateCardDisplay(){
        List<UserCard> cards = null;
        //TODO: do we add validated=1 here?
        try {
            cards = cardUtil.getUserCards("", "", 2, 1);
        } catch (FeefactorCheckedException e) {
            e.printStackTrace();
        }
        if(cards!=null && cards.size()>=1){
            activeCard = cards.get(0);
        }
        final int size=cards==null?0:cards.size();
        
        runOnUiThread(new Runnable() {
            public void run() {
                if(activeCard!=null){
                    activeCardDisplay.setText(activeCard.getCardNumber());
                }
                if(size>1){
                    pickerLabel.setVisibility(View.VISIBLE);
                    activeCardDisplay.setClickable(true);
                    activeCardDisplay.setLongClickable(true);
                    activeCardDisplay.setBackgroundColor(0xBBFFAADD);
                    activeCardDisplay.setTextColor(0xFF4477FF);
                } else {
                    pickerLabel.setVisibility(View.GONE);
                    activeCardDisplay.setClickable(false);
                    activeCardDisplay.setLongClickable(false);
                    activeCardDisplay.setBackgroundColor(0xFFEEEEFF);
                    activeCardDisplay.setTextColor(0xFF000000);
                }
            }
        });
    }
}
