package com.tilly.securenotes.data.model

data class User (val dbUserId: String? = null,
                val email: String = "",
                val name: String = "",
                val avatar: String = ""
)