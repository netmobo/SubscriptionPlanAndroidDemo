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

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.*;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.feefactor.accounts.Account;
import com.feefactor.accounts.AccountHistory;
import com.feefactor.accounts.Accounts;
import com.feefactor.accounts.Plan;
import com.feefactor.accounts.Plans;
import com.feefactor.samples.android.BaseActivity;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.services.BrandService;
import com.feefactor.subscriber.User;

/**
 * @author netmobo
 */
public class UserHomeActivity extends BaseActivity{
    private TextView activeAccountDisplay, errorMessage, planDescription, lastRefresh
        , lastPlanRC, currentFreeVal;
    private Accounts accountUtil;
    private Plans planUtil;
    private Account activeAccount; 
    private User loggedUser;
    private NumberFormat nf = NumberFormat.getInstance();
    
    private View pickerLabel;
    private Button subscribeToPlan;
    private String stpTxt;
    
    @Override
    protected void initDisplay() {
        setContentView(R.layout.user_home);
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(6);
        nf.setMinimumIntegerDigits(1);
        
        //initialize the display fields
        activeAccountDisplay = (TextView)findViewById(R.id.userhome_accountid);
        planDescription = (TextView)findViewById(R.id.userhome_plandescription);
        lastRefresh = (TextView)findViewById(R.id.userhome_lastplanrefreshdate);
        lastPlanRC = (TextView)findViewById(R.id.userhome_lastplanrc);
        currentFreeVal = (TextView)findViewById(R.id.userhome_freevaluebal);
        errorMessage = (TextView)findViewById(R.id.errorText);
        pickerLabel = findViewById(R.id.label1);
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                switch (requestCode) {
                    case R.id.userhome_accountid:
                        Log.i("UHA","Retrieving account selected.");
                        long pickedSerial = 0;
                        try {
                            pickedSerial = data.getLongExtra(SelectAccountActivity.class.getCanonicalName()+".returnValue", 0);
                        } catch (Exception e){
                            e.printStackTrace();
                            //experience NPE on "back"
                        }
                        if(pickedSerial>0){
                            try {
                                activeAccount = accountUtil.getAccount(pickedSerial);
                                sendMessage(SUCCESS);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                sendMessage(FAILED);
                            }
                        }
                        break;
                    default:
                        break;
                }
                fillInData(activeAccount);
            }

