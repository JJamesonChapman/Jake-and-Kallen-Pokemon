package edu.chapman.monsutauoka

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import edu.chapman.monsutauoka.databinding.ActivityMainBinding
import edu.chapman.monsutauoka.extensions.TAG
import android.widget.Toast
import edu.chapman.monsutauoka.services.sensors.MockStepSensorManager
import edu.chapman.monsutauoka.services.sensors.RealStepSensorManager
import edu.chapman.monsutauoka.services.data.SharedPreferencesDataStore
import edu.chapman.monsutauoka.services.StepCounterService
import edu.chapman.monsutauoka.services.sensors.StepSensorManager

class MainActivity : AppCompatActivity() {
    private lateinit var stepCounterService: StepCounterService
    private lateinit var stepSensorManager: StepSensorManager
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupSensors()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNav()
    }

    fun setupSensors() {
        // We know that we have permissions because the entry activity should have asserted that
        val sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor != null) {
            stepSensorManager = RealStepSensorManager(this, sensorManager, sensor)
        } else {
            Toast.makeText(this, "Using MOCK Step Counter", Toast.LENGTH_SHORT).show()
            stepSensorManager = MockStepSensorManager(this)
        }


        val sharedPreferences = getSharedPreferences(this::class.simpleName, MODE_PRIVATE)
        val dataStore = SharedPreferencesDataStore(sharedPreferences)
        stepCounterService = StepCounterService(dataStore)
    }

    fun setupNav() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment

        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_alpha,
                R.id.navigation_beta,
                R.id.navigation_gamma
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        stepSensorManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        stepSensorManager.onPause()

        val t = System.currentTimeMillis() + 5*60*1000 // 5 minutes
        ReminderScheduler.scheduleExactAt(
            context = this,
            triggerAtMillis = t,
            title = "Come back!",
            message = "Your legally distinct pocket entity misses you!",
        )
    }

    fun updateSteps(newStepCount: Float) {
        stepCounterService.updateSteps(newStepCount)
        Log.v(TAG, newStepCount.toString())
    }

    public fun consumeTreat() {
        stepCounterService.consumeTreat()
    }

    fun getStepCounterService(): StepCounterService {
        return stepCounterService
    }
}