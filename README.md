# ⚡ BiteWave: Online Food Ordering System

![Java](https://img.shields.io/badge/Java-Core-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![JavaScript](https://img.shields.io/badge/JavaScript-323330?style=for-the-badge&logo=javascript&logoColor=F7DF1E)

A sleek, full-stack online food ordering web application built as an engineering coursework project. It demonstrates fundamental software architecture by pairing a custom-built, dependency-free Java backend with a modern, responsive frontend featuring a vintage-elegant aesthetic. 

## ✨ Key Features

**Backend (Core Java Engine)**
*   **Zero-Dependency Architecture:** Powered entirely by the standard `com.sun.net.httpserver` library—no Spring, Tomcat, or Maven required.
*   **OOP Principles:** Extensively utilizes Abstraction, Encapsulation, Polymorphism, and Inheritance for menu item modeling.
*   **Modern Java Capabilities:** Implements Java Collections Framework (`ArrayList`, `HashMap`) and Streams API with Lambda expressions for dynamic bill computation and tax processing.
*   **Multi-threading & CORS:** Handles concurrent HTTP requests via an integrated thread-pool executor with built-in Cross-Origin Resource Sharing (CORS) support.

**Frontend (Client UI)**
*   **Elegant UI/UX:** Styled with a custom muted forest green, cream, and rust palette featuring classic typography (`League Gothic` & `Playfair Display SC`).
*   **Dynamic Cart Management:** Real-time quantity updates, subtotal calculations, and cart validation.
*   **Live Filtering & Search:** Instantly filter delicacies by category or search by keywords.
*   **Smart Fallback Mode:** The client-side JavaScript automatically detects if the Java server is offline and switches to a local mock-catalog, ensuring the UI never breaks during demonstrations.

## 🗂️ Project Structure

```text
📦 online-food-ordering
 ┣ 📜 FoodOrderingServer.java   # The complete backend Java API and business logic
 ┣ 📜 index.html                # Main application layout and modal structures
 ┣ 📜 style.css                 # Custom styling, animations, and color palette
 ┗ 📜 app.js                    # Client-side routing, cart state, and API fetching
