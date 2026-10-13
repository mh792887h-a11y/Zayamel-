package com.example.util

object ArabicSearchHelper {

    // تطبيع الحروف العربية المشتركة وتجاهل التشكيل والهمزات
    fun normalizeArabic(text: String?): String {
        if (text.isNullOrBlank()) return ""

        return text
            // إزالة حركات التشكيل والتنوين
            .replace("[\u064B-\u065F\u0670]".toRegex(), "")
            // توحيد الهمزات والألف
            .replace("[أإآٱ]".toRegex(), "ا")
            // توحيد الياء والألف المقصورة
            .replace("ى", "ي")
            // توحيد التاء المربوطة والهاء
            .replace("ة", "ه")
            // إزالة التطويل (الكشيدة)
            .replace("ـ", "")
            .trim()
            .lowercase()
    }

    fun matchesQuery(source: String?, query: String): Boolean {
        if (source.isNullOrBlank()) return false
        val normalizedSource = normalizeArabic(source)
        val normalizedQuery = normalizeArabic(query)
        if (normalizedQuery.isBlank()) return true
        return normalizedSource.contains(normalizedQuery)
    }
}
