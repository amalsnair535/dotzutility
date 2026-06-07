package com.dotz.utility.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.dotz.utility.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.app.AlarmManager
import android.app.PendingIntent
import com.dotz.utility.data.clock.AlarmEntity
import java.util.Calendar

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val db = AppDatabase.get(context)
            val dao = db.alarmDao()
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            CoroutineScope(Dispatchers.IO).launch {
                val alarms = dao.getAllAlarms().first()
                alarms.filter { it.isEnabled }.forEach { alarm ->
                    scheduleAlarm(context, alarmManager, alarm)
                }
            }
        }
    }

    private fun scheduleAlarm(context: Context, alarmManager: AlarmManager, alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_ID", alarm.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}
