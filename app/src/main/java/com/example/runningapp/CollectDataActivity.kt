package com.example.runningapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.runningapp.utils.*
import kotlinx.android.synthetic.main.activity_data_collect.*

class CollectDataActivity : AppCompatActivity() {

    private val imagePickOptions = arrayOf("Take Photo", "Choose From Gallery", "Cancel")

    private val REQUEST_IMAGE_CAPTURE = 99
    private val REQUEST_IMAGE_PICK = 88

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_collect)
        setSupportActionBar(collect_data_toolbar)
        supportActionBar?.let {
            it.title = "Edit Profile"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        AppPermissions(this, this).checkCameraPermission()
        val userDataPreferences =
            getSharedPreferences(USER_DATA_PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)

        val bitmap =
            BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)
        if(bitmap != null){
            val scaleBitmap  = Bitmap.createScaledBitmap(bitmap,120,120,false)
            edit_profile_user_image.setImageBitmap(scaleBitmap)
        }else edit_profile_user_image.setImageBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_add_a_photo_themed_24dp))


        edit_profile_user_image.setOnClickListener { showImagePicker() }

        data_collect_first_name.setText(userDataPreferences.getString(PREFERENCE_FIRST_NAME, ""))
        data_collect_last_name.setText(userDataPreferences.getString(PREFERENCE_LAST_NAME, ""))
        data_collect_weight.setText(userDataPreferences.getInt(PREFERENCE_WEIGHT, 70).toString())
        data_collect_height.setText(userDataPreferences.getInt(PREFERENCE_HEIGHT, 165).toString())
        data_collect_gender.setSelection(userDataPreferences.getInt(PREFERENCE_GENDER, 0))

        data_collect_save.setOnClickListener {
            try {
                val firstName = data_collect_first_name.text.toString()
                val lastName = data_collect_last_name.text.toString()
                val weight = data_collect_weight.text.toString().toInt()
                val height = data_collect_height.text.toString().toInt()
                val gender = data_collect_gender.selectedItemPosition
                with(userDataPreferences.edit()) {
                    putString(PREFERENCE_FIRST_NAME, firstName)
                    putString(PREFERENCE_LAST_NAME, lastName)
                    putInt(PREFERENCE_WEIGHT, weight)
                    putInt(PREFERENCE_HEIGHT, height)
                    putInt(PREFERENCE_GENDER, gender)
                    commit()
                }
                finish()

            } catch (ex: ClassCastException) {
                Toast.makeText(baseContext, getString(R.string.validation_error), Toast.LENGTH_LONG)
                    .show()
            }

        }
    }

    private fun showImagePicker() {
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(imagePickOptions) { _, item ->
                when (imagePickOptions[item]) {
                    "Take Photo" -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.resolveActivity(packageManager)?.let {
                            val file = FileUtil.getOrCreateProfileImageFile(this, "image/jpeg")
                            val fileUri = FileProvider.getUriForFile(
                                this,
                                "com.example.runningapp.fileprovider",
                                file
                            )
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
                            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                        }
                    }
                    "Choose From Gallery" -> {
                        val intent = Intent(Intent.ACTION_GET_CONTENT)
                        intent.type = "image/*"
                        startActivityForResult(intent, REQUEST_IMAGE_PICK)
                    }
                }
            }
            .show()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let {
                        val file = FileUtil.getOrCreateProfileImageFile(
                            this,
                            contentResolver.getType(it) ?: "image/jpeg"
                        )
                        FileUtil.copyStreamToFile(contentResolver.openInputStream(it)!!, file)
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                        edit_profile_user_image.setImageBitmap(
                            Bitmap.createScaledBitmap(bitmap, 120, 120, false)
                        )
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                val bitmap =  BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)

                    edit_profile_user_image.setImageBitmap(
                        Bitmap.createScaledBitmap(bitmap, 120, 120, false)
                    )
                }
            }
        }
    }

    // This function is triggered when user chooses an option from permission dialogue
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {

                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

                    Log.i("Tag", "Permission has been denied by user")
                    finish()
                } else {
                    Log.i("Tag", "Permission has been granted by user")
                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