            public void onCancel() {
            }
        };
        
        ProgressableTask task = new ProgressableTask(this, runnable
                , R.string.progress_userhome_fill, R.string.progress_wait, -1);
        task.start();
    }

    private void fillInData(final Account account){
        if(account!=null){
            //fill in the data.
            final String aaid = account.getAccountID();
            String pd = "";
            String lr = "";
            String lprc = "";
            String cfv = "";
            boolean hideOpts = true;
            
            try {
                Log.i("UHA","Starting to collect new data...");
                Plan plan = planUtil.getPlan(account.getPlanID());
                if(plan!=null){
                    pd = plan.getDescription();
                    
                    StringBuffer rdate = new StringBuffer();
                    rdate.append(account.getLastPlanRefreshDate().get(Calendar.MONTH)+1).append("/");
                    rdate.append(account.getLastPlanRefreshDate().get(Calendar.DAY_OF_MONTH)).append("/");
                    rdate.append(account.getLastPlanRefreshDate().get(Calendar.YEAR));
                    
                    lr = rdate.toString();
                    
                    long timestart = account.getLastPlanRefreshDate().getTimeInMillis() - 60000;
                    long timeend = account.getLastPlanRefreshDate().getTimeInMillis() + 60000;
                    String timeCond = timestart + " and " + timeend;
                    
                    List<AccountHistory> ahs = accountUtil.getAccountHistories(account.getSerialNumber(), "upper(transactiontype) in ('PLANSIGNUP', 'PLANRC') and TRANSACTIONDATE between "+timeCond, "TRANSACTIONDATE DESC, TRANSACTIONTYPE ASC", 1, 1);
                    if(ahs!=null && !ahs.isEmpty()){
                        lprc =nf.format(ahs.get(0).getAmountChange());
                    }
                    
                    cfv = nf.format(account.getFreeValueBal()+account.getFreeRollOverBal());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            try {
                Log.i("UHA","Getting account data");
                long count = accountUtil.getAccountsCount("");
                Log.i("UHA","Count retrieved: " + count);
                if(count>1){
                    hideOpts = false;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            if(pd==null || pd.length()<1){
                pd = "none";
            }
            if(lr==null || lr.length()<1){
                lr = "n/a";
            }
            if(lprc==null || lprc.length()<1){
                lprc = "n/a";
            }
            if(cfv==null || cfv.length()<1){
                lprc = "0";
            }

            final String f_pd = pd;
            final String f_lr = lr;
            final String f_lprc = lprc;
            final String f_cfv = cfv;
            final boolean f_ho = hideOpts;

            runOnUiThread(new Runnable() {
                public void run() {
                    activeAccountDisplay.setText(aaid);
                    planDescription.setText(f_pd);
                    lastRefresh.setText(f_lr);
                    lastPlanRC.setText(f_lprc);
                    currentFreeVal.setText(f_cfv);
                    
                    if(f_ho){
                        Log.i("UHA","Hiding picker..");
                        activeAccountDisplay.setClickable(false);
                        activeAccountDisplay.setLongClickable(false);
                        activeAccountDisplay.setBackgroundColor(0xFFEEEEFF);
                        activeAccountDisplay.setTextColor(0xFF000000);
                        
                        pickerLabel.setVisibility(View.GONE);
                    } else {
                        Log.i("UHA","Displaying picker..");
                        activeAccountDisplay.setClickable(true);
                        activeAccountDisplay.setLongClickable(true);
                        activeAccountDisplay.setBackgroundColor(0xBBFFAADD);
                        activeAccountDisplay.setTextColor(0xFF4477FF);
                        pickerLabel.setVisibility(View.VISIBLE);
                    }
                    
                    if(account.getPlanID()>0){
                        subscribeToPlan.setText(stpTxt);
                    } else {
                        subscribeToPlan.setText("Purchase Plans");
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onStart();
        errorMessage.setVisibility(View.GONE);
    }
    
    private void initializeActionButtons(){
        final Activity act = this;
        subscribeToPlan = (Button)findViewById(R.id.userhome_subscribe);
        stpTxt = subscribeToPlan.getText().toString();
        final BrandService service = QuickstartApplication.getApplication().getBrandService();
        subscribeToPlan.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(act, PlanListActivity.class);
                intent.putExtra(PlanListActivity.class.getCanonicalName()+".selectedAccount", activeAccount);
                startActivity(intent);
            }
        });
        
        Button purchaseMovie = (Button)findViewById(R.id.userhome_purchasemovie);
        purchaseMovie.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(act, PurchaseMovieActivity.class);
                intent.putExtra(PurchaseMovieActivity.class.getCanonicalName()+".selectedAccount", activeAccount);
                intent.putExtra(PurchaseMovieActivity.class.getCanonicalName()+".selectedBrandService", service);
                startActivity(intent);
            }
        });
        
        Button accountHistory = (Button)findViewById(R.id.userhome_accounthistory);
        accountHistory.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(act, HistoryOptionActivity.class);
                intent.putExtra(HistoryOptionActivity.class.getCanonicalName()+".selectedAccount", activeAccount);
                startActivity(intent);
            }
        });
        
        ImageButton logout = (ImageButton)findViewById(R.id.userhome_logout);
        logout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ProgressableRunnable runnable = new ProgressableRunnable() {
                    public void run() {
                        confirmExit();
                    }

                    public void onCancel() {
                    }
                };
                
                ProgressableTask task = new ProgressableTask(act, runnable
                        , R.string.progress_userhome_fill, R.string.progress_wait, -1);
                task.start();
            }
        });
        
        ImageButton refresh = (ImageButton)findViewById(R.id.userhome_refresh);
        refresh.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ProgressableRunnable runnable = new ProgressableRunnable() {
                    public void run() {
                        try {
                            activeAccount = accountUtil.getAccount(activeAccount.getSerialNumber());
                            fillInData(activeAccount);
                        } catch (Exception e) {
                        }
                    }
                    
                    public void onCancel() {
                    }
                };
                
                ProgressableTask task = new ProgressableTask(act, runnable
                        , R.string.progress_userhome_fill, R.string.progress_wait, -1);
                task.start();
            }
        });
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                QuickstartApplication pqa = QuickstartApplication.getApplication();
                accountUtil = pqa.getAccountUtility();
                planUtil = pqa.getPlanUtility();
                loggedUser = pqa.getLoggedUser();
                List<Account> pal = null;
                try {
                    pal = accountUtil.getAccounts("userid="+loggedUser.getUserID(), "balance+creditlimit desc", 1, 1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                activeAccount = null;
                if(pal!=null && !pal.isEmpty()){
                    activeAccount = pal.get(0);
                }
                
                if(activeAccount==null){
                    //handle it gracefully...
                    //activeAccount = new Account();
                    finish();
                }
                
                Map<String, Serializable> input = new HashMap<String, Serializable>();
                input.put(SelectAccountActivity.class.getCanonicalName()+".User", loggedUser);
                addListPicker(activeAccountDisplay, SelectAccountActivity.class, input);
                
                initializeActionButtons();
                fillInData(activeAccount);
            }
            public void onCancel() {
            }
        };
        
        ProgressableTask task = new ProgressableTask(this, runnable
                , R.string.progress_userhome_fill, R.string.progress_wait, -1);
        task.start();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            confirmExit();
            return false;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
    
    private void confirmExit(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final Activity act = this;
        builder.setMessage("Are you sure you want to leave?")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                    act.finish();
               }
            })
            .setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                   //do nothing
               }
            });
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

}
