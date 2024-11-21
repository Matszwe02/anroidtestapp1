package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

//import androidx.navigation.compose.rememberNavController


class MainActivity : ComponentActivity() {

    /**
     * Function to convert string to title case
     *
     * @param string - Passed string
     */
    fun toTitleCase(string: String): String {

        var whiteSpace = true
        val builder = StringBuilder(string) // String builder to store string
        val builderLength = builder.length

        // Loop through builder
        for (i in 0 until builderLength) {
            val c = builder[i] // Get character at builders position
            if (whiteSpace) {

                // Check if character is not white space
                if (!Character.isWhitespace(c)) {

                    // Convert to title case and leave whitespace mode.
                    builder.setCharAt(i, c.titlecaseChar())
                    whiteSpace = false
                }
            } else if (Character.isWhitespace(c)) {
                whiteSpace = true // Set character is white space
            } else {
                builder.setCharAt(i, c.lowercaseChar()) // Set character to lowercase
            }
        }
        return builder.toString() // Return builders text
    }



    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {

        var splash = true

        super.onCreate(savedInstanceState)



        installSplashScreen().setKeepOnScreenCondition{splash}

        lifecycleScope.launch {
            delay(1000)
            splash=false
        }


        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                var name = ""
                var age = 0
                var gender = 'X'
                var height = 0
                var weight = 0f

                NavHost(navController = navController, startDestination = "screen1")
                {
                    composable("screen1")
                    {
                        entry ->1
                        name = entry.savedStateHandle.get<String>("name")?:""
                        age = entry.savedStateHandle.get<Int>("age")?:0
                        gender = entry.savedStateHandle.get<Char>("gender")?:'X'
                        height = entry.savedStateHandle.get<Int>("height")?:0
                        weight = entry.savedStateHandle.get<Float>("weight")?:0f

                        Column(modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp))
                        {

                            Column(modifier = Modifier
                                .padding(32.dp)
                                .align(Alignment.CenterHorizontally)){

                                if (name != "")
                                    Text(text = "Hello, " + name + "!")
                                if (age > 0)
                                    Text(text = "Your age is " + age.toString())

                                if (gender != 'X')
                                {
                                    var x = "male"
                                    if (gender == 'F')
                                        x = "fe" + x

                                    Text(text = "You are " + x)
                                }

                                if (height > 0)
                                    Text(text = "You are " + height + " cm high")

                                if (weight > 0)
                                    Text(text = "You weight " + weight + " kg")

                            }

                            Button(onClick =
                            {
                                navController.navigate("screen2")
                            }
                            ) {
                                Text(text = "Set name and age")
                            }
                            Button(onClick =
                            {
                                navController.navigate("screen3")
                            }
                            ) {
                                Text(text = "Set height and weight")
                            }
                            Button(onClick =
                            {
                                navController.navigate("screen4")
                            }
                            ) {
                                Text(text = "Calculate BMI")
                            }
                        }
                    }

                    composable("screen2")
                    {
//                        entry ->
//                        val text_default = entry.savedStateHandle.get<String>("my_text")?:"none"
//                        val age_default = entry.savedStateHandle.get<String>("age_text")?:"0"

                        Column (
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp))
                        {

                            var name_var by remember{
                                mutableStateOf(name)
                            }
                            var nullableAge = age.toString()
                            if (nullableAge == "0") nullableAge = ""
                            var age_var by remember {
                                mutableStateOf(nullableAge)
                            }
                            var gender_var by remember {
                                mutableStateOf(gender)
                            }


                            val pattern = remember { Regex("^[a-zA-Z]+\$") }
                            OutlinedTextField(value = name_var, onValueChange = {if (it.isEmpty() || it.matches(pattern)) name_var = toTitleCase(it)}, modifier = Modifier.width(300.dp), singleLine = true, label = { Text("Your name") })
                            OutlinedTextField(value = age_var, onValueChange = {if (it.isEmpty()) age_var = it else if (it.isDigitsOnly()) age_var = min(max(it.toInt(), 0), 99).toString()}, modifier = Modifier.width(300.dp), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), label = { Text("Your age") })

                            Row (verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = gender_var == 'M', onClick = { gender_var = 'M' })
                                Text("Male")
                            }

