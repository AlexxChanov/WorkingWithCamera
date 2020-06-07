package com.example.workingwithcamera

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_PHOTO = 42
    private var photoUri: Uri? = null
    private var REQUEST_PERMISSION = 24

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(
                this!!,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
            &&Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PERMISSION
                )

        }
        takePhoto.setOnClickListener { dispatchTakePhotoIntent() }
    }

    private fun dispatchTakePhotoIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).let { takePhotoIntent ->
            takePhotoIntent.resolveActivity(packageManager)?.let {
                val photoFile = try {
                    createPublicImageFile()
                } catch (e: IOException) {
                    e.printStackTrace()
                    null
                }
                photoFile?.let {
                    photoUri = FileProvider.getUriForFile(
                        this,
                        "com.example.workingwithcamera.fileprovider",
                        it
                    )
                    takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(takePhotoIntent, REQUEST_PHOTO)
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            photo.setImageURI(photoUri)
            addToGalery()
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        Log.i("Path", storageDir!!.absolutePath)
        return File(storageDir, "${UUID.randomUUID()}.jpg").also {
            it.createNewFile()
        }
    }

    @Throws(IOException::class)
    private fun createPublicImageFile(): File {
        val storageDir = File("/storage/emulated/0/CoolPhotos").also {
            it.mkdir()
        }
        return File(storageDir, "${UUID.randomUUID()}.jpg").also {
            it.createNewFile()
        }
    }


private fun addToGalery() {
    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).let {
        it.data = photoUri
        sendBroadcast(it)
    }
}

}
