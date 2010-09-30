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

import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.feefactor.accounts.Account;
import com.feefactor.paymentsystems.CardPayments;
import com.feefactor.paymentsystems.PaymentGateway;
import com.feefactor.samples.android.QuickstartApplication;
import com.feefactor.samples.android.R;
import com.feefactor.subscriber.User;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

/**
 * @author netmobo
 */
public class GoogleCheckoutActivity extends Activity {
    private static final String TAG = "GOOGLE_CHECKOUT";    
    
    public static final int ACTIVITY_GOOGLECHECKOUT = 1006786;
    
    public static final int SANDBOX = 0;
    public static final int LIVE = 1;

    public static final String SANDBOX_ENVIRONMENT_URL = "https://sandbox.google.com/checkout/api/checkout/v2/merchantCheckout/Merchant/";
    public static final String LIVE_ENVIRONMENT_URL = "https://checkout.google.com/api/checkout/v2/merchantCheckout/Merchant/";
    
    private Button backToAppButton;
    private WebView webview;
    private String returnUrl;
    private String successUrl;
    private Runnable mViewGoogleCheckout;
    private ProgressDialog mProgressDialog = null;

    private static NumberFormat numberFormat = NumberFormat.getInstance();
    private static Matcher matcher;
    
    private boolean paid = false;
    
    private class MyWebViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Loaded: " + url);
            if (url.contains("google")) {
                // it reached the confirmation page
                if (url.contains("confirmation") || url.contains("receipt")) {
                    paid = true;
                }
                view.loadUrl(url);
                return true;
            } else if (url.contains(returnUrl)) {
                // back to input amount
                if (paid) {
                    closeThis(RESULT_OK);
                }else {
                    closeThis(RESULT_CANCELED);
                }
            } else if (url.contains(successUrl)) {
                // go to manage account
                closeThis(RESULT_OK);
            }

