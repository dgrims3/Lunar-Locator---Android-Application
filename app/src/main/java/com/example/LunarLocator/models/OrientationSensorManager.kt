package com.example.LunarLocator.models

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf // Import this
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2

class OrientationSensorManager(context: Context): SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Use MutableState for observable properties
    private val _azimuth = mutableFloatStateOf(0f)
    val azimuth: State<Float> = _azimuth

    private val _pitch = mutableFloatStateOf(0f)
    val pitch: State<Float> = _pitch

    fun start() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            // Calculate Azimuth from quaternion to account for pitch and roll
            val quaternion = FloatArray(4)
            SensorManager.getQuaternionFromVector(quaternion, event.values)

            val w = quaternion[0]
            val x = quaternion[1]
            val y = quaternion[2]
            val z = quaternion[3]

            val siny_cosp = 2 * (w * z + x * y)
            val cosy_cosp = 1 - 2 * (y * y + z * z)
            val azimuthRad = atan2(siny_cosp, cosy_cosp)
            val azimuthDeg = Math.toDegrees(azimuthRad.toDouble()).toFloat()

            _azimuth.value = ((azimuthDeg - 360f) % 360f) * -1

            // Calculate pitch
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            val zZ = rotationMatrix[8]

            val angleRad = acos(zZ.coerceIn(-1f, 1f).toDouble())  // Coerce to avoid NaN
            var tiltAngle = Math.toDegrees(angleRad).toFloat() - 90f

            _pitch.value = tiltAngle
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
}