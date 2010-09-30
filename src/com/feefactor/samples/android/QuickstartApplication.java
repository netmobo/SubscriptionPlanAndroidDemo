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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.feefactor.AuthDetail;
import com.feefactor.BrandAuthDetail;
import com.feefactor.ClientConfig;
import com.feefactor.FeefactorCheckedException;
import com.feefactor.RtbeUserAuthDetail;
import com.feefactor.accounts.Accounts;
import com.feefactor.accounts.Plans;
import com.feefactor.charging.Transactions;
import com.feefactor.paymentsystems.CardPayments;
import com.feefactor.paymentsystems.PaymentGateway;
import com.feefactor.services.BrandService;
import com.feefactor.services.BrandServices;
import com.feefactor.subscriber.Brand;
import com.feefactor.subscriber.Brands;
import com.feefactor.subscriber.SelfSignUp;
import com.feefactor.subscriber.User;
import com.feefactor.subscriber.Users;

/**
 * @author netmobo
 */
public class QuickstartApplication extends Application {
	public static final String TAG = "QUICKSTARTAPPLICATION";
	
	private boolean loggedIn;

	//set during login
	//cleared during load of loginActivity
	private User loggedUser = null;
	
	//set this during creation...
	private Brand brand = null;
	private BrandService brandService;
	private PaymentGateway paymentGateway = null;

	private ClientConfig config = new ClientConfig(ApplicationConfiguration.SERVER, null);
	
	private Accounts accountUtility = new Accounts(config);
	private Plans planUtility = new Plans(config);
	private Brands brandUtility = new Brands(config);
	private BrandServices brandServiceUtility = new BrandServices(config);
	private Users userUtility = new Users(config);
	private CardPayments cardPaymentUtility = new CardPayments(config);
	private Transactions transactionUtility = new Transactions(config);
	private SelfSignUp selfSignUpUtility;
	private boolean initialized = false;
	
	public User getLoggedUser() {
		return loggedUser;
	}

	public Brand getBrand() {
		return brand;
	}

	public QuickstartApplication() {
        super();
        pqa = this;
        
        //special case...
        BrandAuthDetail bad = new BrandAuthDetail();
        bad.setBrandID(ApplicationConfiguration.BRANDID);
        bad.setDomain(ApplicationConfiguration.DOMAIN);
        selfSignUpUtility = new SelfSignUp(new ClientConfig(ApplicationConfiguration.SERVER, bad));
    }

    public Accounts getAccountUtility() {
		return accountUtility;
	}

	public Users getUserUtility() {
		return userUtility;
	}

	public ClientConfig getConfig() {
		return config;
	}

	public void logout() {
		this.loggedIn = false;
		loggedUser = null;
		config.setAuthenticationDetail(null);
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @throws FeefactorCheckedException
	 */
	public void login(AuthDetail authDetail)
			throws FeefactorCheckedException {
	    if(!initialized){
	        loadConfig();
	        initialized = true;
	    }
	    config.setAuthenticationDetail(authDetail);
	    
	    //test authentication
	    if(brandUtility.getBrand(ApplicationConfiguration.BRANDID)==null){
            FeefactorCheckedException fce = new FeefactorCheckedException();
            fce.setMessage("Invalid username/password");
            fce.setErrorcode(401);
            throw fce;
	    }
	    
	    config.setAuthenticationDetail(authDetail);
	    
        List<User> us = userUtility.getUsers("A.BRANDID="+ ((RtbeUserAuthDetail)authDetail).getBrandID() 
                + " AND USERNAME='" + ((RtbeUserAuthDetail)authDetail).getUsername()+ "'" 
                + " AND PASSWORD='" + ((RtbeUserAuthDetail)authDetail).getPassword() + "'"
                , "", 1, 1);
        if(us==null || us.isEmpty()){
            //odd...
            FeefactorCheckedException fce = new FeefactorCheckedException();
            fce.setMessage("Invalid username/password");
            fce.setErrorcode(401);
            throw fce;
        }
        User endUser = us.get(0);
        loggedUser = endUser;
		initializeApplicationVariables();
		this.loggedIn = true;
	}

	public CardPayments getCardPaymentUtility() {
		return cardPaymentUtility;
	}

	public void setCardPaymentUtility(CardPayments cardPaymentUtility) {
		this.cardPaymentUtility = cardPaymentUtility;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	private void initializeApplicationVariables(){
	    try {
    	    if(brand==null){
                brand = brandUtility.getBrand(ApplicationConfiguration.BRANDID);
            }
    	    if(brandService==null){
    	        brandService = brandServiceUtility.getBrandService(ApplicationConfiguration.BRANDSERVICEID);
    	    }
    	    if(paymentGateway == null){
    	        List<PaymentGateway> pgs = cardPaymentUtility.getBrandPaymentGateways("type='SANDBOX'", "", 1, 1);
    	        if(pgs!=null && !pgs.isEmpty()){
    	            paymentGateway = pgs.get(0);
    	        } else {
    	            paymentGateway = cardPaymentUtility.getBrandPaymentGateway(brand.getPaymentGatewayID());
    	        }
    	    }
	    } catch (Exception e) {
	        Log.w(TAG, "Unable to properly initialize member variables.", e);
	    }
	}
	
    private Properties loadConfig() {
        Resources resources = this.getResources();
        AssetManager assetManager = resources.getAssets();

        // Read from the /assets directory
        try {
            InputStream inputStream = assetManager.open("application.properties");
            Properties properties = new Properties();
            properties.load(inputStream);

            ApplicationConfiguration.init(properties);
        } catch (IOException e) {
            Log.e(TAG, "Unable to properly initialize application.", e);
        }

        return null;
    }

    public Plans getPlanUtility() {
        return planUtility;
    }

    public BrandServices getBrandServiceUtility() {
        return brandServiceUtility;
    }

    public Transactions getTransactionUtility() {
        return transactionUtility;
    }
    
    private static QuickstartApplication pqa = new QuickstartApplication();
    public static QuickstartApplication getApplication(){
        return pqa;
    }

    public PaymentGateway getPaymentGateway() {
        return paymentGateway;
    }

    public SelfSignUp getSelfSignUpUtility() {
        return selfSignUpUtility;
    }

    public BrandService getBrandService() {
        return brandService;
    }
}
