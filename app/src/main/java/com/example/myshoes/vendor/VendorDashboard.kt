package com.example.myshoes.vendor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myshoes.MainActivity
import com.example.myshoes.vendor.models.ProductsObj
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class VendorDashboard : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            com.example.myshoes.ui.theme.MyShoesTheme() {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(topBar = {
                        TopAppBar(backgroundColor = Color.Black,
                            title = {
                                Text(
                                    text = "Vendor Dashboard",
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            })
                    }) {
                        Column(modifier = Modifier.padding(it)) {
                            // getting firebase instance and the database reference
                            val firebaseDatabase = FirebaseDatabase.getInstance()
                            val databaseReference = firebaseDatabase.getReference("ProductDB")
                            productForm(LocalContext.current,databaseReference)
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun productForm(context: Context, databaseReference: DatabaseReference){
    //variables to store the users input
    val productname = remember{ mutableStateOf(TextFieldValue()) }
    val productcontact = remember{ mutableStateOf(TextFieldValue()) }
    val productimage = remember{ mutableStateOf(TextFieldValue()) }
    val productprice = remember{ mutableStateOf(TextFieldValue()) }
    // composable set of textfields for users to add details
    Column(modifier = Modifier
        .fillMaxHeight()
        .fillMaxWidth()
        .background(Color.White),
        verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Add your products to the Product Store",modifier = Modifier.padding(7.dp), style= TextStyle(
            color = Color.Black, fontSize = 10.sp
        ), fontWeight = FontWeight.Bold)

        // text fields
        TextField(value = productname.value, onValueChange = {productname.value = it}, 
            placeholder = { Text(text = "Enter the Product Name")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp), singleLine = true
            )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(value = productcontact.value, onValueChange = {productcontact.value = it},
            placeholder = { Text(text = "Enter the Product Contact")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(value = productimage.value, onValueChange = {productimage.value = it},
            placeholder = { Text(text = "Enter the Image Url")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(5.dp))
        TextField(value = productprice.value, onValueChange = {productprice.value = it},
            placeholder = { Text(text = "Enter the Product Price")}, modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            textStyle = TextStyle(color = Color.Black, fontSize = 15.sp), singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))
        
        Button(onClick = {
//            push our data to the realtime database
            // use the push method to generate a unique key for the record
            val newProduct_Reference = databaseReference.push()
            // key / unique identifier
            val productId = newProduct_Reference.key
            val productObj = productId?.let {
                ProductsObj(
                    it,productname.value.text,productcontact.value.text,productimage.value.text,
                    productprice.value.text)
            }

            // we use a class in firebase called the addValueEventListener
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    newProduct_Reference.setValue(productObj)
                    Toast.makeText(context,"Product has been added successfully!!,", Toast.LENGTH_LONG).show()
                    Log.d("Product Push",snapshot.toString())
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                    Toast.makeText(context,"Product failed to be  added!!,", Toast.LENGTH_LONG).show()
                    Log.d("Product Push",error.message)
                }

            })
            
        }, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            
            Text(text = "Add Product Details", modifier = Modifier.padding(5.dp))
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround){

            val activitylauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()){ activityResult ->

            }
            Button(colors = ButtonDefaults.buttonColors(backgroundColor = Color.Yellow),
                onClick = {
                    val intent = Intent(context,ViewInventory::class.java)
                    activitylauncher.launch(intent)
                }) {

                Text(text = "View Inventory", modifier = Modifier.padding(5.dp))
            }

            //logout

            Button(colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                onClick = {
                     FirebaseAuth.getInstance().signOut()
                     changeUi(context,activitylauncher)
                }) {
                Text(text = "Logout", modifier = Modifier.padding(5.dp))
            }
        }
    }
}

fun changeUi(
    context: Context,
    activitylauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val intent = Intent(context,MainActivity::class.java)
    activitylauncher.launch(intent)
}





























