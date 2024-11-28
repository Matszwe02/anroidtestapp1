package com.example.myapplication

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.w3c.dom.Text


import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs


class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var brightness: Sensor? = null
    private var accel: Sensor? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient


//    var textObj: Unit

    val permissions = arrayOf(
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    var light by mutableStateOf(0f)
    var accelerationX by mutableStateOf(0f)
    var accelerationY by mutableStateOf(0f)
    var accelerationZ by mutableStateOf(0f)

    var latitude by mutableStateOf(0.0)
    var longitude by mutableStateOf(0.0)

    var ballX by mutableStateOf(0.0)
    var ballY by mutableStateOf(0.0)
    var ballSpeedX by mutableStateOf(0.0)
    var ballSpeedY by mutableStateOf(0.0)
    val BALL_RADIUS = 100.0
//    val BOX_SIZE = 600f

    companion object {
        var BOX_SIZE_X = 0.0
        var BOX_SIZE_Y = 0.0
        const val BALL_RADIUS = 50.0
    }

    private var gameLoopRunning = false
    private val gameLoopLock = Any()
    private val frameTime = 1000L / 120L // 120 FPS

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var vibrator: Vibrator


    private fun setUpSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, 1)
        }
        brightness = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                latitude = it.result.latitude
                longitude = it.result.longitude
            }
        }


    }


    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            light = event.values[0]
        }
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelerationX = event.values[0]
            accelerationY = event.values[1]
            accelerationZ = event.values[2]
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }


    // This is onResume function of our app
    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, brightness, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // This is onPause function of our app
    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }


    private fun startGameLoop() {
        synchronized(gameLoopLock) {
            if (!gameLoopRunning) {
                gameLoopRunning = true
                Thread { gameLoop() }.start()
            }
        }
    }

    private fun stopGameLoop() {
        synchronized(gameLoopLock) {
            gameLoopRunning = false
        }
    }

    private fun gameLoop() {
        var x = true
        while (x) {
            synchronized(gameLoopLock) {
                if (!gameLoopRunning) {
                    x = false
                }
            }

            updatePhysics()
            updateUI()

            try {
                Thread.sleep(frameTime)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    private fun playBounceSound(speed: Double) {
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.prepare()
        }
        var vol = speed.coerceIn(10.0, 50.0).toFloat()/50f
        mediaPlayer.setVolume(vol, vol)
        mediaPlayer.start()

            @Suppress("DEPRECATION")
            vibrator.vibrate(speed.coerceIn(5.0, 100.0).toLong())
    }

    private fun updatePhysics() {
        ballSpeedX -= accelerationX * 0.2
        ballSpeedY += accelerationY * 0.2


        if (abs(accelerationX) + abs(accelerationY) < 0.1 && abs(ballSpeedX) + abs(ballSpeedY) < 0.1f)
        {
            ballSpeedX = 0.0
            ballSpeedY = 0.0
        }

        ballSpeedX *= 0.999
        ballSpeedY *= 0.999

        ballX += ballSpeedX
        ballY += ballSpeedY

        // Collision detection
        if (ballX - BALL_RADIUS < 0 || ballX + BALL_RADIUS > BOX_SIZE_X) {
            ballSpeedX *= -0.8
            if (abs(ballSpeedX) > 2.0)
                playBounceSound(abs(ballSpeedX))
            if (ballX - BALL_RADIUS < 0) ballX = BALL_RADIUS else ballX = BOX_SIZE_X - BALL_RADIUS
        }
        if (ballY - BALL_RADIUS < 0 || ballY + BALL_RADIUS > BOX_SIZE_Y) {
            ballSpeedY *= -0.8
            if (abs(ballSpeedY) > 2.0)
                playBounceSound(abs(ballSpeedY))
            if (ballY - BALL_RADIUS < 0) ballY = BALL_RADIUS else ballY = BOX_SIZE_Y - BALL_RADIUS
        }

        // Limit maximum speed
        ballSpeedX = ballSpeedX.coerceIn(-100.0, 100.0)
        ballSpeedY = ballSpeedY.coerceIn(-100.0, 100.0)
    }

    private fun updateUI() {
        runOnUiThread {
            // Update UI here if needed
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGameLoop()
        mediaPlayer.release()
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        var splash = true
        super.onCreate(savedInstanceState)
        setUpSensor()
        mediaPlayer = MediaPlayer.create(this, R.raw.bounce4)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        installSplashScreen().setKeepOnScreenCondition{splash}

        lifecycleScope.launch {
            delay(1000)
            splash=false
        }

        startGameLoop()

        setContent {
            MyApplicationTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    Column {

                        Text(text = "Light: " + light.toString())

                        Text(text = "X: " + accelerationX.toString())
                        Text(text = "Y: " + accelerationY.toString())
                        Text(text = "Z: " + accelerationZ.toString())

                        Text(text = "Lat: " + latitude.toString())
                        Text(text = "Lon: " + longitude.toString())
                    }

                    BoxWithBall()
                }
            }
        }
    }

    @Composable
    fun BoxWithBall() {
        val context = LocalContext.current
        val screenWidth =
            remember(context) { context.resources.displayMetrics.widthPixels.toFloat() }
        val screenHeight =
            remember(context) { context.resources.displayMetrics.heightPixels.toFloat() }

        Canvas(
            modifier = Modifier.fillMaxSize(),
            onDraw = {
                drawRect(Color.Black, alpha = 0.5f)
                drawCircle(
                    Color.hsv(0f, 1f - light.coerceIn(0f, 1000f)/1000f, 1f),
                    radius = BALL_RADIUS.toFloat(),
                    center = Offset(ballX.toFloat(), ballY.toFloat())
                )
            }
        )

        LaunchedEffect(Unit) {
            BOX_SIZE_X = screenWidth.toDouble()
            BOX_SIZE_Y = screenHeight.toDouble()
        }
    }
}

