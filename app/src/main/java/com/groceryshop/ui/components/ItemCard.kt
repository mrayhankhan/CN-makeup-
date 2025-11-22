package com.groceryshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.groceryshop.data.models.Item

/**
 * Reusable ItemCard component for displaying items
 */
@Composable
fun ItemCard(
    item: Item,
    modifier: Modifier = Modifier,
    showStock: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Item image
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .size(80.dp),
                contentScale = ContentScale.Crop
            )
            
            // Item details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    if (showStock) {
                        Text(
                            text = "Stock: ${item.stock}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.stock > 10) 
                                MaterialTheme.colorScheme.onSurfaceVariant 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            // Action buttons
            Row(
                modifier = Modifier.align(Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = actions
            )
        }
    }
}