            return true;
        }
    }

    static {
        numberFormat.setMaximumFractionDigits(6);
        numberFormat.setGroupingUsed(false);
        numberFormat.setMinimumIntegerDigits(1);

        Pattern pattern = Pattern
                .compile("<redirect-url>([^<]*)</redirect-url>");
        matcher = pattern.matcher("");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.google_checkout);

        webview = (WebView) findViewById(R.id.webview_component);
        webview.setWebViewClient(new MyWebViewClient());
        
        backToAppButton = (Button) findViewById(R.id.back_to_app);
        backToAppButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // shoppingcart= OR confirmation
                if (paid) {
                    closeThis(RESULT_OK);
                }else {
                    closeThis(RESULT_CANCELED);
                }
            }
        });

        returnUrl = getString(R.string.return_url);
        successUrl = getString(R.string.success_url);
    }

    private void closeThis(int resultCode) {
        setResult(resultCode);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        
        mViewGoogleCheckout = new Runnable() {
            public void run() {
                Intent intent = getIntent();

                if (intent != null) {

                    String intentData = intent.getDataString();

                    Log.d(TAG, "INTENTDATA: " + intentData);

                    MerchantItemData data = new MerchantItemData();
                    Bundle extras = intent.getExtras();         
                    
                    String sourceIp = getMyDeviceId();
                    String sessionId = sourceIp;
                    
                    // get these from intent
                    
                    String unitPriceAmount = extras.getString(GoogleCheckoutActivity.class.getCanonicalName()+".unitPriceAmount");
                    try{
                        new Double(unitPriceAmount);
                    } catch (Exception e){
                        finish();
                    }
                    
                    Account account = (Account) extras.getSerializable(GoogleCheckoutActivity.class.getCanonicalName()+".selectedAccount");
                    if(account==null){
                        finish();
                    }
                    long accountSerialNumber = account.getSerialNumber();
                    QuickstartApplication qsApp = QuickstartApplication.getApplication();
                    CardPayments cpUtil = qsApp.getCardPaymentUtility();
                    List<PaymentGateway> pgs;
                    try {
                        pgs = cpUtil.getBrandPaymentGateways("type='GOOGLECHECKOUT'", "", 1, 1);
                    } catch (Exception e) {
                        finish();
                        return;
                    }
                    if(pgs==null || pgs.isEmpty()){
                        finish();
                    }
                    PaymentGateway pg = pgs.get(0);
                    long paymentGatewayId = pg.getPaymentGatewayID();
                    Properties props = stringToProperties(pg.getAuthentication(), "=", "[;\r\n]");
                    
                    String merchantID = props.getProperty("MERCHANTID");
                    String merchantKey = props.getProperty("MERCHANTKEY");
                    String tmp = props.getProperty("ENVIRONMENT");
                    int environment = 0;
                    if(tmp==null || tmp.length()<1){
                        environment = SANDBOX;
                    } else {
                        environment = LIVE;
                    }
                        
                    User user = qsApp.getLoggedUser();
                    String unitPriceCurrency = props.getProperty("CURRENCY");
                    String description = "Recharge initiated via phone by "+user.getUsername();         
                    String username = user.getUsername();
                    String oldBalance = numberFormat.format(account.getBalance()+account.getCreditLimit());

                    data.setAccountSerialNumber(accountSerialNumber);
                    data.setDescription(description);
                    data.setOldBalance(oldBalance);
                    data.setPaymentGatewayId(paymentGatewayId);
                    data.setSessionId(sessionId);
                    data.setSourceIp(sourceIp);
                    data.setUnitPriceAmount(unitPriceAmount);
                    data.setUnitPriceCurrency(unitPriceCurrency);
                    data.setUsername(username);

                    String url = submitDataToGoogle(paymentGatewayId, environment,
                            merchantID, merchantKey, data);
                    Log.d(TAG, "LOADURL: "+url);
                    webview.loadUrl(url);
                    mProgressDialog.dismiss();
                } else {
                    // shouldn't happen
                    return;
                }
            }
        };
        Thread thread = new Thread(mViewGoogleCheckout);
        thread.start();
        mProgressDialog = ProgressDialog.show(this, "Please wait...",
                "Redirecting to site ...", true);

    }

    private String getMyDeviceId() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getDeviceId();
    }

    /**
     * This would return the URL to load for client browser to see.
     */
    private String submitDataToGoogle(long paymentGatewayId, int envi,
            String merchantID, String merchantKey,
            MerchantItemData merchantItemData) {

        /*-
         * Android has no full support for XML transformation and manipulation
         * XML param to google would have to be done by hand.
         */

        String itemTag = merchantItemData.getItemXml();

        List<String> itemTags = new ArrayList<String>();
        itemTags.add(itemTag);

        String checkoutTag = prepareCheckoutTag(itemTags, returnUrl,
                successUrl);

        try {
            return submitServerToServer(envi, merchantID, merchantKey,
                    checkoutTag);
        } catch (Exception e) {
            Log.e(TAG, "Unable to send info to google.", e);
            return "";
        }
    }

    private String submitServerToServer(int envi, String merchantID,
            String merchantKey, String xml) throws IOException {
        HttpClient client = new HttpClient();
        String url = getMerchantCheckoutURL(envi, merchantID);
        Log.d(TAG, "URL to send data to: " + url);
        Log.d(TAG, "XML: " + xml);
        PostMethod method = new PostMethod(url);
        client.getState().setCredentials(
                new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT),
                new UsernamePasswordCredentials(merchantID, merchantKey));

        List<String> authPrefs = new ArrayList<String>();
        authPrefs.add(AuthPolicy.BASIC);
        client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY,
                authPrefs);
        client.getParams().setAuthenticationPreemptive(true);
        method.setDoAuthentication(true);

        method.setRequestEntity(new StringRequestEntity(xml, "text/xml",
                "UTF-8"));
        client.executeMethod(method);
        String response = readResponse(method);
        Log.d(TAG, "Response: " + response);
        matcher.reset(response);
        matcher.find();
        String s = matcher.group(1);
        // somehow, it keeps including '&amp;' instead of '&'
        s = s.replaceAll("&amp;", "&");
        return s;
    }

    private String readResponse(HttpMethod method) throws IOException {
        InputStream is = method.getResponseBodyAsStream();
        StringBuffer sb = new StringBuffer();
        int readResult = 0;
        int offset = 0;
        int length = 1024;
        byte[] buffer = new byte[length];
        do {
            try {
                readResult = is.read(buffer, 0, length);
                String temp = new String(buffer);
                sb.append(temp.substring(0, readResult));
                offset += readResult;
            } catch (Exception e) {
                readResult = -1;
            }
        } while (readResult > 0 && readResult >= length);
        return sb.toString();
    }

    // this is for server-to-server
    private String getMerchantCheckoutURL(int env, String merchantID) {
        switch (env) {
            // sandbox.google.com -- 72.14.204.81
            case SANDBOX:
                return SANDBOX_ENVIRONMENT_URL + merchantID;
                // checkout.google.com -- 72.14.204.115
            case LIVE:
                return LIVE_ENVIRONMENT_URL + merchantID;
            default:
                throw new IllegalArgumentException("Unknown environment type.");
        }
    }

    private String prepareCheckoutTag(List<String> itemTags, String returnURL,
            String successURL) {
        StringBuffer sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\r\n");
        sb.append("<checkout-shopping-cart xmlns=\"http://checkout.google.com/schema/2\">");

        sb.append("<shopping-cart>");
        sb.append("<items>");
        for (String itemTag : itemTags) {
            sb.append(itemTag);
        }
        sb.append("</items>");
        sb.append("</shopping-cart>");

        if (!isEmpty(returnURL) || !isEmpty(successURL)) {
            sb.append("<checkout-flow-support>");
            sb.append("<merchant-checkout-flow-support>");
            if (!isEmpty(returnURL)) {
                sb.append("<edit-cart-url>").append(returnURL).append(
                        "</edit-cart-url>");
            }
            if (!isEmpty(successURL)) {
                sb.append("<continue-shopping-url>").append(successURL).append(
                        "</continue-shopping-url>");
            }

            sb.append("</merchant-checkout-flow-support>");
            sb.append("</checkout-flow-support>");
        }

        sb.append("</checkout-shopping-cart>");
        return sb.toString();
    }

    private boolean isEmpty(String input) {
        return input == null || input.trim().length() < 1;
    }
    
    public static Properties stringToProperties(String s, String keyValueDelim, String pairDelim) {
        if (s == null || s.equalsIgnoreCase("")) {
            return new Properties();
        }
        Properties returnProperties = new Properties();
        String[] pairs = s.split(pairDelim); 
        for(String pair : pairs){
            String[] keyval = pair.split(keyValueDelim);
            if(keyval.length<2){
                continue;
            }
            String key = keyval[0];
            String value = pair.substring(key.length()+1);
            
            key = key.toUpperCase();
            returnProperties.put(key, value);
        }
        
        return returnProperties;
    }

}