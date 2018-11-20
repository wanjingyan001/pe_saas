package com.sogukj.pe.ddshare

import java.io.Serializable


data class AccountToken(
		var access_token: String,// 070c171a26d633d1b631dxxxxxxxx
		var errcode: Int,// 0
		var errmsg: String// ok
)



data class AuthorizeCode(
		var errcode: Int,// 0
		var errmsg: String,// ok
		var openid: String,// liSii8KCxxxxx
		var persistent_code: String,// dsa-d-asdasdadHIBIinoninINIn-ssdasd
		var unionid: String// 7Huu46kk
):Serializable


data class AuthorizeReq(
		var tmp_auth_code:String
)