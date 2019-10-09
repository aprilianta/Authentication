package com.grd.authentication.Model

data class ResultUserModel(
    var username: String,
    var nama: String? = null,
    var password: String? = null
)