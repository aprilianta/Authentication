package com.grd.authentication.Model

data class LoginModel (
    var value: String? = null,
    var message: String? = null,
    var result: List<ResultUserModel>? = null
)
