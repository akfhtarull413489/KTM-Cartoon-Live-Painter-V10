package com.ktm.cartoonlivepainter

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.LinearLayout
import java.io.ByteArrayOutputStream

class MainActivity : Activity() {

    private lateinit var webView: WebView
    private val pickImageCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL

        val pickButton = Button(this)
        pickButton.text = "ছবি নির্বাচন করুন"

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.webViewClient = WebViewClient()

        pickButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "image/*"
            startActivityForResult(intent, pickImageCode)
        }

        layout.addView(pickButton)

        layout.addView(
            webView,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(layout)
        webView.loadUrl("file:///android_asset/index.html")
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == pickImageCode && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            try {
                val input = contentResolver.openInputStream(uri) ?: return
                val bitmap = input.use {
                    BitmapFactory.decodeStream(it)
                } ?: return

                val scale = minOf(
                    1200f / bitmap.width,
                    1200f / bitmap.height,
                    1f
                )

                val width = (bitmap.width * scale).toInt()
                val height = (bitmap.height * scale).toInt()

                val resized = Bitmap.createScaledBitmap(
                    bitmap, width, height, true
                )

                val output = ByteArrayOutputStream()
                resized.compress(
                    Bitmap.CompressFormat.JPEG, 85, output
                )

                val encoded = Base64.encodeToString(
                    output.toByteArray(),
                    Base64.NO_WRAP
                )

                webView.evaluateJavascript(
                    "window.showPickedImage('data:image/jpeg;base64,$encoded')",
                    null
                )

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

