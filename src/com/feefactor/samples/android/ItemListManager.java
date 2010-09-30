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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class ItemListManager {
    private EditText goToPage;
    private TextView pageCount;
    private View pagingButtons;
    private long currentPage = 1;
    private String listTitle = "Item list.";
    protected ProgressDialog progress;
    private ItemAdapter adapter;
    private ItemList itemList = new ItemListImpl();
    
    public void refreshList(final ListActivity activity){
        Thread t = new Thread(){
            public void run(){
                Log.i("ILU", "refreshing display.");
                displayProgress(activity);
                itemList.refreshListData(currentPage);
                dismissProgress(activity);
            }
        };
        t.start();
    }
    
    public long getCurrentPage() {
        return currentPage;
    }

    public void initDisplay(ListActivity activity, int listLayoutReferenceID, int stringTitleReferenceID, int itemLayoutReferenceID, boolean monitorRowState) {
        activity.setContentView(listLayoutReferenceID);
        
        initializePagingButtons(activity);
        
        progress = new ProgressDialog(activity);
        progress.setTitle(activity.getString(R.string.itemlist_progress_title));
        progress.setMessage(activity.getString(R.string.itemlist_progress_text));
        
        adapter = new ItemAdapter(activity, this, itemLayoutReferenceID, monitorRowState);
        activity.setListAdapter(adapter);
        
        TextView title = (TextView)activity.findViewById(R.id.list_title);
        try{
            listTitle = activity.getString(stringTitleReferenceID);
        } catch (Exception e){
            e.printStackTrace();
        }
        
        if(isEmpty(listTitle)){
            listTitle = "Item list.";
        }
        title.setText(listTitle);
        
        //putting this on onStart/onResume is too heavy.
        refreshList(activity);
    }
    
    protected void initializePagingButtons(ListActivity activity){
        PagingHandler pg = new PagingHandler(activity);
        
        ImageButton first = (ImageButton)activity.findViewById(R.id.paging_first);
        first.setOnClickListener(pg);
        
        ImageButton back = (ImageButton)activity.findViewById(R.id.paging_back);
        back.setOnClickListener(pg);
        
        ImageButton forward = (ImageButton)activity.findViewById(R.id.paging_forward);
        forward.setOnClickListener(pg);
        
        ImageButton last = (ImageButton)activity.findViewById(R.id.paging_last);
        last.setOnClickListener(pg);
        
        ImageButton go = (ImageButton)activity.findViewById(R.id.paging_go);
        go.setOnClickListener(pg);
        
        pagingButtons = activity.findViewById(R.id.paging_layout);
        
        goToPage = (EditText)activity.findViewById(R.id.paging_goto);
        pageCount = (TextView)activity.findViewById(R.id.paging_pageCount);
        
        ImageButton refresh = (ImageButton)activity.findViewById(R.id.list_refresh);
        if(refresh!=null){
            refresh.setOnClickListener(pg);
        }
    }

    /*- miscellaneous functions */
    protected void displayProgress(ListActivity activity){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                progress.show();
                adapter.notifyDataSetChanged();
            }
        });
    }
    
    protected void dismissProgress(ListActivity activity){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                adapter.notifyDataSetChanged();
                try {
                    progress.dismiss();
                } catch (Exception e){
                    //for some odd reason, this happens.
                    //see: http://stackoverflow.com/questions/1111980/how-to-handle-screen-orientation-change-when-progress-dialog-and-background-threa
                    Log.w("LIA","Unable to dismiss progress dialog!?");
                }
                updatePagingData();
            }
        });
    }
    
    public boolean isEmpty(String s){
        return s==null || s.length()<1;
    }

    private void updatePagingData(){
        goToPage.setText(String.valueOf(currentPage));
        pageCount.setText("/"+itemList.getMaxPageSize());
        
        if(itemList.getMaxPageSize()>1){
            pagingButtons.setVisibility(View.VISIBLE);
        } else {
            pagingButtons.setVisibility(View.GONE);
        }
    }
    
    protected void resetCheckboxes(ListActivity activity){
        adapter.resetCheckboxes();
        SparseBooleanArray sba = activity.getListView().getCheckedItemPositions();
        if(sba!=null){
            sba.clear();
        }
    }

    private class PagingHandler implements OnClickListener{
        private final ListActivity activity;
        private PagingHandler(ListActivity activity){
            this.activity = activity;
        }
        
        /*- this section deals with the OnClickListener */
        public void onClick(View v) {
            long targetPageNumber = 1;
            try {
                targetPageNumber = new Long(goToPage.getText().toString());
            } catch (Exception e){
                targetPageNumber = 1;
            }
            
            switch(v.getId()){
                case R.id.paging_go : 
                    break;
                case R.id.paging_last : 
                    targetPageNumber = itemList.getMaxPageSize();
                    break;
                case R.id.paging_forward : 
                    targetPageNumber = currentPage + 1 ;
                    break;
                case R.id.list_refresh : 
                    targetPageNumber = currentPage;
                    break;
                case R.id.paging_back : 
                    targetPageNumber = currentPage - 1 ;
                    break;
                case R.id.paging_first :
                default :
                    targetPageNumber = 1;
                    break;
            }
            
            if(targetPageNumber<1){
                targetPageNumber = 1;
            }
            if(targetPageNumber > itemList.getMaxPageSize()){
                targetPageNumber = itemList.getMaxPageSize();
            }
            
            currentPage = targetPageNumber;

            refreshList(activity);
        }
    }

    public ItemList getItemList() {
        return itemList;
    }

    public void setItemList(ItemList itemList) {
        if(itemList==null){
            this.itemList.clear();
        } else {
            this.itemList = itemList;
        }
    }
    
    private class ItemListImpl implements ItemList{
        public void clear() {
        }

        public int getListCount() {
            return 0;
        }
        public Item getListItem(int pos) {
            return null;
        }

        public long getMaxPageSize() {
            return 1;
        }

        public Serializable getSerializableItem(int pos) {
            return null;
        }

        public void refreshListData() {
        }

        public void refreshListData(long currentPage) {
        }
    }
}
