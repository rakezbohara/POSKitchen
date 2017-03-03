package com.app.rakez.poskitchen;

/**
 * Created by RAKEZ on 2/23/2017.
 */
public class OrderItem {
    private String orderID;
    private String status;
    private String orderItem;

    public OrderItem(String orderID, String orderItem, String status) {
        this.orderID = orderID;
        this.orderItem = orderItem;
        this.status = status;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(String orderItem) {
        this.orderItem = orderItem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
