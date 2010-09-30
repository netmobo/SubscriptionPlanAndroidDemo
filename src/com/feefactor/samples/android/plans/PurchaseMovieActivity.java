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
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.feefactor.FeefactorCheckedException;
import com.feefactor.accounts.Account;
import com.feefactor.paymentsystems.CardPayments;
import com.feefactor.paymentsystems.CardTransactionHistory;
import com.feefactor.paymentsystems.UserCard;
import com.feefactor.samples.android.ApplicationConfiguration;
import com.feefactor.samples.android.ProgressableRunnable;
import com.feefactor.samples.android.ProgressableTask;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.services.BrandProduct;

/**
 * @author netmobo
 */
public class PurchaseMovieActivity extends PurchaseActivity{
    private UserCard activeCard;
    private TextView activeCardDisplay, freeValueDisplay;
    private CardPayments cardUtil;
    private NumberFormat nf = NumberFormat.getInstance();
    private ProductList plRef;
    private volatile boolean paid;

    private View ccpanel, fvpanel, pickerLabel;
    
    public PurchaseMovieActivity() {
        super(R.string.itemlist_textView_products, R.layout.movielist);
        
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(6);
        nf.setMinimumIntegerDigits(1);
    }
    
    @Override
    protected Account getActiveAccount() {
        Intent intent = getIntent();
        return (Account) intent.getSerializableExtra(PurchaseMovieActivity.class.getCanonicalName()+".selectedAccount");
    }

    @Override
    public void onStart() {
        super.onStart();
        plRef = new ProductList(ApplicationConfiguration.BRANDSERVICEID, "upper(productcode) like '%MOVIE%'");
        getItemListManager().setItemList(plRef);
        getItemListManager().refreshList(this);
        
        boolean useCards = false;
        Account account = getActiveAccount();
        if(account.getPlanID()>0 
                && account.getAccountPlanID()>0 
                && (account.getFreeValueBal()>0 || account.getFreeRollOverBal()>0)){
            ccpanel.setVisibility(View.GONE);
            fvpanel.setVisibility(View.VISIBLE);
        } else {
            ccpanel.setVisibility(View.VISIBLE);
            fvpanel.setVisibility(View.GONE);
            useCards = true;
        }
        
        //check the available cards...
        if(useCards){
            ProgressableTask task = new ProgressableTask(this, new ProgressableRunnable() {
                public void run() {
                    updateCardDisplay();
                }
                
                public void onCancel() {
                }
            }, R.string.progress_userhome_fill, R.string.progress_wait, -1);
            task.start();
        } else {
            freeValueDisplay.setText(nf.format(account.getFreeValueBal()+account.getFreeRollOverBal()));
        }
    }
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        ProgressableRunnable runnable = new ProgressableRunnable() {
            public void run() {
                switch (requestCode) {
                    case ADDCARD:
                        updateCardDisplay();
                        break;
                    case R.id.ml_usercard:
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
                            CardTransactionHistory cth = cardUtil.getCardTransactionHistory(getActiveAccount().getSerialNumber(), cchid);
                            if(cth.getResult().equalsIgnoreCase("accepted")){
                                paid = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        
                        if(paid){
                            purchase(getActiveAccount(), (BrandProduct) data.getSerializableExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedProduct"));
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
    
    private static final int ADDCARD = 0xF00F00;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ccpanel = findViewById(R.id.ml_cc_panel);
        fvpanel = findViewById(R.id.ml_fv_panel);
        pickerLabel = findViewById(R.id.label1);
        
        activeCardDisplay = (TextView) findViewById(R.id.ml_usercard);
        freeValueDisplay = (TextView) findViewById(R.id.ml_freevalue);
        
        cardUtil = QuickstartApplication.getApplication().getCardPaymentUtility();
        
        final Activity act = this;
        ImageButton addCard = (ImageButton) findViewById(R.id.ml_addcard);
        addCard.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(act, UserCardActivity.class);
                startActivityForResult(intent, ADDCARD);
            }
        });
    }

    private static final int RECHARGE = 0xF00FA;
    @Override
    protected void purchase(Account activeAccount, BrandProduct bp) {
        // TODO Auto-generated method stub
        Account account = getActiveAccount();
        if(account.getPlanID()>0 
                && account.getAccountPlanID()>0 
                && (account.getFreeValueBal()>0 || account.getFreeRollOverBal()>0)){
            super.purchase(activeAccount, bp);
        } else if(plRef.getRegularPrice(bp)<=0){
            //free item!!!
            super.purchase(activeAccount, bp);
        } else if(paid){
            paid = false;
            //free item!!!
            super.purchase(activeAccount, bp);
        } else {
            Intent intent = new Intent(this, CreditCardPaymentActivity.class);
            /*-
             * NOTE: There are several assumptions here:
             * 
             * 1. Quickstart plans are all of type UNIT (plan.planType=2)
             * 2. Once account.freeValueBal is depleted, regular prices are used
             * 3. Scheduled prices are NOT considered here.
             */
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".unitPriceAmount", ""+plRef.getRegularPrice(bp));
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedCard", activeCard);
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedProduct", bp);
            intent.putExtra(CreditCardPaymentActivity.class.getCanonicalName()+".selectedAccount", account);
            startActivityForResult(intent, RECHARGE);
        }
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