                            Row (verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = gender_var == 'F', onClick = { gender_var = 'F' })
                                Text(text = "Female")
                            }

                            Button(onClick =
                            {
                                var handle = navController.previousBackStackEntry?.savedStateHandle
                                handle?.set("name", name_var)
                                handle?.set("age", age_var.toIntOrNull()?:0)
                                handle?.set("gender", gender_var)
                                navController.popBackStack()
                            }
                            ) {
                                Text(text = "apply")
                            }
                        }
                    }


                    composable("screen3")
                    {
//                        entry ->
//                        val text_default = entry.savedStateHandle.get<String>("my_text")?:"none"
//                        val age_default = entry.savedStateHandle.get<String>("age_text")?:"0"

                        Column (
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp))
                        {

                            var nullableHeight = height.toString()
                            if (nullableHeight == "0") nullableHeight = ""
                            var height_var by remember{
                                mutableStateOf(nullableHeight)
                            }

                            var nullableWeight = weight.toString()
                            if (nullableWeight == "0.0") nullableWeight = ""
                            var weight_var by remember {
                                mutableStateOf(nullableWeight)
                            }
                            val pattern = remember { Regex("^[a-zA-Z]+\$") }

                            Row (verticalAlignment = Alignment.CenterVertically)
                            {
                                OutlinedTextField(value = height_var, onValueChange = {if (it.isEmpty()) height_var = it else if (it.isDigitsOnly()) height_var = min(max(it.toInt(), 0), 299).toString()}, modifier = Modifier.width(300.dp), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), label = { Text("Height (cm)") })
                            }
                            Row (verticalAlignment = Alignment.CenterVertically)
                            {
                                OutlinedTextField(value = weight_var, onValueChange = {if (it.isEmpty()) weight_var = it else if (it.isDigitsOnly()) weight_var = min(max(it.toInt(), 0), 999).toString() else if (null != it.toFloatOrNull()) weight_var = min(max(it.toFloat(), 0f), 999f).toString()}, modifier = Modifier.width(300.dp), keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number), label = { Text("Weight (kg)") })
                            }

                            Button(onClick =
                            {
                                var handle = navController.previousBackStackEntry?.savedStateHandle
                                handle?.set("height", height_var.toIntOrNull()?:0)
                                handle?.set("weight", weight_var.toFloatOrNull()?:0f)
                                navController.popBackStack()
                            }
                            ) {
                                Text(text = "apply")
                            }
                        }
                    }


                    composable("screen4")
                    {
//                        entry ->
//                        val text_default = entry.savedStateHandle.get<String>("my_text")?:"none"
//                        val age_default = entry.savedStateHandle.get<String>("age_text")?:"0"

                        var height_var by remember{
                            mutableStateOf(height)
                        }

                        var weight_var by remember {
                            mutableStateOf(weight)
                        }

                        var bmi = 0
                        var bmi_str = "Cannot calculate BMI for given parameters"
                        if (weight_var != 0f && height_var != 0)
                        {
                            bmi = (weight_var / Math.pow((height_var.toDouble()/100), 2.0)).toInt()
                            bmi_str = bmi.toString()
                        }

                        Column (
                            Modifier
                                .fillMaxSize()
                                .padding(32.dp))
                        {

                            Button(onClick =
                            {
                                navController.popBackStack()
                            }
                            ) {
                                Text(text = "Done")
                            }

                            Column (modifier = Modifier.align(Alignment.CenterHorizontally))
                            {
                                Text(text = "Your BMI is: ")
                            }

                            Column (modifier = Modifier.align(Alignment.CenterHorizontally))
                            {
                                Text(text = bmi_str, fontSize = 50.sp, lineHeight = 60.sp, textAlign = TextAlign.Center)
                            }

                            Column (modifier = Modifier.align(Alignment.CenterHorizontally))
                            {
                                if (bmi == 0)
                                {

                                }
                                else if (bmi < 18.5)
                                {
                                    Text(text = "Underweight")
                                }
                                else if (bmi < 25)
                                {
                                    Text(text = "Normal weight")
                                }
                                else if (bmi < 30)
                                {
                                    Text(text = "Overweight")
                                }
                                else
                                {
                                    Text(text = "Obesity")
                                }
                            }

                        }
                    }



                }

            }
        }
    }
}






