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
package com.utility;

import java.util.Calendar;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author netmobo
 */
public class StringUtility {
	 public static Properties stringToProperties(String input, String delimter1, String delimeter2, boolean capitalizeKeys) {
        if (input == null || input.equalsIgnoreCase("")) {
            return new Properties();
        }
        Properties returnProperties = new Properties();
        StringTokenizer st = new StringTokenizer(input, delimeter2);

        while (st.hasMoreTokens()) {
            String[] key_val = st.nextToken().split(delimter1);
            String val = null;
            switch (key_val.length) {
            case 0:
                continue;
            case 1:
                val = "";
                continue;
            default:
                val = key_val[1];
            }

            returnProperties.setProperty(capitalizeKeys ? key_val[0].trim().toUpperCase() : key_val[0].trim(), val.trim());
        }

        return returnProperties;
    }
    public static Calendar convertMmddyyyyToCalendar(String data){
        //MM/dd/yyyy
        String[] parts = data.split("[/-]");
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, new Integer(parts[0])-1);
        cal.set(Calendar.DAY_OF_MONTH, new Integer(parts[1]));
        cal.set(Calendar.YEAR, new Integer(parts[2]));
        cal.getTime();
        return cal;
    }
    
    public static String convertCalendarToMmddyyyy(Calendar cal){
        //MM/dd/yyyy
        StringBuffer sb = new StringBuffer();
        sb.append(cal.get(Calendar.MONTH)+1).append("/");
        sb.append(cal.get(Calendar.DAY_OF_MONTH)).append("/");
        sb.append(cal.get(Calendar.YEAR));
        return sb.toString();
    }
}
