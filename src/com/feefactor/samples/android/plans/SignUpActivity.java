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

import java.util.Calendar;
import java.util.List;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.feefactor.FeefactorCheckedException;
import com.feefactor.RtbeUserAuthDetail;
import com.feefactor.accounts.Account;
import com.feefactor.accounts.AccountPlan;
import com.feefactor.accounts.Accounts;
import com.feefactor.accounts.Plan;
import com.feefactor.accounts.Plans;
import com.feefactor.samples.android.ApplicationConfiguration;
import com.feefactor.samples.android.BaseActivity;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.subscriber.User;
import com.feefactor.subscriber.UserQuestion;
import com.feefactor.subscriber.SelfSignUp;
import com.feefactor.subscriber.Users;

/**
 * @author netmobo
 */
public class SignUpActivity extends BaseActivity {
    private static final String TAG = "SIGNUPACTIVITY";
    
    private Button signupBtn;
    private EditText usernameEt;
    private EditText passwordEt;
    private EditText emailEt;
    
    private EditText userQuestionEt;
    private EditText userQuestionAnswerEt;
    
    private static Plan trial;
    
    protected void initDisplay() {
        setContentView(R.layout.signup);
        
        signupBtn = (Button) findViewById(R.id.signup_register_button);
        
        usernameEt = (EditText) findViewById(R.id.username);
        passwordEt = (EditText) findViewById(R.id.password);
        emailEt = (EditText) findViewById(R.id.email);
        
        userQuestionEt = (EditText) findViewById(R.id.security_question);
        userQuestionAnswerEt = (EditText) findViewById(R.id.security_answer);
                
        String username;
        String email;
        String question;
        String answer;
        
        // if we are in test mode, autogenerate account
        long random = new Double(Math.random()*1000).longValue();
        username = random + getString(R.string.username);
        email = random + getString(R.string.email);
        // default
        question = "What was your childhood nickname?";
        answer = "Boy Droid";
        usernameEt.setText(username);
        emailEt.setText(email);
        userQuestionEt.setText(question);
        userQuestionAnswerEt.setText(answer);
        
        // Set Click Listener
        signupBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                signupClicked();
            }
        });
    }
    public void signupClicked() {
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                signup();
            }

            public void onCancel() {
            }
        };

        ProgressableTask task = new ProgressableTask(this, runnable, R.string.signup);
        task.start();
    }
    
        
    private void signup() {
        String username;
        String password;
        String email;
        String question;
        String answer; 
        
        username = usernameEt.getText().toString().trim();
        password = passwordEt.getText().toString().trim();
        email = emailEt.getText().toString().trim();
        question = userQuestionEt.getText().toString().trim();
        answer = userQuestionAnswerEt.getText().toString().trim();
        
        QuickstartApplication qsApp = QuickstartApplication.getApplication();
        long BRANDID = ApplicationConfiguration.BRANDID;
        
        // BARE MINIMUM
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setBillingEmailAddress(email);
        user.setBrandID(BRANDID);
        // January 1 2020
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, 10);
        user.setExpiration(c);
        user.setBillingSchedule("0 0 01 * * *");        

        try {
            Log.d(TAG, "Setting up config...");
            
            SelfSignUp selfSignUpUtility = qsApp.getSelfSignUpUtility();
            
            Log.d(TAG, "Inserting user...");
            long userId = selfSignUpUtility.insertUser(user, "Signup via mobile.");
            Log.i(TAG, "insertUser: " + userId);
            
            Log.d(TAG, "Log in...");
            RtbeUserAuthDetail authDetail = new RtbeUserAuthDetail();
            authDetail.setBrandID(ApplicationConfiguration.BRANDID);
            authDetail.setPassword(user.getPassword());
            authDetail.setUsername(user.getUsername());
            
            // at this point we log-in the user
            qsApp.login(authDetail);
            
            Users userUtility = qsApp.getUserUtility();
            
            UserQuestion userQuestion = new UserQuestion();
            userQuestion.setUserID(userId);
            userQuestion.setQuestion(question);
            userQuestion.setAnswer(answer);
            Log.d(TAG, "Inserting User Question...");
            userUtility.insertUserQuestion(userQuestion, "Signup via mobile.");
            
            Plans planUtility = QuickstartApplication.getApplication().getPlanUtility();
            if(trial==null){
                List<Plan> plans = null;
                try {
                    plans = planUtility.getPlans("upper(description) like '%TRIAL%'", "", 1, 1);
                } catch (FeefactorCheckedException e) {
                    e.printStackTrace();
                }
                if(plans!=null && !plans.isEmpty()){
                    trial = plans.get(0);
                } else {
                    trial = new Plan();
                }
            }

            Accounts accountUtil = qsApp.getAccountUtility();
            List<Account> accounts = accountUtil.getAccounts("", "", 1, 1);
            if(accounts!=null && accounts.size()>0 && trial!=null){
                AccountPlan ap = new AccountPlan();
                ap.setPlanID(trial.getPlanID());
                ap.setSerialNumber(accounts.get(0).getSerialNumber());
                ap.setEffectivityDate(Calendar.getInstance());
                
                accountUtil.scheduleSubscription(ap, Accounts.MODE_FULL, true, true, "signup");
            }
            
            Intent intent = new Intent(this, UserHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
            
            sendMessage(SUCCESS);
        } catch (FeefactorCheckedException e) {
            Log.e(TAG, e.getMessage());
            sendMessage(FAILED);
        }
    }
}