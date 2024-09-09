package com.example.myrentingapplication

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.database.*
import kotlinx.coroutines.delay

//data class ImageData
sealed class ImageData {
    data class Base64Image(val base64String: String) : ImageData()
    data class UrlImage(val url: String) : ImageData()
}
//Function to show the Electronics under Electronics category and to add filters by Category, Name and Author and to apply price slider using simple linear regression
@Composable
fun ElectronicsCategory(navController: NavController) {
    var products by remember { mutableStateOf(listOf<Product>()) }
    var productTypes by remember { mutableStateOf(listOf<String>()) }
    var productNames by remember { mutableStateOf(listOf<String>()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedProductTypes by remember { mutableStateOf(setOf<String>()) }
    var selectedProductNames by remember { mutableStateOf(setOf<String>()) }
    var isProductTypeExpanded by remember { mutableStateOf(false) }
    var isProductNameExpanded by remember { mutableStateOf(false) }
    var selectedPrice by remember { mutableStateOf(50f) }
    var showSplashScreen by remember { mutableStateOf(true) }
    var brandNames by remember { mutableStateOf(listOf<String>()) }
    var selectedBrandNames by remember { mutableStateOf(setOf<String>()) }
    var isBrandNameExpanded by remember { mutableStateOf(false) }
    val model = SimpleLinearRegressionModel()

    LaunchedEffect(Unit) {
        delay(5000)
        fetchProductsFromFirebase(
            onSuccess = { fetchedProducts ->
                products = fetchedProducts
                productTypes = fetchedProducts.map { it.productType }.distinct()
                productNames = fetchedProducts.map { it.productName }.distinct()
                brandNames = fetchedProducts.map { it.brand }.distinct()
                showSplashScreen = false
            },
            onError = { error ->
                errorMessage = error.message
                Log.e("FirebaseError", "Error fetching data: ${error.message}")
                showSplashScreen = false
            }
        )
    }
    val dependentProductNames by remember {
        derivedStateOf {
            if (selectedProductTypes.isEmpty()) {
                products.map { it.productName }.distinct()
            } else {
                products.filter { selectedProductTypes.contains(it.productType) }
                    .map { it.productName }
                    .distinct()
            }
        }
    }
    val dependentProductTypes by remember {
        derivedStateOf {
            if (selectedProductNames.isEmpty()) {
                products.map { it.productType }.distinct()
            } else {
                products.filter { selectedProductNames.contains(it.productName) }
                    .map { it.productType }
                    .distinct()
            }
        }
    }

    if (showSplashScreen) {
        SplashScreen()
    } else {
        val backgroundPainter: Painter = painterResource(id = R.drawable.backgroundlandingpage)

        Box {
            Image(
                painter = backgroundPainter,
                contentDescription = "Background Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    IconButton(onClick = { navController.navigate("search_screen") }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                errorMessage?.let {
                    BasicText(text = "Error fetching data: $it")
                }

                if (products.isNotEmpty()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        ExpandableSection(
                            isExpanded = isProductTypeExpanded,
                            onExpandToggle = { isExpanded ->
                                isProductTypeExpanded = isExpanded
                            },
                            title = "Category",
                            items = dependentProductTypes,
                            selectedItems = selectedProductTypes,
                            onSelectionChange = { productType, isChecked ->
                                selectedProductTypes = if (isChecked) {
                                    selectedProductTypes + productType
                                } else {
                                    selectedProductTypes - productType
                                }
                            },
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )

                        ExpandableSection(
                            isExpanded = isProductNameExpanded,
                            onExpandToggle = { isExpanded ->
                                isProductNameExpanded = isExpanded
                            },
                            title = "Name",
                            items = dependentProductNames,
                            selectedItems = selectedProductNames,
                            onSelectionChange = { productName, isChecked ->
                                selectedProductNames = if (isChecked) {
                                    selectedProductNames + productName
                                } else {
                                    selectedProductNames - productName
                                }
                            },
                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    ExpandableSection(
                        isExpanded = isBrandNameExpanded,
                        onExpandToggle = { isExpanded ->
                            isBrandNameExpanded = isExpanded
                        },
                        title = "Brand",
                        items = brandNames,
                        selectedItems = selectedBrandNames,
                        onSelectionChange = { brandName, isChecked ->
                            selectedBrandNames = if (isChecked) {
                                selectedBrandNames + brandName
                            } else {
                                selectedBrandNames - brandName
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    PriceSlider(
                        selectedPrice = selectedPrice,
                        onPriceChange = { newValue -> selectedPrice = newValue }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    val filteredProducts = products.filter { product ->
                        val isWithinPriceRange = product.pricePerDay <= selectedPrice.toInt()
                        val predictedRelevance = model.predictRelevanceScore(product.pricePerDay, selectedPrice.toInt())
                        (selectedProductTypes.isEmpty() || selectedProductTypes.contains(product.productType)) &&
                                (selectedProductNames.isEmpty() || selectedProductNames.contains(product.productName)) &&
                                (selectedBrandNames.isEmpty() || selectedBrandNames.contains(product.brand)) &&
                                isWithinPriceRange && predictedRelevance >= 0.01f
                    }
                    LazyColumn {
                        items(filteredProducts) { product ->
                            ProductListItem(
                                product = product,
                                onProductClick = { selectedProduct ->
                                    navController.navigate("product_detail/${selectedProduct.productName}")
                                }
                            )
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun SplashScreen() {
    val startAnimation = remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnimation.value = true
    }
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = 2000
        )
    )
    val scale by animateFloatAsState(
        targetValue = if (startAnimation.value) 1.2f else 1f,
        animationSpec = tween(
            durationMillis = 2000
        )
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.splashwallpaper),
            contentDescription = "Background Image",
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Splash Screen Image",
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .scale(scale)
                .alpha(alpha)
        )
    }
}

@Composable
fun ExpandableSection(
    isExpanded: Boolean,
    onExpandToggle: (Boolean) -> Unit,
    title: String,
    items: List<String>,
    selectedItems: Set<String>,
    onSelectionChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onExpandToggle(!isExpanded) }
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                contentDescription = if (isExpanded) "Collapse" else "Expand"
            )
        }
        if (isExpanded) {
            LazyColumn {
                items(items) { item ->
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = selectedItems.contains(item),
                            onCheckedChange = { isChecked ->
                                onSelectionChange(item, isChecked)
                            }
                        )
                        Text(
                            text = item,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    onProductClick: (Product) -> Unit
) {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .border(width = 2.dp, color = Color.Gray, shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
            .clickable { onProductClick(product) }
    ) {
        Row {
            val imageData = parseImageData(product.productImage)

            when (imageData) {
                is ImageData.Base64Image -> {
                    imageData.base64String.decodeBase64()?.let { imageBitmap ->
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Product Image",
                            modifier = Modifier
                                .size(100.dp)
                                .padding(end = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
                is ImageData.UrlImage -> {
                    val painter = rememberImagePainter(data = imageData.url)
                    Image(
                        painter = painter,
                        contentDescription = "Product Image",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(end = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = product.productName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 3.dp, bottom = 4.dp)
                )
                Text(
                    text = product.productType,
                    modifier = Modifier.padding(top = 3.dp)
                )
                Text(
                    text = "Price: £${product.pricePerDay}/day",
                    color = Color.Black,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }
    }
}



@Composable
fun PriceSlider(
    selectedPrice: Float,
    onPriceChange: (Float) -> Unit
) {
    Column {
        Text(
            text = "Price: £${selectedPrice.toInt()}",
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = selectedPrice,
            onValueChange = onPriceChange,
            valueRange = 0f..100f,
            steps = 100,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
data class Product(
    val ownerName: String = "",
    val ownerEmail: String = "",
    val ownerNumber: String = "",
    val ownerAddress: String = "",
    val category: String = "",
    val productName: String = "",
    val productType: String = "",
    val brand: String = "",
    val minimalRentalPeriod: String = "",
    val pricePerDay: Int = 0,
    val productDescription: String = "",
    var productImage: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "ownerName" to ownerName,
            "ownerEmail" to ownerEmail,
            "ownerNumber" to ownerNumber,
            "ownerAddress" to ownerAddress,
            "SelectedCategory" to category,
            "productName" to productName,
            "productType" to productType,
            "brand" to brand,
            "minimalRentalPeriod" to minimalRentalPeriod,
            "pricePerDay" to pricePerDay,
            "productDescription" to productDescription,
            "productImage" to productImage
        )
    }
}
fun parseImageData(imageString: String): ImageData {
    return if (imageString.startsWith("http")) {
        ImageData.UrlImage(imageString)
    } else {
        ImageData.Base64Image(imageString)
    }
}

fun String.decodeBase64(): ImageBitmap? {
    return try {
        val decodedBytes = Base64.decode(this, Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        bitmap.asImageBitmap()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun fetchProductsFromFirebase(onSuccess: (List<Product>) -> Unit, onError: (Exception) -> Unit) {
    val database = FirebaseDatabase.getInstance()
    val ref = database.getReference("products")

    ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val productList = mutableListOf<Product>()
            for (dataSnapshot in snapshot.children) {
                val product = dataSnapshot.getValue(Product::class.java)
                if (product != null && product.category == "Electronics") {
                    productList.add(product)
                }
            }
            onSuccess(productList)
        }

        override fun onCancelled(error: DatabaseError) {
            onError(Exception(error.message))
        }
    })
}


class SimpleLinearRegressionModel {
    fun predictRelevanceScore(productPrice: Int, selectedPrice: Int): Float {
        return if (productPrice <= selectedPrice) {
            1f
        } else {
            1f - ((productPrice - selectedPrice).toFloat() / selectedPrice.toFloat()).coerceIn(0f, 1f)
        }
    }
}