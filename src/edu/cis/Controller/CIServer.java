package edu.cis.Controller;

import acm.program.*;
import edu.cis.Model.CISConstants;
import edu.cis.Model.Request;
import edu.cis.Model.SimpleServerListener;
import edu.cis.Utils.SimpleServer;

import java.util.*;

public class CIServer extends ConsoleProgram implements SimpleServerListener {
    private static final int PORT = 8000;
    private SimpleServer server = new SimpleServer(this, PORT);

    private Map<String, User> users = new HashMap<>();
    private Map<String, MenuItem> menuItems = new HashMap<>();
    private Map<String, Order> orders = new HashMap<>();

    public void run() {
        println("Starting server on port " + PORT);
        server.start();
    }

    public String requestMade(Request request) {
        String cmd = request.getCommand();
        println(request.toString());

        try {
            switch (cmd) {
                case CISConstants.PING:
                    return "Hello, internet";

                case CISConstants.CREATE_USER:
                    return createUser(request);

                case CISConstants.ADD_MENU_ITEM:
                    return addMenuItem(request);

                case CISConstants.PLACE_ORDER:
                    return placeOrder(request);

                case CISConstants.DELETE_ORDER:
                    return deleteOrder(request);

                case CISConstants.GET_ITEM:
                    return getItem(request);

                case CISConstants.GET_USER:
                    return getUser(request);

                case CISConstants.GET_ORDER:
                    return getOrder(request);

                default:
                    return "Error: Unknown command " + cmd + ".";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private String createUser(Request request) {
        String userId = request.getParam(CISConstants.USER_ID_PARAM);
        String userName = request.getParam(CISConstants.USER_NAME_PARAM);
        String yearLevel = request.getParam(CISConstants.YEAR_LEVEL_PARAM);

        if (users.containsKey(userId)) {
            throw new IllegalArgumentException("User ID already exists");
        }

        User user = new User(userId, userName, yearLevel);
        users.put(userId, user);
        return CISConstants.SUCCESS;
    }

    private String addMenuItem(Request request) {
        String itemId = request.getParam(CISConstants.ITEM_ID_PARAM);
        String name = request.getParam(CISConstants.ITEM_NAME_PARAM);
        String desc = request.getParam(CISConstants.DESC_PARAM);
        double price = Double.parseDouble(request.getParam(CISConstants.PRICE_PARAM));
        String type = request.getParam(CISConstants.ITEM_TYPE_PARAM);

        if (menuItems.containsKey(itemId)) {
            throw new IllegalArgumentException("Menu item ID already exists");
        }

        MenuItem item = new MenuItem(name, desc, price, itemId, type);
        menuItems.put(itemId, item);
        return "success";
    }

    private String placeOrder(Request request) {
        validateParams(request, CISConstants.USER_ID_PARAM, CISConstants.ORDER_ID_PARAM,
                CISConstants.ITEM_ID_PARAM, CISConstants.ORDER_TYPE_PARAM);

        String userId = request.getParam(CISConstants.USER_ID_PARAM);
        String orderId = request.getParam(CISConstants.ORDER_ID_PARAM);
        String itemId = request.getParam(CISConstants.ITEM_ID_PARAM);
        String orderType = request.getParam(CISConstants.ORDER_TYPE_PARAM);

        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        if (!menuItems.containsKey(itemId)) {
            throw new IllegalArgumentException("Menu item not found");
        }
        if (orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Order ID already exists");
        }

        User user = users.get(userId);
        MenuItem item = menuItems.get(itemId);

        if (user.getMoney() - item.getPrice() < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        if (item.getAmountAvailable() <= 0) {
            throw new IllegalArgumentException("Item is out of stock");
        }
        item.decrementAmountAvailable();

        Order order = new Order(itemId, orderType, orderId);
        orders.put(orderId, order);
        user.addOrder(order);
        user.deductMoney(item.getPrice());

        return "success";
    }

    private String deleteOrder(Request request) {
        String userId = request.getParam(CISConstants.USER_ID_PARAM);
        String orderId = request.getParam(CISConstants.ORDER_ID_PARAM);

        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        User user = users.get(userId);

        if (!orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Order not found");
        }

        user.removeOrder(orderId);
        orders.remove(orderId);

        return "success";
    }

    private String getItem(Request request) {
        String itemId = request.getParam(CISConstants.ITEM_ID_PARAM);

        if (!menuItems.containsKey(itemId)) {
            throw new IllegalArgumentException("Item not found");
        }

        MenuItem item = menuItems.get(itemId);
        return item.toString();
    }

    private String getUser(Request request) {
        String userId = request.getParam(CISConstants.USER_ID_PARAM);

        if (!users.containsKey(userId)) {
            throw new IllegalArgumentException("User not found");
        }

        User user = users.get(userId);
        return user.toString();
    }

    private String getOrder(Request request) {
        String orderId = request.getParam(CISConstants.ORDER_ID_PARAM);

        if (!orders.containsKey(orderId)) {
            throw new IllegalArgumentException("Order not found");
        }

        Order order = orders.get(orderId);
        return order.toString();
    }

    private void validateParams(Request request, String... params) {
        for (String param : params) {
            if (request.getParam(param) == null) {
                throw new IllegalArgumentException("Missing parameter: " + param);
            }
        }
    }

    private class User {
        private String userID;
        private String name;
        private String yearLevel;
        private List<Order> orderList = new ArrayList<>();
        private double money = 50.0;

        public User(String userID, String name, String yearLevel) {
            this.userID = userID;
            this.name = name;
            this.yearLevel = yearLevel;
        }

        public void addOrder(Order order) {
            orderList.add(order);
        }

        public void removeOrder(String orderId) {
            orderList.removeIf(o -> o.getOrderID().equals(orderId));
        }

        public double getMoney() {
            return money;
        }

        public void deductMoney(double amount) {
            money -= amount;
        }

        public String toString() {
            String ordersStr = orderList.toString();
            ordersStr = ordersStr.replaceAll("\\[", "");
            ordersStr = ordersStr.replaceAll("]", "");
            return String.format("CISUser{userID='%s', name='%s', yearLevel='%s', orders= %s, money=%.1f}",
                    userID, name, yearLevel, ordersStr, money);
        }
    }

    private class MenuItem {
        private String name;
        private String description;
        private double price;
        private String id;
        private int amountAvailable = 10;
        private String type;

        public MenuItem(String name, String description, double price, String id, String type) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.id = id;
            this.type = type;
        }

        public double getPrice() {
            return price;
        }

        public int getAmountAvailable() {
            return amountAvailable;
        }

        public void decrementAmountAvailable() {
            amountAvailable--;
        }

        public String toString() {
            return String.format(
                    "MenuItem{name='%s', description='%s', price=%.1f, id='%s', amountAvailable=%d, type='%s'}",
                    name, description, price, id, amountAvailable, type);
        }
    }

    private class Order {
        private String itemID;
        private String type;
        private String orderID;

        public Order(String itemID, String type, String orderID) {
            this.itemID = itemID;
            this.type = type;
            this.orderID = orderID;
        }

        public String getOrderID() {
            return orderID;
        }

        public String toString() {
            return String.format("Order{itemID='%s', type='%s', orderID='%s'}",
                    itemID, type, orderID);
        }
    }

    public static void main(String[] args) {
        CIServer f = new CIServer();
        f.start(args);
    }
}