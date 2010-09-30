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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.feefactor.samples.android.Item;
import com.feefactor.samples.android.ItemList;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.services.BrandProduct;
import com.feefactor.services.BrandProductPlanPrice;
import com.feefactor.services.BrandProductPrice;
import com.feefactor.services.BrandServices;

/**
 * @author netmobo
 */
public class ProductList implements ItemList{
    private List<BrandProduct> productList = new ArrayList<BrandProduct>();
    //static is an optimization for reruns but makes data possibly stale.
    private static Map<Long, Double> regularPrice = new HashMap<Long,Double>();
    private static Map<Long, Double> planPrice = new HashMap<Long,Double>();
    private String finalCondition = "";
    private NumberFormat nf = NumberFormat.getInstance();
    
    private long brandserviceId;
    
//    public ProductList() {
//        super();
//        nf.setGroupingUsed(true);
//        nf.setMaximumFractionDigits(6);
//        nf.setMinimumIntegerDigits(1);
//    }
    
    public ProductList(long serviceId) {
        super();
        
        this.brandserviceId = serviceId;
        
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(6);
        nf.setMinimumIntegerDigits(1);
    }
    
    public ProductList(long serviceId, String additionalCondition) {
        super();
        
        this.brandserviceId = serviceId;
        
        if(additionalCondition!=null && additionalCondition.length()>0){
            finalCondition += " ("+additionalCondition + ")";
        }
    }
    
    private long maxPageSize = 1;

    public long getMaxPageSize() {
        return maxPageSize;
    }

    public void refreshListData() {
        refreshListData(1);
    }
    
    public void refreshListData(long currentPage) {
        QuickstartApplication pqa = QuickstartApplication.getApplication();
        BrandServices serviceUtil = pqa.getBrandServiceUtility();
        long pageNumber = currentPage;
        
        Log.i("PrL", "Service ID: " + brandserviceId);
        
        try {
            maxPageSize = serviceUtil.getBrandProductsCount(brandserviceId, finalCondition);
            maxPageSize = new Double(Math.ceil(maxPageSize/10)).longValue();
            if(maxPageSize<1){
                maxPageSize=1;
            }
            productList = pqa.getBrandServiceUtility().getBrandProducts(brandserviceId, finalCondition, "PRODUCTCODE", 10, pageNumber);
            
            if(productList!=null && !productList.isEmpty()){
                //terribly slow....
                for(BrandProduct bp : productList){
                    if(!regularPrice.containsKey(bp.getProductID())){
                        List<BrandProductPrice> regulars = 
                            serviceUtil.getBrandProductPrices(bp.getProductID(), "", "INDEXNUMBER ASC", 1, 1);

                        if(regulars != null && regulars.size() > 0) {
                            try {
                                regularPrice.put(bp.getProductID(), regulars.get(0).getPrice());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }else {
                            Log.d(ProductList.class.getName(), "No base rates found for BP: " 
                                    + bp.getProductCode() + " (" + bp.getProductID() + ")");
                        }
                    }
                    
                    if(!planPrice.containsKey(bp.getProductID())){
                        List<BrandProductPlanPrice> plans = 
                            serviceUtil.getBrandProductPlanPrices(bp.getProductID(), 0, "", "INDEXNUMBER ASC", 1, 1);
                        
                        if(plans != null && plans.size() > 0) {
                            try {
                                planPrice.put(bp.getProductID(), plans.get(0).getPrice());
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                        }else {
                            Log.d(ProductList.class.getName(), "No plan rates found for BP: " 
                                    + bp.getProductCode() + " (" + bp.getProductID() + ")");
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("PrL", "Unable to retrieve list.");
            e.printStackTrace();
        }
        
        if(productList==null){
            productList = new ArrayList<BrandProduct>();
        }
    }
    
    public int getCount() {
        return productList.size();
    }

    public Item getListItem(int position) {
        try {
            return new BrandProductItem(productList.get(position));
        } catch (Exception e){
            return null;
        }
    }

    public int getListCount() {
        return productList.size();
    }

    class BrandProductItem implements Item{
        private String content;
        private String title;
        private BrandProduct brandProduct;
        
        private BrandProductItem(BrandProduct brandProduct){
            StringBuffer sb = new StringBuffer();
            sb.append(brandProduct.getDescription());
            if(regularPrice.containsKey(brandProduct.getProductID()) && regularPrice.get(brandProduct.getProductID())>0){
                sb.append(": ").append(nf.format(regularPrice.get(brandProduct.getProductID())));
            }
            if(planPrice.containsKey(brandProduct.getProductID()) && planPrice.get(brandProduct.getProductID())>0){
                sb.append(" (").append(nf.format(planPrice.get(brandProduct.getProductID()))).append(")");
            }
            title = sb.toString();
            content = brandProduct.getProductCode();
            this.brandProduct = brandProduct;
        }

        public String getRowContent() {
            return content;
        }

        public String getRowTitle() {
            return title;
        }
        
        public long getRowID(){
            return brandProduct.getProductID();
        }
    }

    public Serializable getSerializableItem(int pos) {
        return productList.get(pos);
    }
    
    public void clear() {
        productList.clear();
    }
    
    double getRegularPrice(BrandProduct bp){
        return regularPrice.get(bp.getProductID());
    }
    
    double getPlanPrice(BrandProduct bp){
        return planPrice.get(bp.getProductID());
    }
}
