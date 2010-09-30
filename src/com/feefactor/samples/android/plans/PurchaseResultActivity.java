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
import android.view.View;
import android.widget.TextView;

import com.feefactor.samples.android.BaseActivity;
import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class PurchaseResultActivity extends BaseActivity{
    private View successGroup;
    private TextView titleDisplay, errorMessage;
    
    @Override
    protected void initDisplay() {
        setContentView(R.layout.purchase_result);
        
        titleDisplay = (TextView) findViewById(R.id.purchase_title);
        errorMessage = (TextView) findViewById(R.id.purchase_errorMessage);
        successGroup = findViewById(R.id.purchase_success);
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        Intent intent = getIntent();
        String title = intent.getStringExtra(PurchaseResultActivity.class.getCanonicalName()+".selectedProduct");
        if(title==null || title.length()<1){
            successGroup.setVisibility(View.GONE);
            errorMessage.setVisibility(View.VISIBLE);
        } else {
            successGroup.setVisibility(View.VISIBLE);
            errorMessage.setVisibility(View.GONE);
            
            titleDisplay.setText(title);
        }
    }
}
