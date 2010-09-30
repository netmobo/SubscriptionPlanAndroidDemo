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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.util.Log;

import com.feefactor.accounts.Account;
import com.feefactor.accounts.AccountHistory;
import com.feefactor.samples.android.Item;
import com.feefactor.samples.android.ItemList;
import com.feefactor.samples.android.QuickstartApplication;

/**
 * @author netmobo
 */
public class AccountHistoryList implements ItemList{
    private List<AccountHistory> accountHistoryList = new ArrayList<AccountHistory>();
    
    private final Account account;
    
    public AccountHistoryList(Account account) {
        super();
        this.account = account;
    }

    private long maxPageSize = 1;

    public long getMaxPageSize() {
        return maxPageSize;
    }

    public void refreshListData() {
        refreshListData(1);
    }
    
    public void refreshListData(long currentPage) {
        if(account==null || account.getSerialNumber()<1){
            return;
        }
        
        QuickstartApplication pqa = QuickstartApplication.getApplication();
        long pageNumber = currentPage;
        
        try {
            long timestamp = System.currentTimeMillis();
          
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            c.add(Calendar.DAY_OF_YEAR, -14);
            long _14days = c.getTimeInMillis();
            StringBuffer condition = new StringBuffer();
            condition.append("transactiondate between ").append(_14days).append(" and ").append(timestamp);
            condition.append(" and transactiontype<>'Account generation'");
            
            maxPageSize = pqa.getAccountUtility().getAccountHistoriesCount(account.getSerialNumber(), condition.toString());
            maxPageSize = new Double(Math.ceil(maxPageSize/10)).longValue();
            if(maxPageSize<1){
                maxPageSize=1;
            }
            
            accountHistoryList = pqa.getAccountUtility().getAccountHistories(account.getSerialNumber(), condition.toString(), "accounthistoryid desc", 10, pageNumber);
        } catch (Exception e) {
            Log.e("AHL", "Unable to retrieve list.");
        }
        
        if(accountHistoryList==null){
            accountHistoryList = new ArrayList<AccountHistory>();
        }
    }
    
    public int getCount() {
        return accountHistoryList.size();
    }

    public Item getListItem(int position) {
        try {
            return new AccountHistoryItem(accountHistoryList.get(position));
        } catch (Exception e){
            return null;
        }
    }

    public int getListCount() {
        return accountHistoryList.size();
    }

    public class AccountHistoryItem implements Item{
        private String content;
        private String title;
        private AccountHistory accountHistory;
        
        private AccountHistoryItem(AccountHistory accountHistory){
            StringBuffer sb = new StringBuffer();
            sb.append(accountHistory.getDescription()).append(": ").append(accountHistory.getAmountChange());
            title = sb.toString();
            
            sb.setLength(0);
            //avoid the use of java.util.Date.
            Calendar timestamp = accountHistory.getTransactionDate();
            sb.append("Timestamp: ").append(timestamp.get(Calendar.MONTH)+1);
            sb.append("/").append(timestamp.get(Calendar.DAY_OF_MONTH));
            sb.append("/").append(timestamp.get(Calendar.YEAR));
            sb.append(" ").append(timestamp.get(Calendar.HOUR_OF_DAY));
            sb.append(":").append(timestamp.get(Calendar.MINUTE));
            sb.append(":").append(timestamp.get(Calendar.SECOND));
            sb.append(".").append(timestamp.get(Calendar.MILLISECOND));
            content = sb.toString();
            this.accountHistory = accountHistory;
        }

        public String getRowContent() {
            return content;
        }

        public String getRowTitle() {
            return title;
        }
        
        public long getRowID(){
            return accountHistory.getAccountHistoryID();
        }
    }

    public Serializable getSerializableItem(int pos) {
        return accountHistoryList.get(pos);
    }

    
    public void clear() {
        accountHistoryList.clear();
    }
}
