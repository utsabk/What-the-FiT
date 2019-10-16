package com.example.runningapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.runningapp.utils.FileUtil
import kotlinx.android.synthetic.main.activity_data_collect.*
import kotlinx.android.synthetic.main.fragment_profile.*
import android.graphics.Bitmap



class CollectDataActivity : AppCompatActivity() {

    private val imagePickOptions = arrayOf("Take Photo", "Choose From Gallery", "Cancel")

    private val REQUEST_IMAGE_CAPTURE = 99
    private val REQUEST_IMAGE_PICK = 88

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_collect)
        setSupportActionBar(collect_data_toolbar)
        supportActionBar?.let{
            it.title = "Edit Profile"
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)

        val bitmap = BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path)
        edit_profile_user_image.setImageBitmap(Bitmap.createScaledBitmap(bitmap,120, 120, false)?:
        BitmapFactory.decodeResource(resources, R.drawable.ic_add_a_photo_themed_24dp))

        edit_profile_user_image.setOnClickListener { showImagePicker() }

        data_collect_first_name.setText(sharedPreferences.getString(getString(R.string.preference_first_name), ""))
        data_collect_last_name.setText(sharedPreferences.getString(getString(R.string.preference_last_name), ""))
        data_collect_weight.setText(sharedPreferences.getInt(getString(R.string.preference_weight), 70).toString())
        data_collect_height.setText(sharedPreferences.getInt(getString(R.string.preference_height), 165).toString())
        data_collect_gender.setSelection(sharedPreferences.getInt(getString(R.string.preference_gender), 0))

        data_collect_save.setOnClickListener {
            try {
                val firstName = data_collect_first_name.text.toString()
                val lastName = data_collect_last_name.text.toString()
                val weight = data_collect_weight.text.toString().toInt()
                val height = data_collect_height.text.toString().toInt()
                val gender = data_collect_gender.selectedItemPosition
                with(sharedPreferences.edit()) {
                    putString(getString(R.string.preference_first_name), firstName)
                    putString(getString(R.string.preference_last_name), lastName)
                    putInt(getString(R.string.preference_weight), weight)
                    putInt(getString(R.string.preference_height), height)
                    putInt(getString(R.string.preference_gender), gender)
                    commit()
                }
                finish()

            } catch (ex: ClassCastException) {
                Toast.makeText(baseContext, getString(R.string.validation_error), Toast.LENGTH_LONG)
                    .show()
            }

        }
    }

    private fun showImagePicker(){
        AlertDialog.Builder(this)
            .setTitle("Add Photo")
            .setItems(imagePickOptions) {_, item ->
                when(imagePickOptions[item]) {
                    "Take Photo" -> {
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        intent.resolveActivity(packageManager)?.let {
                            val file = FileUtil.getOrCreateProfileImageFile(this, "image/jpeg")
                            val fileUri = FileProvider.getUriForFile(this, "com.example.runningapp.fileprovider", file)
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
            when(requestCode) {
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let {
                        val file = FileUtil.getOrCreateProfileImageFile(this, contentResolver.getType(it) ?: "image/jpeg")
                        FileUtil.copyStreamToFile(contentResolver.openInputStream(it)!!, file)
                        val bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(it))
                        edit_profile_user_image.setImageBitmap(Bitmap.createScaledBitmap(bitmap,120, 120, false))
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {profile_user_image.setImageBitmap(BitmapFactory.decodeFile(FileUtil.getOrCreateProfileImageFile(this, "image/jpeg").path))}
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}
