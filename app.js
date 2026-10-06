/**
 * ONLINE FOOD ORDERING SYSTEM - CLIENT CONTROLLER
 * Connects with Java Backend (FoodOrderingServer.java) on localhost:8080.
 * Automatically falls back to offline catalog if backend server is not running.
 */

const API_BASE = 'http://localhost:8080/api';

// Fallback in case user runs Live Server before running the Java server
const FALLBACK_MENU = [
  { id: 1, name: "Paneer Butter Masala", category: "Main Course", price: 240.0, description: "Cottage cheese cubes simmered in creamy tomato gravy.", imageUrl: "https://images.unsplash.com/photo-1631452180519-c014fe946bc7?auto=format&fit=crop&w=600&q=80" },
  { id: 2, name: "Butter Naan", category: "Breads", price: 45.0, description: "Classic Indian tandoor leavened flatbread brushed with butter.", imageUrl: "https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=600&q=80" },
  { id: 3, name: "Veg Dum Biryani", category: "Biryani", price: 280.0, discountedPrice: 238.0, isOffer: true, description: "Basmati rice layered with spiced garden vegetables and saffron.", imageUrl: "https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=600&q=80" },
  { id: 4, name: "Crispy Paneer Burger", category: "Fast Food", price: 160.0, description: "Handmade spiced patty with chipotle mayo and crunchy lettuce.", imageUrl: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80" },
  { id: 5, name: "Cold Coffee with Vanilla", category: "Beverages", price: 120.0, discountedPrice: 108.0, isOffer: true, description: "Rich espresso blended with chilled milk and vanilla gelato.", imageUrl: "https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=600&q=80" },
  { id: 6, name: "Gulab Jamun (2 Pcs)", category: "Desserts", price: 90.0, description: "Golden fried milk solids soaked in rose cardamom sugar syrup.", imageUrl: "https://images.unsplash.com/photo-1589119908995-c6837fa14d48?auto=format&fit=crop&w=600&q=80" }
];

// State
let foodItems = [];
let cart = []; // Array of { id, name, price, quantity }
let activeCategory = 'all';
let searchQuery = '';

// DOM Elements
const foodGrid = document.getElementById('foodGrid');
const cartCount = document.getElementById('cartCount');
const cartDrawer = document.getElementById('cartDrawer');
const cartOverlay = document.getElementById('cartOverlay');
const cartItemsContainer = document.getElementById('cartItemsContainer');
const backendBadge = document.getElementById('backendBadge');

// Price Elements
const billSubtotal = document.getElementById('billSubtotal');
const billTax = document.getElementById('billTax');
const billDelivery = document.getElementById('billDelivery');
const billGrandTotal = document.getElementById('billGrandTotal');

// Modals
const checkoutModal = document.getElementById('checkoutModal');
const receiptModal = document.getElementById('receiptModal');
const receiptDetails = document.getElementById('receiptDetails');

// 1. Fetch Menu from Java API
async function fetchMenu() {
  try {
    const res = await fetch(`${API_BASE}/menu`);
    if (!res.ok) throw new Error("Backend responded with error");
    foodItems = await res.json();
    backendBadge.textContent = "Java Backend Connected";
    backendBadge.classList.remove('fallback');
  } catch (err) {
    console.warn("Java Backend not detected at localhost:8080. Using built-in schema.", err);
    foodItems = FALLBACK_MENU;
    backendBadge.textContent = "Java Offline (Fallback Active)";
    backendBadge.classList.add('fallback');
  }
  renderMenu();
}

// 2. Render Food Cards
function renderMenu() {
  const filtered = foodItems.filter(item => {
    const matchCat = activeCategory === 'all' || item.category === activeCategory;
    const matchSearch = item.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                        item.description.toLowerCase().includes(searchQuery.toLowerCase());
    return matchCat && matchSearch;
  });

  if (filtered.length === 0) {
    foodGrid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--text-muted); padding: 3rem;">No food items found matching your filter.</p>`;
    return;
  }

  foodGrid.innerHTML = filtered.map(item => {
    const effectivePrice = item.discountedPrice || item.price;
    return `
      <div class="food-card">
        <div class="food-card-img-wrapper">
          <img src="${item.imageUrl}" alt="${item.name}" class="food-card-img" loading="lazy">
          ${item.isOffer ? `<span class="offer-tag">Special Offer</span>` : ''}
        </div>
        <div class="food-card-content">
          <span class="card-category">${item.category}</span>
          <h3>${item.name}</h3>
          <p>${item.description}</p>
          <div class="card-footer">
            <div class="price-box">
              ${item.discountedPrice ? `<span class="original-price">₹${item.price.toFixed(2)}</span>` : ''}
              <span class="current-price">₹${effectivePrice.toFixed(2)}</span>
            </div>
            <button class="add-btn" onclick="addToCart(${item.id})">+ Add</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

// 3. Cart Logic
function addToCart(id) {
  const item = foodItems.find(i => i.id === id);
  if (!item) return;

  const existing = cart.find(c => c.id === id);
  const effectivePrice = item.discountedPrice || item.price;

  if (existing) {
    existing.quantity += 1;
  } else {
    cart.push({
      id: item.id,
      name: item.name,
      price: effectivePrice,
      quantity: 1
    });
  }
  updateCartUI();
}

function changeQuantity(id, delta) {
  const item = cart.find(c => c.id === id);
  if (!item) return;

  item.quantity += delta;
  if (item.quantity <= 0) {
    cart = cart.filter(c => c.id !== id);
  }
  updateCartUI();
}

function updateCartUI() {
  const totalCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  cartCount.textContent = totalCount;

  if (cart.length === 0) {
    cartItemsContainer.innerHTML = `<div class="empty-cart-msg">Your food cart is empty.<br>Pick some delicious items!</div>`;
    billSubtotal.textContent = "₹0.00";
    billTax.textContent = "₹0.00";
    billDelivery.textContent = "₹0.00";
    billGrandTotal.textContent = "₹0.00";
    return;
  }

  cartItemsContainer.innerHTML = cart.map(item => `
    <div class="cart-item-row">
      <div class="item-details">
        <h4>${item.name}</h4>
        <span>₹${(item.price * item.quantity).toFixed(2)}</span>
      </div>
      <div class="qty-controls">
        <button class="qty-btn" onclick="changeQuantity(${item.id}, -1)">-</button>
        <span class="qty-count">${item.quantity}</span>
        <button class="qty-btn" onclick="changeQuantity(${item.id}, 1)">+</button>
      </div>
    </div>
  `).join('');

  // Local bill estimation
  const subtotal = cart.reduce((sum, i) => sum + (i.price * i.quantity), 0);
  const tax = subtotal * 0.05;
  const delivery = subtotal > 400 ? 0 : 40;
  const grandTotal = subtotal + tax + delivery;

  billSubtotal.textContent = `₹${subtotal.toFixed(2)}`;
  billTax.textContent = `₹${tax.toFixed(2)}`;
  billDelivery.textContent = subtotal > 400 ? "FREE" : `₹${delivery.toFixed(2)}`;
  billGrandTotal.textContent = `₹${grandTotal.toFixed(2)}`;
}

// 4. Cart Drawer Toggles
document.getElementById('cartOpenBtn').onclick = () => {
  cartDrawer.classList.add('open');
  cartOverlay.classList.add('open');
};

const closeCart = () => {
  cartDrawer.classList.remove('open');
  cartOverlay.classList.remove('open');
};

document.getElementById('cartCloseBtn').onclick = closeCart;
cartOverlay.onclick = closeCart;

// 5. Checkout Modal Toggles
document.getElementById('openCheckoutModalBtn').onclick = () => {
  if (cart.length === 0) {
    alert("Please add items to your cart first!");
    return;
  }
  closeCart();
  checkoutModal.classList.add('open');
};

document.getElementById('modalCloseBtn').onclick = () => {
  checkoutModal.classList.remove('open');
};

// 6. Submit Order to Java Backend
document.getElementById('checkoutForm').onsubmit = async (e) => {
  e.preventDefault();
  const name = document.getElementById('custName').value;
  const address = document.getElementById('custAddress').value;

  const payload = {
    name: name,
    address: address,
    cart: cart.map(i => ({ id: i.id, quantity: i.quantity }))
  };

  let receiptData = null;

  try {
    const res = await fetch(`${API_BASE}/checkout`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    if (res.ok) {
      receiptData = await res.json();
    } else {
      throw new Error();
    }
  } catch (err) {
    // Client-side fallback invoice if Java server wasn't running
    const subtotal = cart.reduce((sum, i) => sum + (i.price * i.quantity), 0);
    const tax = subtotal * 0.05;
    const delivery = subtotal > 400 ? 0 : 40;
    receiptData = {
      orderId: "ORD-" + Math.random().toString(36).substr(2, 6).toUpperCase(),
      customerName: name,
      subtotal: subtotal,
      tax: tax,
      deliveryFee: delivery,
      grandTotal: subtotal + tax + delivery,
      status: "Confirmed (Local Engine)"
    };
  }

  // Display Receipt
  receiptDetails.innerHTML = `
    <div><strong>Order ID:</strong> ${receiptData.orderId}</div>
    <div><strong>Customer:</strong> ${receiptData.customerName}</div>
    <div><strong>Items Count:</strong> ${cart.reduce((s, i) => s + i.quantity, 0)}</div>
    <div><strong>Subtotal:</strong> ₹${receiptData.subtotal.toFixed(2)}</div>
    <div><strong>GST (5%):</strong> ₹${receiptData.tax.toFixed(2)}</div>
    <div><strong>Delivery Fee:</strong> ₹${receiptData.deliveryFee.toFixed(2)}</div>
    <div style="border-top:1px dashed var(--border-color); padding-top:4px;"><strong>Grand Total:</strong> ₹${receiptData.grandTotal.toFixed(2)}</div>
  `;

  checkoutModal.classList.remove('open');
  receiptModal.classList.add('open');

  // Reset cart
  cart = [];
  updateCartUI();
};

document.getElementById('doneReceiptBtn').onclick = () => {
  receiptModal.classList.remove('open');
};

// 7. Filtering & Search Listeners
document.getElementById('categoryContainer').onclick = (e) => {
  if (e.target.classList.contains('filter-btn')) {
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    e.target.classList.add('active');
    activeCategory = e.target.dataset.category;
    renderMenu();
  }
};

document.getElementById('searchInput').oninput = (e) => {
  searchQuery = e.target.value;
  renderMenu();
};

// Initial load
fetchMenu();