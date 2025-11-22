# 🎬 Quick Demo Guide

## 🔐 Login Credentials

### Shop Owners (5 available)
```
Email: owner1@grocery.com → owner5@grocery.com
Password: owner123
```

### Customers (3 available)
```
Email: customer1@grocery.com → customer3@grocery.com
Password: customer123
```

## 📱 Demo Script

### Part 1: Owner Features (5 minutes)

1. **Login as Owner**
   - Email: `owner1@grocery.com`
   - Password: `owner123`

2. **View Dashboard**
   - ✅ Shows 30 items for this shop
   - ✅ Inventory summary displayed
   - ✅ Items show image, name, price, stock

3. **Add New Item**
   - Click "+" button
   - Fill: Name, Description, Price, Stock
   - (Optional) Select image
   - Click "Add Item"
   - ✅ Item appears in dashboard

4. **Edit Item**
   - Click "Edit" on any item
   - Change price or stock
   - Click "Update Item"
   - ✅ Changes reflected immediately

5. **View Orders**
   - Click "View Orders"
   - See customer orders (if any)
   - Mark as dispatched → delivered

---

### Part 2: Customer Features (10 minutes)

1. **Login as Customer**
   - Email: `customer1@grocery.com`
   - Password: `customer123`

2. **Enable Location (Optional)**
   - Click "Enable Location"
   - Grant permissions
   - ✅ Distances calculated automatically

3. **Browse by Shop**
   - "By Shop" tab selected
   - ✅ 5 shops displayed
   - ✅ Each shows distance (if location enabled)
   - Click "View Items" on any shop
   - ✅ 30 items displayed

4. **Browse All Items**
   - Switch to "All Items" tab
   - ✅ All 150 items displayed
   - ✅ Each shows shop name

5. **Add to Cart**
   - Select any item
   - Adjust quantity with +/- buttons
   - Click "Add to Cart"
   - ✅ Cart badge updates
   - Add 3-5 different items

6. **View Cart**
   - Click cart icon (top right)
   - ✅ All items displayed
   - ✅ Quantities editable
   - ✅ Total calculated

7. **Checkout**
   - Select a shop (important!)
   - Enter location: `40.7128, -74.0060` (or use GPS)
   - ✅ Distance calculated
   - ✅ Delivery time estimated
   - Click "Checkout"
   - Review: distance, ETA, total
   - Click "Place Order"
   - ✅ Order succeeds
   - ✅ Cart cleared
   - ✅ Stock decremented

---

### Part 3: Atomic Transaction Demo (5 minutes)

**Scenario: Test stock validation**

1. **Customer Session**
   - Login as `customer1@grocery.com`
   - Add 10 units of "Apple 1" to cart
   - **Don't checkout yet!**

2. **Owner Session (Different Device/Emulator)**
   - Login as `owner1@grocery.com`
   - Find "Apple 1"
   - Edit → Set stock to 5
   - Save

3. **Back to Customer**
   - Go to cart
   - Try to checkout
   - ✅ **Error: "Insufficient stock for Apple 1"**
   - ✅ **No partial stock deduction**
   - ✅ **Transaction rolled back**

---

## 🎯 Key Points to Highlight

### 1. Demo Data
- ✅ **5 shops** automatically created
- ✅ **30 items per shop** = 150 total
- ✅ **Unique IDs** for all items
- ✅ **Images** (placeholder URLs)
- ✅ **Varied prices and stock**

### 2. Owner Features
- ✅ Full CRUD on items
- ✅ Image upload support
- ✅ Inventory dashboard
- ✅ Order management
- ✅ Stock and price updates

### 3. Customer Features
- ✅ Browse by shop or all items
- ✅ Location-based distance
- ✅ Add to cart with quantities
- ✅ Atomic checkout
- ✅ Delivery time estimation

### 4. Technical Highlights
- ✅ **Atomic transactions** (Firestore)
- ✅ **GPS integration** (FusedLocationProvider)
- ✅ **Haversine distance** calculation
- ✅ **Firebase Storage** for images
- ✅ **Material 3 UI** (Jetpack Compose)
- ✅ **MVVM architecture**

## ⚡ Quick Testing Checklist

- [ ] Demo data auto-creates on first launch
- [ ] 5 owner accounts work
- [ ] 3 customer accounts work
- [ ] Owner can add/edit/delete items
- [ ] Owner can view orders
- [ ] Customer sees all 150 items
- [ ] Distance calculation works
- [ ] Cart updates correctly
- [ ] Checkout validates stock atomically
- [ ] Order fails if stock insufficient
- [ ] No partial stock deduction
- [ ] Images display correctly
- [ ] Navigation works smoothly

## 🐛 Troubleshooting During Demo

| Issue | Solution |
|-------|----------|
| Demo data not created | Check internet, wait 10 seconds, restart app |
| Location not working | Manually enter: `40.7128, -74.0060` |
| Images not loading | Normal - uses placeholder URLs |
| Can't login | Verify credentials, check Firebase Auth enabled |
| Stock not updating | Refresh by going back and forward |

## 📊 Demo Metrics to Show

- **Total Items**: 150 (5 shops × 30 items)
- **Stock Range**: 50-100 units per item
- **Price Range**: $2.00 - $12.00
- **Delivery Calculation**: 10 min base + (distance/20 km/h × 60)
- **Transaction Safety**: All-or-nothing stock updates

## 🎓 Assignment Compliance

✅ All 14 prompts implemented  
✅ All technical requirements met  
✅ Demo data exceeds minimums  
✅ Atomic transactions working  
✅ Location features complete  

---

**Demo Duration**: 15-20 minutes  
**Preparation**: Ensure google-services.json is in place and app is built
