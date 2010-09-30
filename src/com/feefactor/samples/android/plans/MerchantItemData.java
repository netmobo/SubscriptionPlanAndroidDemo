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

/**
 * @author netmobo
 */
public class MerchantItemData {
    private String description;
    private String unitPriceAmount;
    private String unitPriceCurrency;
    
    // private merchant data
    private String sessionId;
    private long accountSerialNumber;
    private long paymentGatewayId;
    private String username;
    private String sourceIp;
    private String oldBalance;

    public String getItemXml() {
        StringBuffer sb = new StringBuffer();
        sb.append("<item>");

        sb.append("<item-name>").append(accountSerialNumber).append("</item-name>");
        sb.append("<item-description>").append(description).append(
                "</item-description>");
        sb.append("<unit-price currency=\"").append(unitPriceCurrency).append("\">")
                .append(unitPriceAmount).append("</unit-price>");
        sb.append("<quantity>").append(1).append("</quantity>");
        sb.append("<merchant-item-id>").append(accountSerialNumber).append(
                "</merchant-item-id>");

        // private data...
        sb.append("<merchant-private-item-data>");
        sb.append("<sessionID>").append(sessionId).append("</sessionID>");
        sb.append("<payeeID>").append(accountSerialNumber).append("</payeeID>");
        sb.append("<pgid>").append(paymentGatewayId).append("</pgid>");
        sb.append("<username>").append(username).append("</username>");
        sb.append("<targetLevel>").append("Account").append("</targetLevel>");
        sb.append("<sourceIP>").append(sourceIp).append("</sourceIP>");
        sb.append("<oldBalance>").append(oldBalance).append(
                "</oldBalance>");
        sb.append("<startTime>").append(System.currentTimeMillis()).append(
                "</startTime>");
        sb.append("</merchant-private-item-data>");

        sb.append("</item>");
        return sb.toString();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUnitPriceAmount() {
        return unitPriceAmount;
    }

    public void setUnitPriceAmount(String unitPriceAmount) {
        this.unitPriceAmount = unitPriceAmount;
    }

    public String getUnitPriceCurrency() {
        return unitPriceCurrency;
    }

    public void setUnitPriceCurrency(String unitPriceCurrency) {
        this.unitPriceCurrency = unitPriceCurrency;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getAccountSerialNumber() {
        return accountSerialNumber;
    }

    public void setAccountSerialNumber(long accountSerialNumber) {
        this.accountSerialNumber = accountSerialNumber;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getOldBalance() {
        return oldBalance;
    }

    public void setOldBalance(String oldBalance) {
        this.oldBalance = oldBalance;
    }

    public long getPaymentGatewayId() {
        return paymentGatewayId;
    }

    public void setPaymentGatewayId(long paymentGatewayId) {
        this.paymentGatewayId = paymentGatewayId;
    }
}
