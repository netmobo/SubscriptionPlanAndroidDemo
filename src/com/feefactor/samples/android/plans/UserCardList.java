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
import java.util.List;

import android.util.Log;

import com.feefactor.paymentsystems.UserCard;
import com.feefactor.samples.android.Item;
import com.feefactor.samples.android.ItemList;
import com.feefactor.samples.android.QuickstartApplication;

/**
 * @author netmobo
 */
public class UserCardList implements ItemList{
    private List<UserCard> cardList = new ArrayList<UserCard>();
    private String finalCondition = "";

    private long maxPageSize = 1;

    public long getMaxPageSize() {
        return maxPageSize;
    }

    public void refreshListData() {
        refreshListData(1);
    }
    
    public void refreshListData(long currentPage) {
        QuickstartApplication pqa = QuickstartApplication.getApplication();
        long pageNumber = currentPage;
        
        try {
            maxPageSize = pqa.getCardPaymentUtility().getUserCardsCount(finalCondition);
            maxPageSize = new Double(Math.ceil(maxPageSize/10)).longValue();
            if(maxPageSize<1){
                maxPageSize=1;
            }
            cardList = pqa.getCardPaymentUtility().getUserCards(finalCondition, "CARDNUMBER", 10, pageNumber);
        } catch (Exception e) {
            Log.e("UCL", "Unable to retrieve list.");
        }
        
        if(cardList==null){
            cardList = new ArrayList<UserCard>();
        }
    }
    
    public int getCount() {
        return cardList.size();
    }

    public Item getListItem(int position) {
        try {
            return new CardItem(cardList.get(position));
        } catch (Exception e){
            return null;
        }
    }

    public int getListCount() {
        return cardList.size();
    }

    public class CardItem implements Item{
        private String content;
        private String title;
        private UserCard card;
        
        private CardItem(UserCard card){
            StringBuffer sb = new StringBuffer();
            sb.append(card.getCardNumber());
            title = sb.toString();
            content = card.toString();
            this.card = card;
        }

        public String getRowContent() {
            return content;
        }

        public String getRowTitle() {
            return title;
        }
        
        public long getRowID(){
            return card.getCardID();
        }
    }

    public Serializable getSerializableItem(int pos) {
        return cardList.get(pos);
    }
    
    public void clear() {
        cardList.clear();
    }

    public void setAdditionalCondition(String condition) {
        // TODO Auto-generated method stub
        
    }
}
