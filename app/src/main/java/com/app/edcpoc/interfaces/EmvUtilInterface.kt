package com.app.edcpoc.interfaces

import android.content.Context

interface EmvUtilInterface {
    fun onDoSomething(context: Context)
    fun onError(message: String)
}