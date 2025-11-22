# Firebase Storage - Placeholder Image Setup

## Option 1: Using Web URLs (Easiest for Demo)

The demo data already uses placeholder image URLs from `via.placeholder.com`. No additional setup needed!

```kotlin
// Already implemented in Repository.kt
imageUrl = "https://via.placeholder.com/300x200?text=$itemName"
```

## Option 2: Upload Custom Images to Firebase Storage

### Step 1: Prepare Images

Create a folder with 10-15 placeholder images:
```bash
mkdir placeholder_images
# Add your images: item1.jpg, item2.jpg, etc.
```

### Step 2: Upload via Firebase Console (Manual)

1. Go to Firebase Console → Storage
2. Click "Upload file"
3. Select all images
4. Note the public URLs

### Step 3: Upload via Firebase CLI (Automated)

```bash
# Install Firebase CLI
npm install -g firebase-tools

# Login
firebase login

# Initialize (if not done)
firebase init storage

# Upload images
firebase storage:upload placeholder_images/item1.jpg /items/item1.jpg
firebase storage:upload placeholder_images/item2.jpg /items/item2.jpg
# ... repeat for all images

# Or use a script:
for file in placeholder_images/*.jpg; do
  filename=$(basename "$file")
  firebase storage:upload "$file" "/items/$filename"
done
```

### Step 4: Get Public URLs

After uploading, get download URLs:

```bash
# Using gsutil (Google Cloud SDK)
gsutil ls -L gs://YOUR-PROJECT-ID.appspot.com/items/
```

Or programmatically in Kotlin:

```kotlin
val storage = FirebaseStorage.getInstance()
val imagesRef = storage.reference.child("items")

suspend fun getImageUrls(): List<String> {
    val urls = mutableListOf<String>()
    val result = imagesRef.listAll().await()
    
    result.items.forEach { item ->
        val url = item.downloadUrl.await().toString()
        urls.add(url)
    }
    
    return urls
}
```

### Step 5: Update Repository Demo Data

Replace the placeholder URL generation in `Repository.kt`:

```kotlin
// Add this companion object property
companion object {
    val DEMO_IMAGE_URLS = listOf(
        "https://firebasestorage.googleapis.com/v0/b/YOUR-PROJECT.appspot.com/o/items%2Fitem1.jpg?alt=media",
        "https://firebasestorage.googleapis.com/v0/b/YOUR-PROJECT.appspot.com/o/items%2Fitem2.jpg?alt=media",
        // ... add all your URLs
    )
}

// Then in createItemsForShop():
imageUrl = DEMO_IMAGE_URLS[i % DEMO_IMAGE_URLS.size]
```

## Option 3: Dynamic Image Upload (Already Implemented!)

The `AddEditItemScreen` already supports image uploads:

1. Owner clicks "Select Image"
2. Picks image from device
3. `OwnerViewModel.uploadImage()` uploads to Firebase Storage
4. Returns download URL
5. Saves to Firestore item document

**The upload logic is in `OwnerViewModel.kt`:**

```kotlin
suspend fun uploadImage(imageUri: Uri): String {
    val filename = "items/${UUID.randomUUID()}.jpg"
    val storageRef = storage.reference.child(filename)
    
    storageRef.putFile(imageUri).await()
    return storageRef.downloadUrl.await().toString()
}
```

## Free Tier Limits

Firebase Storage free tier (Spark Plan):
- **5 GB storage**
- **1 GB/day downloads**
- **50,000 reads/day**
- **20,000 writes/day**

**Sufficient for demo purposes!**

## Sample Images for Demo

You can use these free placeholder services:
- `https://via.placeholder.com/300x200?text=Item+Name`
- `https://picsum.photos/300/200` (random images)
- `https://source.unsplash.com/300x200/?grocery` (Unsplash random)

## Current Implementation

The app currently uses:
1. **Demo data**: Uses `via.placeholder.com` URLs
2. **Owner uploads**: Saves to Firebase Storage at `/items/{UUID}.jpg`
3. **Automatic handling**: If no image selected, uses placeholder

**No additional setup required for basic demo!**
