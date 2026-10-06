import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * ONLINE FOOD ORDERING SYSTEM - BACKEND
 * Demonstrates:
 * - OOP: Abstraction, Encapsulation, Polymorphism, Inheritance
 * - Java Collections: List, ArrayList, Map, HashMap
 * - Streams API & Lambda Expressions
 * - Exception Handling & Multi-threading
 * - Built-in Java HTTP Microserver with CORS support
 */

// --- Base Class (Encapsulation & Polymorphism) ---
class MenuItem {
    private final int id;
    private final String name;
    private final String category;
    private final double price;
    private final String description;
    private final String imageUrl;

    public MenuItem(int id, String name, String category, double price, String description, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Polymorphic method: Base items have standard pricing
    public double calculateDiscountedPrice() {
        return this.price;
    }

    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"description\":\"%s\",\"imageUrl\":\"%s\"}",
            id, escape(name), escape(category), price, escape(description), escape(imageUrl)
        );
    }

    protected static String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}

// --- Derived Class (Inheritance & Method Overriding) ---
class PromotionalMenuItem extends MenuItem {
    private final double discountPercent;

    public PromotionalMenuItem(int id, String name, String category, double price, String description, String imageUrl, double discountPercent) {
        super(id, name, category, price, description, imageUrl);
        this.discountPercent = discountPercent;
    }

    @Override
    public double calculateDiscountedPrice() {
        return getPrice() * (1.0 - (discountPercent / 100.0));
    }

    @Override
    public String toJson() {
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"category\":\"%s\",\"price\":%.2f,\"discountedPrice\":%.2f,\"description\":\"%s\",\"imageUrl\":\"%s\",\"isOffer\":true}",
            getId(), escape(getName()), escape(getCategory()), getPrice(), calculateDiscountedPrice(), escape(getDescription()), escape(getImageUrl())
        );
    }
}

// --- Cart Item Data Structure ---
class OrderItem {
    private final MenuItem item;
    private final int quantity;

    public OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }

    public MenuItem getItem() { return item; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return item.calculateDiscountedPrice() * quantity; }
}

// --- Service Class (Business Logic & Collections) ---
class FoodOrderService {
    private final Map<Integer, MenuItem> menuCatalog = new HashMap<>();

    public FoodOrderService() {
        initializeMenu();
    }

    private void initializeMenu() {
        // Populating catalog using Polymorphism
        add(new MenuItem(1, "Paneer Butter Masala", "Main Course", 240.0, "Cottage cheese cubes simmered in creamy tomato gravy.", "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80"));
        add(new MenuItem(2, "Butter Naan", "Breads", 45.0, "Classic Indian tandoor leavened flatbread brushed with butter.", "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80"));
        add(new PromotionalMenuItem(3, "Veg Dum Biryani", "Biryani", 280.0, "Basmati rice layered with spiced garden vegetables and saffron.", "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80", 15.0));
        add(new MenuItem(4, "Crispy Paneer Burger", "Fast Food", 160.0, "Handmade spiced patty with chipotle mayo and crunchy lettuce.", "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80"));
        add(new PromotionalMenuItem(5, "Cold Coffee with Vanilla", "Beverages", 120.0, "Rich espresso blended with chilled milk and vanilla gelato.", "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80", 10.0));
        add(new MenuItem(6, "Gulab Jamun (2 Pcs)", "Desserts", 90.0, "Golden fried milk solids soaked in rose cardamom sugar syrup.", "https://images.unsplash.com/photo-1589119908995-c6837fa14d48?auto=format&fit=crop&w=600&q=80"));
    }

    private void add(MenuItem item) {
        menuCatalog.put(item.getId(), item);
    }

    public List<MenuItem> getAllItems() {
        return new ArrayList<>(menuCatalog.values());
    }

    public MenuItem findById(int id) {
        return menuCatalog.get(id);
    }

    // Bill Processing using Java Streams
    public Map<String, Object> processOrder(List<Map<String, Integer>> incomingItems, String customerName, String address) {
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map<String, Integer> entry : incomingItems) {
            int id = entry.get("id");
            int qty = entry.get("quantity");
            MenuItem item = findById(id);
            if (item != null && qty > 0) {
                orderItems.add(new OrderItem(item, qty));
            }
        }

