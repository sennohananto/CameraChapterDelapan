package com.binar.camerachapterdelapan

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.media.MediaScannerConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.*

class MainActivity : AppCompatActivity() {

    lateinit var bitmapResult: Bitmap

    companion object{
        //REQUEST_CODE untuk meminta banyak permission yang terdiri dari CAMERA, WRITE EXT STORAGE, dan READ EXT STORAGE
        const val REQUEST_CODE = 201

        //Digunakan jika user memilih Ambil gambar dari Camera
        const val CAMERA_REQUEST = 1001

        //RequestDigunakan jika user memilih Pilih Gambar dari Gallery
        const val GALLERY_REQUEST = 1002

        //Daftar permission yang akan dipinta
        val arrayListPermission = arrayOf(
                //Agar aplikasi bisa membuka aplikasi Camera
                Manifest.permission.CAMERA,
                //Agar hasil dari tangkapan Aplikasi Camera bisa ditulis/disimpan ke Storage
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                //Agar bisa membaca hasil tangkapan camera maupun membaca gambar yang sudah ada di Gallery HP
                Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        btnTakePicture.setOnClickListener {
            if(checkPermissions()){
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Choose From ...")
                val option = arrayOf("Select Photo From Gallery", "Take a Picture From Camera")
                pictureDialog.setItems(option){dialog, which ->
                    when(which){
                        0 -> {
                            //Gallery
                            choosePhotoFromGallery()
                        }

                        1 -> {
                            //Camera
                            takePhotoFromCamera()
                        }
                    }
                }

                pictureDialog.show()
            }else{
                requestRequiredPermissions()
            }
        }


        btnSaveImage.setOnClickListener {
            saveImage(bitmapResult)
        }
    }

    //Mengecek semua permission yang dibutuhkan. Akan false jika setidaknya ada 1 permission yang belum mendapatkan izin.
    fun checkPermissions(): Boolean{
        return (
                    (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) &&
                    (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        )
    }

    //melakukan semua request permission yang dibutuhkan aplikasi
    fun requestRequiredPermissions(){
        requestPermissions(arrayListPermission, REQUEST_CODE)
    }

    //Callback / respon dari konfirmasi permission (pilihan allow / deny)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        //Cek request codenya sama seperti request code ketika meminta permission.
        when(requestCode){
            REQUEST_CODE -> {
                //Loop semua permission apakah diizinkan atau tidak.
                for(i in permissions.indices){
                    if ((permissions[i] == arrayListPermission[i]) && (grantResults[i] == PackageManager.PERMISSION_GRANTED)){
                        Toast.makeText(this, "Permission ${permissions[i]} Diizinkan", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this, "Permission ${permissions[i]} Ditolak", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    //Method yang menangani hasil / respon dari Implicit Intent ke Aplikasi Gallery maupun Aplikasi Camera
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Jika user memilih Gallery, menjalankan block ini.
        if(requestCode == GALLERY_REQUEST){
            //Cek data hasil responnya tidak null
            if(data!=null){
                val contentUri = data.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
                Toast.makeText(this, "image loaded", Toast.LENGTH_SHORT).show()
                ivImage.setImageBitmap(bitmap)
                bitmapResult = bitmap
                btnSaveImage.visibility = View.VISIBLE
            }else{
                Toast.makeText(this, "Image Loading Failed", Toast.LENGTH_LONG).show()
            }
        }else if(requestCode == CAMERA_REQUEST){
            val thumbnail = data?.extras?.get("data") as Bitmap
            ivImage.setImageBitmap(thumbnail)

            bitmapResult = thumbnail
            btnSaveImage.visibility = View.VISIBLE
        }
    }

    fun choosePhotoFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, GALLERY_REQUEST)
    }

    fun takePhotoFromCamera(){
        val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intentCamera, CAMERA_REQUEST)
    }

    fun saveImage(bitmap: Bitmap): String{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bytes)

        val directoryTarget = File((Environment.getExternalStorageDirectory()).toString() + "/PICTURES")
        Log.d("CH8",directoryTarget.toString())

        if(!directoryTarget.exists()){
            directoryTarget.mkdirs()
        }

        val file = File(directoryTarget, ((Calendar.getInstance().timeInMillis).toString()+".jpg"))

        file.createNewFile()

        val fileOutputStream = FileOutputStream(file)

        fileOutputStream.write(bytes.toByteArray())

        MediaScannerConnection.scanFile(this, arrayOf(file.path), arrayOf("images/jpg"), null)

        fileOutputStream.close()

        Log.d("CH8","File saved in ${file.absolutePath}")

        return file.absolutePath
    }
}