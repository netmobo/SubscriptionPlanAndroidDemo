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

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class SingleSelectListActivity extends ListActivity implements ItemListActivity{
    private ItemListManager listManager = new ItemListManager();
    private int titleReferenceID;
    public SingleSelectListActivity(){
    }
    
    public SingleSelectListActivity(int titleReferenceID){
        this.titleReferenceID = titleReferenceID;
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        
        listManager.initDisplay(this, R.layout.select_itemlist, titleReferenceID, R.layout.item, false);
        
        View saveButton = findViewById(R.id.list_save);
        saveButton.setVisibility(View.GONE);
        
        getListView().setOnItemClickListener(new ReturnOnSelect(this));
    }

    public ItemListManager getItemListManager() {
        return listManager;
    }
    
    private class ReturnOnSelect implements OnItemClickListener{
        private Activity act;
        private ReturnOnSelect(Activity act){
            this.act = act;
        }
        public void onItemClick(AdapterView<?> parent, View itemSelected, int position, long rowid) {
            try {
                Item item = listManager.getItemList().getListItem(position);
                if(item!=null){
                    Intent data = new Intent();
                    data.putExtra(act.getClass().getCanonicalName()+".returnValue", item.getRowID());
                    setResult(Activity.RESULT_OK, data);
                }
            } catch (Exception e){
                Log.e("SSLIA", "Unable to process item.");
            }
            
            try {
                finish();
            } catch (Exception e){
                Log.e("SSLIA", "Unable to end activity.");
            }
        }
    }
}
