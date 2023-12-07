package com.ez.kotlin.frame.base

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.ez.kotlin.frame.R
import com.ez.kotlin.frame.net.ApiException
import com.ez.kotlin.frame.net.NetDialog
import com.ez.kotlin.frame.net.ResponseException
import com.ez.kotlin.frame.utils.DayNightMode
import com.ez.kotlin.frame.utils.NetWorkUtil
import com.ez.kotlin.frame.utils.ToastUtil
import com.gyf.immersionbar.ktx.immersionBar
import com.kunminx.architecture.ui.page.DataBindingFragment

abstract class BaseFragment<VM : BaseViewModel> : DataBindingFragment() {
    /**
     * ViewModel 实例
     */
    protected lateinit var viewModel: VM

    /**
     * Loading
     * */
    private val mNetDialog by lazy { NetDialog(activity as AppCompatActivity) }

    /**
     * 跳过监听
     */
    var isObserveViewModel = false


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        immersionBar {
            fitsSystemWindows(true)
            transparentNavigationBar()
            statusBarColor(BaseApplication.instance.statusBarColorId)
        }
        startObserve()
        initBindView(view)
        initData()
    }

    /**
     *  初始化ViewModel
     *
     */
    override fun initViewModel() {
        getBindingVMClass()?.let {
            viewModel = ViewModelProvider(this)[it]
        }
    }

    /**
     *  添加ViewModel
     *
     */
    open fun <T : BaseViewModel> getViewModel(clazz: Class<T>): T {
        return ViewModelProvider(this)[clazz]
    }

    override fun onDestroyView() {
        viewModel.run {
            start().removeObservers(this@BaseFragment.viewLifecycleOwner)
            success().removeObservers(this@BaseFragment.viewLifecycleOwner)
            error().removeObservers(this@BaseFragment.viewLifecycleOwner)
            finally().removeObservers(this@BaseFragment.viewLifecycleOwner)
        }
        super.onDestroyView()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (BaseApplication.instance.dayNightMode == DayNightMode.SYSTEM) {
            when (newConfig.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    //关闭夜间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    //打开夜间模式
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }

                else -> {}
            }
        }
    }

    /**
     *  DialogLoading 显示
     *
     */
    open fun stateDialogLoading() {
        if (!mNetDialog.isShowing) {
            mNetDialog.show()
        }
    }

    open fun stateDialogDismiss() {
        if (mNetDialog.isShowing) {
            mNetDialog.dismiss()
        }
    }

    open fun stateDialogLoading(isCancel: Boolean, loading: String?) {
        if (!mNetDialog.isShowing) {
            mNetDialog.setCancelable(isCancel)
            mNetDialog.showLoadingText(loading)
            mNetDialog.show()
        }
    }

    /**
     *  viewModel实例
     *  */
    abstract fun getBindingVMClass(): Class<VM>?

    /**
     * 初始化 View
     */
    abstract fun initBindView(view: View)

    /**
     * 初始化数据
     */
    abstract fun initData()

    /**
     * 点击事件
     */
    abstract fun onClick(v: View)

    /**
     * TODO 请求监听
     *
     */
    private fun startObserve() {
        if (isObserveViewModel) viewModel.run {
            start().observe(this@BaseFragment.viewLifecycleOwner) {
                //开始
                onRequestStart(it.first, it.second)
            }
            success().observe(this@BaseFragment.viewLifecycleOwner) {
                //成功
                onRequestSuccess(it.first, it.second)
            }
            error().observe(this@BaseFragment.viewLifecycleOwner) {
                //报错
                onRequestError(it.first, it.second)
            }
            finally().observe(this@BaseFragment.viewLifecycleOwner) {
                //结束
                onRequestFinally(it.first, it.second)
            }
        }
    }

    /**
     *  接口请求开始，子类可以重写此方法做一些操作
     *  */
    open fun onRequestStart(requestCode: String, it: Boolean) {
        stateDialogLoading()
    }

    /**
     *  接口请求成功，子类可以重写此方法做一些操作
     *  */
    open fun onRequestSuccess(requestCode: String, it: Boolean) {
    }

    /**
     * 接口请求完毕，子类可以重写此方法做一些操作
     * */
    open fun onRequestFinally(requestCode: String, it: Int?) {
        stateDialogDismiss()
    }

    /**
     *  接口请求出错，子类可以重写此方法做一些操作
     *  */
    open fun onRequestError(requestCode: String, it: Exception?) {
        //处理一些已知异常
        showErrorTip(requestCode, it)
    }

    /**
     * 处理一些已知异常
     */
    open fun showErrorTip(requestCode: String, it: Exception?) {
        //处理一些已知异常
        it?.run {
            if (NetWorkUtil.isNoProxyConnected(requireContext())) {
                when (it) {
                    //服务器特殊错误处理
                    is ApiException -> {
                        onServiceError(requestCode, it.code, it.message)
                    }
                    //正常错误显示
                    is ResponseException -> {
                        ToastUtil().longShow("(${it.code})${it.message}")
                    }
                    //无提示信息错误显示
                    else -> {
                        ToastUtil().longShow(R.string.unknown_error)
                    }
                }
            } else {
                //无网络提示
                ToastUtil().longShow(R.string.network_error_content)
            }
        }
    }

    /**
     * 服务器特殊错误处理
     * ‘登录超时’等
     * */
    open fun onServiceError(requestCode: String, code: Int, message: String?) {

    }
}