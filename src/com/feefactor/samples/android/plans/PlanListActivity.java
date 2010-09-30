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

import com.feefactor.accounts.Account;
import com.feefactor.accounts.Plan;
import com.feefactor.samples.android.MainListActivity;
import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class PlanListActivity extends MainListActivity {
    public PlanListActivity() {
        super(R.string.itemlist_textView_plans, R.layout.item);
        String finalCondition = "plantype in (1,2,4)"
            + " and upper (description) like '%FREEDOM%'"
            + " and upper (description) not like '%TRIAL%'";
        getItemListManager().setItemList(new PlanList(finalCondition));
    }

    @Override
    protected void expandItem(View itemSelected, int position, long rowid) {
        Plan plan = (Plan) getItemListManager().getItemList().getSerializableItem(position);
        Account account = (Account) getIntent().getSerializableExtra(PlanListActivity.class.getCanonicalName()+".selectedAccount");
        
        Intent intent = new Intent(this, PlanActivity.class);
        intent.putExtra(PlanActivity.class.getCanonicalName()+".selectedPlan", plan);
        intent.putExtra(PlanActivity.class.getCanonicalName()+".selectedAccount", account);
        startActivityForResult(intent, -1);
    }

    public void onStart() {
        super.onStart();
        Account account = (Account) getIntent().getSerializableExtra(PlanListActivity.class.getCanonicalName()+".selectedAccount");
        if(account==null){
            finish();
        }
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch(resultCode){
            case RESULT_OK : finish();
            default:
        }
    }
}
