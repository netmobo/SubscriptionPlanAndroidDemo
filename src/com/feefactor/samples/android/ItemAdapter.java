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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.feefactor.samples.android.R;

/**
 * @author netmobo
 */
public class ItemAdapter extends ArrayAdapter<Item>{
    private final ListActivity activity;
    private final ItemListManager listManager;
    private final int itemLayoutReferenceID;
    private Map<Integer, View> cbMap = new HashMap<Integer, View>();
    private boolean monitorRowState = false;
    
    public ItemAdapter(ListActivity activity, ItemListManager listUtil, int itemLayoutReferenceID, boolean monitorRowState) {
        super(activity, itemLayoutReferenceID, new ArrayList<Item>());
        this.activity = activity;
        this.itemLayoutReferenceID = itemLayoutReferenceID;
        this.monitorRowState = monitorRowState;
        this.listManager = listUtil;
    }

    /*- this section deals with the Adapter */
    static class ViewHolder {
        TextView title;
        TextView content;
        CheckBox checkBox;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        View v;
        
        if(monitorRowState){
            v = cbMap.get(position);
        } else {
            v = convertView;
        }
        
        if(v==null) {
            LayoutInflater vi = (LayoutInflater) activity.getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(itemLayoutReferenceID, parent, false);

            vh = new ViewHolder();
            vh.title = (TextView) v.findViewById(R.id.item_title);
            vh.content = (TextView) v.findViewById(R.id.item_content);
            
            try {
                vh.checkBox = (CheckBox)v.findViewById(R.id.item_checkbox);
            } catch (Exception e){
            }
            
            v.setTag(vh);
            
            if(monitorRowState){
                cbMap.put(position, v);
            }
        }
        
        vh = (ViewHolder) v.getTag();
        
        Item item = listManager.getItemList().getListItem(position);

        if(item!=null){
            vh.title.setText(item.getRowTitle());
            vh.content.setText(item.getRowContent());
            v.setVisibility(View.VISIBLE);
        } else {
            vh.title.setText("");
            vh.content.setText("");
            v.setVisibility(View.GONE);
        }

        if(position%2==0){
            v.setBackgroundColor(0xBABEFEED);
        } else {
            v.setBackgroundColor(0xDEAFBEEF);
        }
        
        return v;
    }

    @Override
    public int getCount() {
        return listManager.getItemList().getListCount();
    }
    
    protected void resetCheckboxes(){
        if(!monitorRowState){
            return;
        }
        
        for(View v : cbMap.values()){
            ViewHolder vh = (ViewHolder) v.getTag();
            try {
                vh.checkBox.setChecked(false);
            } catch (Exception e){
            }
        }
    }
}
