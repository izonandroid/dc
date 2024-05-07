package com.izontechnology.dcapp.data.common

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import com.izontechnology.dcapp.base.BaseApplication
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File


fun String?.toRequestBody(): RequestBody? {
    return this?.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun String.toRequestBody(key: String): RequestBody {
    val body = MultipartBody.Builder().setType(MultipartBody.FORM)
    return body.addFormDataPart(key, this).build()
}

fun File?.toRequestBodyFile(key: String): MultipartBody.Part? {
    if (this == null){
        return null
    }
    val filePart = MultipartBody.Part.createFormData(
        key,
        this.name,
        this.asRequestBody((getMimeType(this)?:"image/*").toMediaTypeOrNull())
    )

    return filePart
}

fun getMimeType(file: File?): String? {
    val uri: Uri = Uri.fromFile(file)
    val cR: ContentResolver = BaseApplication.INSTANCE.contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(uri))
}

fun Uri.getFileName(context: Context): String {
    val returnCursor = context.contentResolver.query(this, null, null, null, null)
    val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    returnCursor?.moveToFirst()
    val fileName = nameIndex?.let { returnCursor?.getString(it) }
    returnCursor?.close()
    return fileName?:""
}