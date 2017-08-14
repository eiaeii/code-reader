package com.loopeer.codereaderkt.ui.activity

import android.annotation.TargetApi
import android.app.SearchManager
import android.content.Context
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.widget.SearchView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.loopeer.codereaderkt.Navigator
import com.loopeer.codereaderkt.R
import com.loopeer.codereaderkt.databinding.ActivitySimpleWebBinding
import com.loopeer.codereaderkt.ui.view.NestedScrollWebView


class SimpleWebActivity : BaseActivity(), SearchView.OnQueryTextListener {


    lateinit var binding: ActivitySimpleWebBinding

    internal var mWebContent: NestedScrollWebView? = null
    //    internal var mToolbar: Toolbar? = null
    internal var mProgressBar: ProgressBar? = null
    private var mSearchView: SearchView? = null

    private var mUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_simple_web)

        initWeb()
        parseIntent()
    }

    private fun initWeb() {
        mWebContent = findViewById(R.id.web_content) as NestedScrollWebView
        mWebContent!!.getSettings().setJavaScriptEnabled(true)
        mWebContent!!.getSettings().setDomStorageEnabled(true)
        mWebContent!!.getSettings().setGeolocationEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebContent!!.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW)
        }

        mWebContent!!.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                mSearchView!!.setQuery(url, true)
                return true
            }

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                mSearchView!!.setQuery(request.url.toString(), true)
                return true
            }
        })
        mWebContent!!.setWebChromeClient(WebChromeClient())
    }

    private fun parseIntent() {
        val intent = intent
        mUrl = intent.getStringExtra(Navigator.EXTRA_WEB_URL)
        val htmlString = intent.getStringExtra(Navigator.EXTRA_HTML_STRING)
        if (mUrl == null) mUrl = intent.dataString
        if (htmlString != null) loadData(htmlString)
    }

    private fun loadData(htmlString: String) {
        mWebContent!!.loadData(htmlString, "text/html", "utf-8")
    }

    private fun loadUrl(webUrl: String) {
        mWebContent!!.loadUrl(webUrl)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_web_input, menu)
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val inputView = menu.findItem(R.id.action_web_input)
        mSearchView = inputView.actionView as SearchView
        mSearchView!!.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        mSearchView!!.isIconified = false
        mSearchView!!.setOnQueryTextListener(this)
        mSearchView!!.imeOptions = EditorInfo.IME_ACTION_GO
        mSearchView!!.queryHint = getString(R.string.web_url_input_hint)
        mSearchView!!.maxWidth = Integer.MAX_VALUE
        if (mUrl != null && mSearchView != null) mSearchView!!.setQuery(mUrl, true)
        MenuItemCompat.setOnActionExpandListener(inputView, object : MenuItemCompat.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                mSearchView!!.post { mSearchView!!.setQuery(mUrl, false) }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                return true
            }
        })
        menuInflater.inflate(R.menu.menu_web_save, menu)
        menuInflater.inflate(R.menu.menu_web_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_save) {
            /*if (!TextUtils.isEmpty(mUrl) && !DownloadUrlParser.parseGithubUrlAndDownload(this@SimpleWebActivity, mUrl)) {
                showMessage(getString(R.string.repo_download_url_parse_error))
            }*/
            return true
        }
        if (id == R.id.menu_action_open_by_browser) {
//            Navigator.startOutWebActivity(this, mUrl)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    if (mWebContent!!.canGoBack()) {
                        mWebContent!!.goBack()
                    } else {
                        finish()
                    }
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        if (!TextUtils.isEmpty(query)) {
            mUrl = query
            loadUrl(mUrl!!)
            mSearchView!!.clearFocus()
        }
        return false
    }

    override fun onQueryTextChange(newText: String): Boolean {
        return false
    }

    inner class WebChromeClient : android.webkit.WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            if (newProgress == 100) {
                mProgressBar!!.visibility = View.GONE
            } else {
                if (mProgressBar!!.visibility == View.GONE)
                    mProgressBar!!.visibility = View.VISIBLE
                mProgressBar!!.progress = newProgress
            }
            super.onProgressChanged(view, newProgress)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mWebContent!!.destroy()
    }

    companion object {
        private val TAG = "SimpleWebActivity"
    }
}