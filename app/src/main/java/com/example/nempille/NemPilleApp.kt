package com.example.nempille


import android.app.Application
import dagger.hilt.android.HiltAndroidApp

//tells Hilt to generate all the DI setup for the whole app,
//starting here
@HiltAndroidApp
class NemPilleApp : Application()