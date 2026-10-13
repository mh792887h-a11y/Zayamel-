package com.example.util

import android.content.Context
import android.content.Intent
import com.example.data.model.Zamil

object ShareHelper {
    fun shareZamil(context: Context, zamil: Zamil) {
        val shareUrl = "https://zawamil-yemen.web.app/zamil/${zamil.id}"
        val shareText = """
            🎵 استمع إلى: ${zamil.title}
            🎤 للمنشد: ${zamil.performer}
            
            🔗 عبر تطبيق زوامل اليمن:
            $shareUrl
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, zamil.title)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "مشاركة الزامل عبر:")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }
}
