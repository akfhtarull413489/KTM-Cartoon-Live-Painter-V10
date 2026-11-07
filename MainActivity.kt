package com.ktm.cartoonlivepainter

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var uploadMessage: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER_RESULT_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // লেআউট ফাইলের ঝামেলা এড়াতে সরাসরি কোড থেকে WebView সেট করা হলো
        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true

        webView.webViewClient = WebViewClient()

        // ফাইল পিকার ও ফাইল সিলেক্ট করার জন্য WebChromeClient
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                if (uploadMessage != null) {
                    uploadMessage!!.onReceiveValue(null)
                    uploadMessage = null
                }
                uploadMessage = filePathCallback

                try {
                    val intent = fileChooserParams?.createIntent()
                    if (intent != null) {
                        startActivityForResult(intent, FILE_CHOOSER_RESULT_CODE)
                    }
                } catch (e: Exception) {
                    uploadMessage = null
                    return false
                }
                return true
            }
        }

        // assets ফোল্ডারে থাকা আপনার index.html ফাইলটি লোড করা হচ্ছে
        webView.loadUrl("file:///android_asset/index.html")
    }

    // ফাইল সিলেক্ট করার পর ফলাফল রিসিভ করার জন্য
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (uploadMessage == null) return
            val result = WebChromeClient.FileChooserParams.parseResult(resultCode, data)
            uploadMessage!!.onReceiveValue(result)
            uploadMessage = null
        }
    }

    // ব্যাক বাটন প্রেস করলে পেছনের পেজে যাওয়ার জন্য
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
