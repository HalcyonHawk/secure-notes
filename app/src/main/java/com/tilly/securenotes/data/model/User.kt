package com.tilly.securenotes.data.model

data class User(val dbDocId: String? = null,
                val email: String = "",
                val name: String = "",
                val avatar: String = ""
)