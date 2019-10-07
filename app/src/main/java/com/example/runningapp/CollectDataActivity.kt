package com.example.runningapp

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_data_collect.*

class CollectDataActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data_collect)

        val sharedPreferences =
            getSharedPreferences(getString(R.string.preference_key), Context.MODE_PRIVATE)

        data_collect_first_name.setText(
            sharedPreferences.getString(
                getString(R.string.preference_first_name),
                ""
            )
        )
        data_collect_last_name.setText(
            sharedPreferences.getString(
                getString(R.string.preference_last_name),
                ""
            )
        )
        data_collect_weight.setText(
            sharedPreferences.getInt(
                getString(R.string.preference_weight),
                70
            ).toString()
        )
        data_collect_height.setText(
            sharedPreferences.getInt(
                getString(R.string.preference_height),
                165
            ).toString()
        )
        data_collect_gender.setSelection(
            sharedPreferences.getInt(
                getString(R.string.preference_gender),
                0
            )
        )

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
}
