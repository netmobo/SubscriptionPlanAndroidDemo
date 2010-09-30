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

import java.util.Properties;

import android.util.Log;

import com.feefactor.Server;

/**
 * @author netmobo
 */
public class ApplicationConfiguration {
    private static final String TAG = ApplicationConfiguration.class.getSimpleName();
    
    public static long BRANDID = 200001;
    public static long BRANDSERVICEID = 484957;
    public static String DOMAIN = "200001.feefactor.com";
    
    // Server Configuration
    public static Server SERVER = Server.VOLTAIRE_TEST;
    static {
        try {
            SERVER = SERVER.clone();
        } catch (CloneNotSupportedException e) {
            Log.w(TAG, "Unable to clone pre-defined server. Others might get affected.");
        }
    }
    // Pricing will be based on this APPCODE
    public final static String PRODUCT_CODE = "QUICKSTART3";

    // REASON for the transactions
    public final static String REASON = "Quickstart Sample App 1";

    public static boolean sTestMode = false;

    static void init(Properties props){
        DOMAIN = props.getProperty("domain", DOMAIN);
        BRANDID = new Long(props.getProperty("brandId", String.valueOf(BRANDID)));
        
        String host = SERVER.getHost();
        String prefix = SERVER.getPrefix();
        int port = SERVER.getPort();
        
        host = props.getProperty("host", host);
        prefix = props.getProperty("prefix", prefix);
        port = new Integer(props.getProperty("port", String.valueOf(port)));
        
        SERVER.setHost(host);
        SERVER.setPort(port);
        SERVER.setPrefix(prefix);
        Log.i(TAG, "Successfully updated application configuration.");
    }
}
