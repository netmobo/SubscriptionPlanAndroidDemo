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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.feefactor.accounts.Account;
import com.feefactor.accounts.Plan;
import com.feefactor.accounts.Plans;
import com.feefactor.charging.Transactions;
import com.feefactor.samples.android.ItemListActivity;
import com.feefactor.samples.android.ItemListManager;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.services.BrandProduct;

/**
 * @author netmobo
 */
public abstract class PurchaseActivity extends ListActivity implements ItemListActivity{
    private ItemListManager listManager = new ItemListManager();
    private int titleReferenceID, layoutReferenceID = R.layout.select_itemlist;
    private Dialog confirmationDialog;
    private volatile BrandProduct selectedProduct;
    
    public PurchaseActivity(int titleReferenceID){
        this.titleReferenceID = titleReferenceID;
    }
    public PurchaseActivity(int titleReferenceID, int layoutReferenceID){
        this.titleReferenceID = titleReferenceID;
        this.layoutReferenceID = layoutReferenceID;
    }
    
    public ItemListManager getItemListManager() {
        return listManager;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        listManager.initDisplay(this, layoutReferenceID, titleReferenceID, R.layout.item, false);
        
        getListView().setOnItemClickListener(new ProductListener());
        final Activity act = this;
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE: 
                        ProgressableRunnable runnable = new ProgressableRunnable() {
                            public void run() {
                                purchase(getActiveAccount(), selectedProduct);
                            }
                            public void onCancel() {
                            }
                        };
                        
                        ProgressableTask task = new ProgressableTask(act, runnable
                                , R.string.progress_purchase, R.string.progress_wait, -1);
                        task.start();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener);
        confirmationDialog = builder.create();

    }

    protected void purchase(Account activeAccount, BrandProduct bp){
        Log.i("UHA","Performing purchase....");
        QuickstartApplication pqa = QuickstartApplication.getApplication();
        Transactions tu = pqa.getTransactionUtility();
        String selectedProduct = "";
        try {
            tu.chargeAccount(activeAccount.getBrandID(), activeAccount.getAccountID()
                    , "DB:ff:"+activeAccount.getBrandID()
                    , bp.getProductCode()
                    , 1
                    , "test");
            //refresh data
            selectedProduct = bp.getDescription();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, PurchaseResultActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        intent.putExtra(PurchaseResultActivity.class.getCanonicalName()+".selectedProduct", selectedProduct);
        startActivity(intent);
        finish();
    }
    
    private class ProductListener implements OnItemClickListener{
        public void onItemClick(AdapterView<?> parent, View itemSelected, int position, long rowid) {
            selectedProduct = (BrandProduct) listManager.getItemList().getSerializableItem(position);
            confirmationDialog.show();
        }
    }
    
    protected abstract Account getActiveAccount();
    
    protected Plan getActivePlan(){
        Account account = getActiveAccount();
        if(account==null || account.getPlanID()<1){
            return null;
        }
        
        try {
            Plans au = QuickstartApplication.getApplication().getPlanUtility();
            return au.getPlan(getActiveAccount().getPlanID());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
