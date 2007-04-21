package org.jvnet.jax_ws_commons.transport.smtp.server;

import org.springframework.beans.factory.BeanNameAware;

/**
 * @org.apache.xbean.XBean element="smtp"
 */
public class SMTPConfig implements BeanNameAware {
    private String name;
    private String address;
    
    public void setBeanName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
        System.out.println("**** Address ***="+address);
    }

    public String getAddress() {
        return address;
    }

}
