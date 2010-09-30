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
package com.feefactor.samples.android;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Map;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.DatePicker;
import android.widget.TextView;

/**
 * @author netmobo
 */
public abstract class BaseActivity extends Activity {
	public static final int FAILED = 0;
	public static final int SUCCESS = 1;
	private String errorMessage;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dpl = new DatePickerListener(this);
		initDisplay();
	}

	/**
	 * Declare which layout to be used here. Abstract classes may specify to use a default.
	 * This is should always be the first line of onCreate(Bundle b) if overridden
	 */
	protected abstract void initDisplay();

    protected Handler _handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            super.handleMessage(message);
            switch (message.what) {
            case SUCCESS:
                ToastUtil.alertLong(BaseActivity.this, "Success");
                break;
            case FAILED:
                ToastUtil.alertLong(BaseActivity.this, "Failed");
                break;
            default:
                ToastUtil.alertLong(BaseActivity.this, errorMessage);
            }
        }
    };

    public void sendMessage(int messageCode) {
        if (_handler != null) {
            _handler.sendEmptyMessage(messageCode);
        }
    }
    
    public void sendMessage(String errorMessage) {
        if (_handler != null) {
            this.errorMessage = errorMessage;
            _handler.sendEmptyMessage(-1);
        }
    }
	
	public boolean isEmpty(String s){
	    return s==null || s.length()<1;
	}
	
    private DatePickerListener dpl;
    protected void addDatePicker(TextView v){
        Log.i("BA","datePicker set on " + v.getId());
        v.setOnClickListener(dpl);
        v.setOnLongClickListener(dpl);
    }
    
    private final int DATEPICKER_DIALOG = 0xedad0021;
    private class DatePickerListener implements OnLongClickListener, OnClickListener, OnDateSetListener{
        private TextView toUpdate;
        private DatePickerDialog dpg;
        private final Context context;
        
        public DatePickerListener(Context context) {
            super();
            this.context = context;
        }
        
        public void onClick(View view) {
            onLongClick(view);
        }
        
        /*-
         * Month adjustment needed as MM starts at 1 while datepicker starts at 0
         */
        public boolean onLongClick(View view) {
            Log.i("BA.DPL","onLongClick activated on " + view.getId()+", " + ((TextView)view).getText());
            int year, month, dayOfMonth;
            try {
                String currentText = ((TextView)view).getText().toString();
                String[] parts = currentText.split("[/-]");
                month = new Integer(parts[0])-1;
                dayOfMonth = new Integer(parts[1]);
                year = new Integer(parts[2]);
            } catch (Exception e){
                Log.e("BaA", "Unable to update parse date.");
                Calendar c = Calendar.getInstance();
                year = c.get(Calendar.YEAR);
                month = c.get(Calendar.MONTH);
                dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            }
            
            dpg = new DatePickerDialog(context, this, year, month, dayOfMonth);
            showDialog(DATEPICKER_DIALOG);
            toUpdate = (TextView) view;
            return true;
        }

        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String result = (month+1)+"/"+dayOfMonth+"/"+year;
            toUpdate.setText(result);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id){
            case DATEPICKER_DIALOG : return dpl.dpg;
            default:
        }
        return super.onCreateDialog(id);
    }
    
    protected void addListPicker(TextView targetView, Class<? extends SingleSelectListActivity> activity){
        addListPicker(targetView, activity, null);
    }
    
    protected void addListPicker(TextView targetView, Class<? extends SingleSelectListActivity> activity, Map<String, Serializable> extras){
        ListPicker lp = new ListPicker(activity, extras);
        targetView.setOnClickListener(lp);
        targetView.setOnLongClickListener(lp);
    }
    
    
    private class ListPicker implements OnClickListener, OnLongClickListener{
        private final Class<? extends SingleSelectListActivity> activity;
        private final Map<String, Serializable> extras;
        
        public ListPicker(Class<? extends SingleSelectListActivity> activity, Map<String, Serializable> extras) {
            super();
            this.activity = activity;
            this.extras = extras;
        }

        public void onClick(View v) {
            onLongClick(v);
        }

        public boolean onLongClick(View v) {
            Intent intent = new Intent(getApplicationContext(), activity);
            Log.i("BaseA", "listPicker set for: " + v.getId());
            
            if(extras!=null && !extras.isEmpty()){
                for(String key : extras.keySet()){
                    intent.putExtra(key, extras.get(key));
                }
            }
            startActivityForResult(intent, v.getId());
            return true;
        }
    }
}
