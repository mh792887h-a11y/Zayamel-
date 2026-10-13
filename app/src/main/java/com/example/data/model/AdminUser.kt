package com.example.data.model

data class AdminUser(
    val email: String,
    val displayName: String = "الإدارة",
    val photoUrl: String? = null,
    val token: String? = null,
    val role: String = "super_admin"
) {
    companion object {
        const val AUTHORIZED_ADMIN_EMAIL = "m91857687@gmail.com"
        
        fun isAuthorizedAdmin(email: String?): Boolean {
            return email?.trim()?.equals(AUTHORIZED_ADMIN_EMAIL, ignoreCase = true) == true
        }
    }
}
