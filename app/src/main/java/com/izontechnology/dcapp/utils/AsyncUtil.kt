package com.izontechnology.dcapp.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit,
    doInBackground: (coroutineScope: CoroutineScope) -> R,
    onPostExecute: (R) -> Unit
) = launch {
    onPreExecute() // runs in Main Thread
    val result = withContext(Dispatchers.IO) {
        doInBackground(this) // runs in background thread without blocking the Main Thread
    }
    onPostExecute(result) // runs in Main Thread
}
