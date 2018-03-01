package com.ragabuza.drawningapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import processing.android.PFragment
import processing.core.PApplet
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {
    private var sketch: PApplet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        photoButton.setOnClickListener {
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, 12)
        }

    }

    override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(reqCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            try {
                val imageUri = data.data
                val imageStream = contentResolver.openInputStream(imageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)

                val bitmap = selectedImage.getResizedBitmap(container.width.toFloat(), container.height.toFloat())

                photoButton.visibility = View.GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                sketch = Sketch(
                        bitmap.width,
                        bitmap.height,
                        bitmap,
                        { image ->
                            this@MainActivity.runOnUiThread({
                                Originalcontainer.setImageBitmap(image)
                            })
                        }
                )
                val fragment = PFragment(sketch)
                fragment.setView(container, this)


            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (sketch != null) {
            sketch!!.onRequestPermissionsResult(
                    requestCode, permissions, grantResults)
        }
    }

    private fun Bitmap.getResizedBitmap(newWidth: Float, newHeight: Float): Bitmap {
        val width = this.width
        val height = this.height
        val scale = if (width > height) newWidth / width else newHeight / height
        val matrix = Matrix()
        matrix.postScale(scale, scale)
        val resizedBitmap = Bitmap.createBitmap(
                this, 0, 0, width, height, matrix, false)
        this.recycle()
        return resizedBitmap
    }


    public override fun onNewIntent(intent: Intent) {
        if (sketch != null) {
            sketch!!.onNewIntent(intent)
        }
    }

}