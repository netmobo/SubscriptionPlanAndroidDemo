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

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.feefactor.AuthDetail;
import com.feefactor.RtbeUserAuthDetail;
import com.feefactor.samples.android.ApplicationConfiguration;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.R;
import com.feefactor.samples.android.BaseActivity;

/**
 * @author netmobo
 */
public class LoginActivity extends BaseActivity implements OnFocusChangeListener, OnClickListener {
	private static final String TAG = "Login";
	
	public static final String PREFS_NAME = "LoginPreference";
	public static final String PASSWORD_USER_NAME = "StoredUserPasswordPreference";
	public static final String LAST_USER_USERNAME = "lastUserUsername";
	
	private CheckBox rememberPassword;
	private EditText usernameEt;
	private EditText passwordEt;
	private Button loginBtn;
	private Button signUpBtn;
    
    private QuickstartApplication pqsApp;
    
	@Override
	protected void initDisplay() {
	    setContentView(R.layout.login);
	    //move this and prepare stuff on resume
	    
		usernameEt = (EditText) findViewById(R.id.login_username);
		usernameEt.setOnFocusChangeListener(this);
		
		passwordEt = (EditText) findViewById(R.id.login_password);

		
		loginBtn = (Button) findViewById(R.id.login_login_button);
		// Set Click Listener
		loginBtn.setOnClickListener(this);
		pqsApp = QuickstartApplication.getApplication();
	}

    @Override
    protected void onStart() {
        super.onStart();
        //reset input fields.
        pqsApp.logout();
        prepareUsernameField();
        signUpBtn = (Button) findViewById(R.id.login_signup_button);
        signUpBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(), SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(intent);
            }
        });
        
        if(signUpBtn != null){
            signUpBtn.setVisibility(View.VISIBLE);
        }
    }
    
    private void prepareUsernameField(){
        String username = "";
        username = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(LAST_USER_USERNAME, "");
        usernameEt.setText(username);
        preparePasswordField(username);
        
        rememberPassword = (CheckBox) findViewById(R.id.login_remember_password);
        if(!isEmpty(passwordEt.getText().toString())){
            rememberPassword.setChecked(true);
        } else {
            rememberPassword.setChecked(false);
        }
    }
    
    private void preparePasswordField(String username){
        String lastUsedPassword = "";
        if(!isEmpty(username)){
            lastUsedPassword = getSharedPreferences(PASSWORD_USER_NAME, MODE_PRIVATE).getString(username, "");
        }
        
        passwordEt.setText(lastUsedPassword);
    }
    
    /*- specific to the view listeners*/
    public void onFocusChange(View v, boolean hasFocus) {
        if(hasFocus){
            //do nothing
            return;
        }
        preparePasswordField(usernameEt.getText().toString());
        
        rememberPassword = (CheckBox) findViewById(R.id.login_remember_password);
        if(!isEmpty(passwordEt.getText().toString())){
            rememberPassword.setChecked(true);
        }
    }
    
    private void displayErrorMessage(){
        TextView errorText = (TextView)findViewById(R.id.errorText);
        if(errorText == null){
            errorText = new TextView(getApplicationContext());
        }
        errorText.setText("Invalid username and/or password");
        errorText.setVisibility(View.VISIBLE);
    }
    
    public void onClick(View v) {
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                login();
            }
            public void onCancel() {
            }
        };

        ProgressableTask task = new ProgressableTask(this, 
                runnable, R.string.loginActivity_progressableTask_in);
        task.start();
    }
    
    public void login() {
        try {
            String username;
            String password;

            username = usernameEt.getText().toString();
            password = passwordEt.getText().toString();
            
            //authenticate first before storing... 
            AuthDetail ad = null;
            RtbeUserAuthDetail ruad = new RtbeUserAuthDetail();
            ruad.setBrandID(ApplicationConfiguration.BRANDID);
            ruad.setUsername(username);
            ruad.setPassword(password);
            
            ad = ruad;
            
            Log.d(TAG, "Attempting to log-in...");
            pqsApp.login(ad);
            Log.d(TAG, "Logged in!");
            
            if(!pqsApp.isLoggedIn()){
                sendMessage(FAILED);
                runOnUiThread(new Runnable() {
                    public void run() {
                        displayErrorMessage();
                    }
                });
                return;
            }

            // save to preferences
            String key = "";
            key = LAST_USER_USERNAME;
            
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putString(key, username)
                .commit();
            
            if(rememberPassword.isChecked()){
                getSharedPreferences(PASSWORD_USER_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(username, password)
                    .commit();
            } else {
                getSharedPreferences(PASSWORD_USER_NAME, MODE_PRIVATE)
                    .edit()
                    .remove(username)
                    .commit();
            }

            sendMessage(SUCCESS);
            
            doUserLogin(ad);
        } catch (Exception e) {
            Log.e(TAG, "Unable to authenticate. Reason: "+e.getMessage());
            pqsApp.logout();
            sendMessage(FAILED);
        }
    }
    
    private void doUserLogin(AuthDetail ad){
        //go to main window
        Intent intent = new Intent(this, UserHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}