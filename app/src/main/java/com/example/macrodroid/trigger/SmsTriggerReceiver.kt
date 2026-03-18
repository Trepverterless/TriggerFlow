package com.example.macrodroid.trigger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.macrodroid.domain.MacroService

class SmsTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
            val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
            
            messages.forEach { smsMessage ->
                val phoneNumber = smsMessage.displayOriginatingAddress ?: return@forEach
                val messageBody = smsMessage.messageBody ?: return@forEach
                
                // 确保服务正在运行
                MacroService.startService(context)
                
                // 处理短信触发
                MacroService.handleSmsTrigger(context, phoneNumber, messageBody)
            }
        }
    }
}