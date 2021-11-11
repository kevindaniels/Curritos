package com.kevin.curritos.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val signedRequest = sign(chain.request())
        return chain.proceed(signedRequest)
    }

    private fun sign(request: Request): Request {
        val builder = request.newBuilder()
        val token = "Bearer Kc-k-sjlcmP2MeOu-F2PvExqUXc3681zXd7D9L4SVSol1O3Z0dNH3W5RgO6rG80ulQCMXfqcHOu" +
                "GXsN2fq-R3Fgnw8Bn_RmPau3FkGND3mDqoPVFKoR3qEUTpOV6YXYx"

        builder.header("Authorization", token)

        return builder.build()
    }
}