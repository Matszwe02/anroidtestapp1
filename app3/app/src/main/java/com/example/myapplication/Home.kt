package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


data class DataEntry
(
    var name: String,
    var lastname: String,
    var index: Int
)




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, context: Context) {

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")


    val authState = authViewModel.authState.observeAsState()

    var name by remember {
        mutableStateOf("None")
    }
    var lastname by remember {
        mutableStateOf("None")
    }
    var index by remember {
        mutableStateOf(0)
    }

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Fetch data when the component is composed
    remember(authViewModel) {
        db.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("name").getValue(String::class.java) ?: ""
            lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
            index = snapshot.child("index").getValue(Int::class.java)?:0

        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        val nametext = Text(text = "First Name: " + name)
        Text(text = "Last Name: " + lastname)
        Text(text = "Index Number: " + index)


        Button(onClick = {
            db.get().addOnSuccessListener { snapshot ->
                name = snapshot.child("name").getValue(String::class.java) ?: ""
                lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
                index = snapshot.child("index").getValue(Int::class.java) ?: 0



            }.addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to refresh data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Refresh")
        }


        Button(onClick = {
            navController.navigate("newinfo")
            {
                popUpTo("home") { inclusive = true }
            }
        }) {
            Text(text = "Change Data")
        }


        TextButton(onClick = {
            authViewModel.signout()
        }) {
            Text(text = "Sign out")
        }
    }

}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewinfoPage(modifier: Modifier, context: Context, navController: NavController, authViewModel: AuthViewModel)
{

    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val db = FirebaseDatabase.getInstance("https://application-191ac-default-rtdb.europe-west1.firebasedatabase.app").getReference("users/$userId")




    var name by remember {
        mutableStateOf("None")
    }
    var lastname by remember {
        mutableStateOf("None")
    }
    var index by remember {
        mutableStateOf(0)
    }

    // Fetch data when the component is composed
    remember(authViewModel) {
        db.get().addOnSuccessListener { snapshot ->
            name = snapshot.child("name").getValue(String::class.java) ?: ""
            lastname = snapshot.child("lastname").getValue(String::class.java) ?: ""
            index = snapshot.child("index").getValue(Int::class.java)?:0

        }.addOnFailureListener { exception ->
            Toast.makeText(context, "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        OutlinedTextField(value = name, onValueChange = {name = it}, placeholder = { Text(text = "First Name")})

        OutlinedTextField(value = lastname, onValueChange = {lastname = it}, placeholder = { Text(text = "Last Name")})

        OutlinedTextField(value = index.toString(), onValueChange = {index = it.toIntOrNull()?:0}, placeholder = { Text(text = "Index Number")})


        Button(onClick = {
            val entry = DataEntry(
                name,
                lastname,
                index
            )

            Log.d("FirebaseDB", "Reference: ${db.path}")
            Log.d("FirebaseDB", "Data: $entry")

            db.setValue(entry)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Updated successfully", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Failed to update data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text(text = "Save")
        }


    }
}