        // Functional Java stream computations
        double subtotal = orderItems.stream().mapToDouble(OrderItem::getSubtotal).sum();
        double gstRate = 0.05; // 5% GST
        double tax = subtotal * gstRate;
        double deliveryFee = (subtotal > 400 || subtotal == 0) ? 0.0 : 40.0;
        double grandTotal = subtotal + tax + deliveryFee;

        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> invoice = new HashMap<>();
        invoice.put("orderId", orderId);
        invoice.put("customerName", customerName);
        invoice.put("deliveryAddress", address);
        invoice.put("subtotal", subtotal);
        invoice.put("tax", tax);
        invoice.put("deliveryFee", deliveryFee);
        invoice.put("grandTotal", grandTotal);
        invoice.put("totalItems", orderItems.stream().mapToInt(OrderItem::getQuantity).sum());
        return invoice;
    }
}

// --- Main HTTP Server Runner ---
public class FoodOrderingServer {
    private static final int PORT = 8080;
    private static final FoodOrderService service = new FoodOrderService();

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(10)); // Thread-pool executor

            // Endpoint 1: GET /api/menu
            server.createContext("/api/menu", (HttpExchange exchange) -> {
                addCorsHeaders(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                    String json = "[" + service.getAllItems().stream()
                            .map(MenuItem::toJson)
                            .collect(Collectors.joining(",")) + "]";
                    respondJson(exchange, 200, json);
                } else {
                    respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            });

            // Endpoint 2: POST /api/checkout
            server.createContext("/api/checkout", (HttpExchange exchange) -> {
                addCorsHeaders(exchange);
                if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                    try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                         BufferedReader reader = new BufferedReader(isr)) {
                        String body = reader.lines().collect(Collectors.joining());

                        // Quick parse of JSON payload
                        String customerName = extractField(body, "name", "Customer");
                        String address = extractField(body, "address", "Not provided");

                        // Extract items from JSON: id and quantity pairs
                        List<Map<String, Integer>> cart = parseCartItems(body);
                        Map<String, Object> invoice = service.processOrder(cart, customerName, address);

                        String jsonResponse = String.format(
                            "{\"orderId\":\"%s\",\"customerName\":\"%s\",\"subtotal\":%.2f,\"tax\":%.2f,\"deliveryFee\":%.2f,\"grandTotal\":%.2f,\"status\":\"Confirmed\"}",
                            invoice.get("orderId"), invoice.get("customerName"), invoice.get("subtotal"),
                            invoice.get("tax"), invoice.get("deliveryFee"), invoice.get("grandTotal")
                        );
                        respondJson(exchange, 200, jsonResponse);
                    } catch (Exception ex) {
                        respondJson(exchange, 400, "{\"error\":\"Invalid order request format\"}");
                    }
                } else {
                    respondJson(exchange, 405, "{\"error\":\"Method not allowed\"}");
                }
            });

            System.out.println("====================================================");
            System.out.println("Java Food Ordering System API running on port: " + PORT);
            System.out.println("Endpoint: http://localhost:" + PORT + "/api/menu");
            System.out.println("Endpoint: http://localhost:" + PORT + "/api/checkout");
            System.out.println("You can now run VS Code Live Server on index.html!");
            System.out.println("====================================================");
            server.start();

        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private static void respondJson(HttpExchange exchange, int statusCode, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static String extractField(String json, String field, String fallback) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx == -1) return fallback;
        int colon = json.indexOf(":", idx);
        int startQuote = json.indexOf("\"", colon);
        int endQuote = json.indexOf("\"", startQuote + 1);
        if (startQuote != -1 && endQuote != -1) {
            return json.substring(startQuote + 1, endQuote);
        }
        return fallback;
    }

    private static List<Map<String, Integer>> parseCartItems(String json) {
        List<Map<String, Integer>> result = new ArrayList<>();
        int cartIdx = json.indexOf("\"cart\"");
        if (cartIdx == -1) return result;
        int startBracket = json.indexOf("[", cartIdx);
        int endBracket = json.indexOf("]", startBracket);
        if (startBracket == -1 || endBracket == -1) return result;

        String slice = json.substring(startBracket, endBracket + 1);
        String[] objects = slice.split("},");
        for (String obj : objects) {
            try {
                int idIdx = obj.indexOf("\"id\":");
                int qtyIdx = obj.indexOf("\"quantity\":");
                if (idIdx != -1 && qtyIdx != -1) {
                    int id = Integer.parseInt(obj.substring(idIdx + 5).replaceAll("[^0-9]", "").trim());
                    int qty = Integer.parseInt(obj.substring(qtyIdx + 11).replaceAll("[^0-9]", "").trim());
                    Map<String, Integer> map = new HashMap<>();
                    map.put("id", id);
                    map.put("quantity", qty);
                    result.add(map);
                }
            } catch (Exception ignored) {}
        }
        return result;
    }
}