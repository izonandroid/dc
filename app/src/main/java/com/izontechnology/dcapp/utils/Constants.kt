package com.izontechnology.dcapp.utils

import android.os.Environment
import com.izontechnology.dcapp.data.common.SaveWifi

// Constants for RIDE MODE
const val CITY = "CITY"
const val SPORT = "SPORT"
const val SPORTS = "SPORTS"
const val ECO = "ECO"

// Constants for MODE
const val C = "C"
const val S = "S"
const val E = "E"

const val BASE_URL = "http://192.168.2.34:9000/api/"

const val FAILED = "Failed"
const val SUCCESS = "Success"

const val GPS_RESOLUTION_CODE = 1001
//const val izonPackageName = "com.izon360.fleetgolf"
const val izonPackageName = "com.izontechnology.golfapp"

const val ACTION_INSTALL_COMPLETE = "INSTALL_COMPLETE"
const val ACTION_UNINSTALL_COMPLETE = "UNINSTALL_COMPLETE"
const val PACKAGE_NAME = "PACKAGE_NAME"
const val LAUNCHER_RESTARTER_PACKAGE_ID = "PACKAGE_NAME"
const val WORK_TAG = "nightupdate"

// Constants for taco-meter
const val TACO_START_POSITION = 0f
const val TACO_END_POSITION = 12000f

const val FRAGMENT_DIM_AMT = 0.0f
const val ZERO = 0
const val MINUS_ONE = -1
const val MINUS_FIFTY = -50
const val MINUS_SIXTY = -60
const val MINUS_SEVENTY = -70
const val ONE = 1
const val TWO = 2
const val THREE = 3
const val FOUR = 4
const val FIVE = 5
const val SIX = 6
const val EIGHT = 8
const val NINE = 9
const val SIXTEEN = 16
const val TWENTY_FOUR = 24
const val THIRTY_ONE = 31
const val FIFTY = 50
const val SIXTY = 60
const val TEN = 10
const val TWENTY = 20
const val AUDIO_VALUE = 69
const val HUNDRED = 100
const val ONETWENTY = 120
const val ONEEIGHTY = 180
const val TWOHUNDRED = 200
const val TWOFORTY = 240
const val TWOHUNDRED_FIFTY_FIVE = 255
const val THREEHUNDRED = 300
const val FOURHUNDRED = 400
const val FIVEHUNDRED = 500
const val SIXHUNDRED = 600
const val SEVENHUNDRED = 700
const val EIGHTHUNDRED = 800
const val EIGHTFIFTY = 850
const val NINEHUNDRED = 900
const val THOUSAND = 1000
const val SIXTY_THOUSAND = 60000
const val THARTY_THOUSAND = 30000
const val FIVETHOUSAND = 5000
const val TENTHOUSAND = 10000
const val TWOTHOUSAND = 2000
const val GUIDE_DELAY = 5000
const val PROGRESS_TIME = 4500L
const val SOUND_X = 1033
const val SOUND_X1 = 1033
const val THREETHOUSAND = 3000L

const val BRIGHTNESS_X1 = 660
const val DISPLAY_X1 = 1020

const val DISPLAY_MULTIPLICATION = 10.20
const val BRIGHTNESS_MULTIPLICATION = 6.6

const val BRIGHTNESS_CONST = 2.55
const val SYS_BRIGHTNESS = 255

const val SYSTEM_EQUAL_BRIGHTNESS = 2.59
const val SYSTEM_EQUAL_DISPLAY = 4

const val DISMISS_DELAY = 10000L
const val INTERVAL_DELAY = 1000L
const val CONTROL_DELAY = 100L
const val RIDE_MODE_TRANSITION_DELAY = 250L
const val RIDE_MODE_TRANSITION_DELAY2 = 100L
const val DATE_ANIMATION_DELAY = 250L

const val BASIC_IP = "0.0.0.0"
const val N = "N"
const val DECIMAL_VALUE = "00"
const val START_TIMER = "00:00"
const val TYPE = "type"
const val HEADSET = "headset"
const val MOBILE = "mobile"
const val HELMET = "helmet"
const val CURRENT_RIDE_VALUE = "currentRideValue"
const val BRIGHTNESS_VALUE = "brightnessValue"
const val SOUND_VALUE = "soundValue"
const val WPA = "WPA"
const val WEP = "WEP"
const val WPA2 = "WPA2"
const val WPA_EAP = "WPA_EAP"
const val IEEE8021X = "IEEE8021X"
const val UNKNOWN_SSID = "unknown ssid"
const val ESS = "ESS"
const val NONE = "None"
const val START_BRACKET = "["
const val END_BRACKET = "]"
const val ASTRIC_UNICODE = '\u2731'

const val LIGHT_THEME = 0
const val DARK_THEME = 1

var DOWNLOAD_DIR_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
var TILE_JSON_PATH = DOWNLOAD_DIR_PATH+"/tilesversion.json"
var FIRMWARE_JSON_PATH = DOWNLOAD_DIR_PATH+"/firware.json"
var FACILITY_JSON_PATH = DOWNLOAD_DIR_PATH+"/facility.json"

val savedWifiList = arrayListOf<SaveWifi>(
    SaveWifi("IZONTALK","12345678"),
    SaveWifi("IZONGCUG","12345678"),
    SaveWifi("DEVELOPER","PLLC@2020"),
    SaveWifi("SPYHRE-EAST","Spyhre@2021"),
    SaveWifi("Redmi Note 9 Pro Max","vish10043"),
//    SaveWifi("SPYHRE-EAST","Spyhre@2021"),
)