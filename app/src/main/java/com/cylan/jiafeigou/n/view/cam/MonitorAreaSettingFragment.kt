package com.cylan.jiafeigou.n.view.cam

import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import butterknife.OnClick
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.module.GlideApp
import com.cylan.jiafeigou.n.mvp.contract.cam.MonitorAreaSettingContact
import com.cylan.jiafeigou.support.log.AppLogger
import com.cylan.jiafeigou.utils.PreferencesUtils
import com.cylan.jiafeigou.utils.ToastUtil
import com.cylan.jiafeigou.utils.ViewUtils
import com.cylan.jiafeigou.widget.crop.Shaper
import kotlinx.android.synthetic.main.fragment_monitor_area_setting.*

/**
 * Created by yanzhendong on 2017/11/15.
 */
class MonitorAreaSettingFragment : BaseFragment<MonitorAreaSettingContact.Presenter>(), MonitorAreaSettingContact.View {
    private var monitorWidth: Int = 0
    private var monitorHeight: Int = 0
    private var monitorAreaArray = mutableListOf<DpMsgDefine.Rect4F>()
    private var monitorPictureReady: Boolean = false
    private var restoreMonitorLayout: Boolean = true
    private var monitorAreaMarginRadio = 0.0f//听说不能全部图片区域选择?,先写好先
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_monitor_area_setting, container, false)
    }

    override fun initViewAndListener() {
        super.initViewAndListener()
        effect_container.keepScreenOn = true
        ViewUtils.setRequestedOrientation(activity as Activity, ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE)
        effect_container.setSizeUpdateListener(this::onMonitorAreaChanged)
        effect_container.setOnSystemUiVisibilityChangeListener { ViewUtils.setSystemUiVisibility(monitor_picture, false) }
        monitorWidth = context!!.resources.getDimensionPixelSize(R.dimen.y206)
        monitorHeight = context!!.resources.getDimensionPixelSize(R.dimen.y136)
        presenter.loadMonitorAreaSetting()
    }

    override fun onStart() {
        super.onStart()
        ViewUtils.setSystemUiVisibility(monitor_picture, false)
    }

    private fun onMonitorAreaChanged(shaper: Shaper?, width: Int, height: Int) {
        AppLogger.w("onMonitorAreaChanged:width:$width,height:$height")
        val hintView = shaper?.shaper?.findViewById<View>(R.id.effect_hint)
        if (hintView != null) {
            //长和宽 任何一个小于默认尺寸,则隐藏提示文字
//            hintView.visibility = if (width >= monitorWidth && height >= monitorHeight) View.VISIBLE else View.GONE
        }
    }

    override fun onGetMonitorPictureSuccess(url: String) {
        AppLogger.w("onGetMonitorPictureSuccess:$url")
        tryGetMonitorPicture(url)
    }


    override fun onGetMonitorPictureError() {
        AppLogger.w("onGetMonitorPictureError")
        hideLoadingBar()
        alertErrorGetMonitorPicture()
    }

    override fun tryGetLocalMonitorPicture() {
        val localUrl: String? = PreferencesUtils.getString(JConstant.MONITOR_AREA_PICTURE + ":$uuid")
        localUrl?.apply {
            GlideApp.with(this@MonitorAreaSettingFragment)
                    .asBitmap()
                    .load(this)
                    .onlyRetrieveFromCache(true)
                    .skipMemoryCache(true)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                            resource?.apply {
                                updateMonitorAreaPicture(this)
                                if (monitorAreaArray.size > 0) {
                                    //说明区域数据已经先回来了
                                    toggleMonitorAreaMode(false)
                                }
                            }
                        }
                    })
        }
    }


    private fun tryGetMonitorPicture(url: String) {
        GlideApp.with(this)
                .asBitmap()
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap?, transition: Transition<in Bitmap>?) {
                        if (resource != null) {
                            AppLogger.w("设置区域设置图片")
                            PreferencesUtils.putString(JConstant.MONITOR_AREA_PICTURE + ":$uuid", url)
                            hideLoadingBar()
                            updateMonitorAreaPicture(resource)
                            toggleMonitorAreaMode(restoreMonitorLayout)
                        }
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        hideLoadingBar()
                        alertErrorGetMonitorPicture()
                    }
                })
    }

    private fun alertErrorGetMonitorPicture() {
        finish.isEnabled = monitorPictureReady
        if (monitorPictureReady) {
            ToastUtil.showToast(getString(R.string.DETECTION_AREA_FAILED_LOAD))
        } else {
            AlertDialog.Builder(context!!)
                    .setMessage(R.string.DETECTION_AREA_FAILED_LOAD_RETRY)
                    .setCancelable(false)
                    .setPositiveButton(R.string.WELL_OK, { _, _ -> exitToParent() })
                    .create()
                    .show()
        }
    }

    fun updateMonitorAreaPicture(drawable: Bitmap) {
        monitorPictureReady = true
        monitor_picture.setImageBitmap(drawable)
        val params = monitor_picture.layoutParams
        val pictureRadio: Float = drawable.height.toFloat() / drawable.width.toFloat()
        val metrics = Resources.getSystem().displayMetrics
        val screenRadio: Float = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()
        val monitorPictureWidth: Int
        val monitorPictureHeight: Int
        val heightMargin: Int
        val widthMargin: Int
        if (pictureRadio >= screenRadio) {
            //图片的高宽比 大于 屏幕的 高宽比 则此时需要高度铺满屏幕高度,宽度计算出来
            monitorPictureHeight = metrics.heightPixels
            monitorPictureWidth = (metrics.heightPixels.toFloat() / pictureRadio).toInt()

        } else {
            //图片的高宽比小于屏幕的高宽比 则此时需要宽度铺满屏幕宽度,高度计算出来
            monitorPictureWidth = metrics.widthPixels
            monitorPictureHeight = (metrics.heightPixels.toFloat() * pictureRadio).toInt()
        }
        params.width = monitorPictureWidth
        params.height = monitorPictureHeight
        heightMargin = (monitorPictureHeight * monitorAreaMarginRadio).toInt()
        widthMargin = (monitorPictureWidth * monitorAreaMarginRadio).toInt()
        monitor_picture.post { monitor_picture.layoutParams = params }
        effect_container.post {
            val layoutParams = effect_container.layoutParams as RelativeLayout.LayoutParams
            layoutParams.setMargins(widthMargin / 2, heightMargin / 2, widthMargin / 2, heightMargin / 2)
            effect_container.layoutParams = layoutParams
        }
        finish.isEnabled = true
    }

    private fun toggleMonitorAreaMode(restore: Boolean) {
        this.restoreMonitorLayout = restore
        if (!monitorPictureReady) {
            //图片还没准备好 不允许进行模式切换
            return
        }
        if (restoreMonitorLayout) {
            monitor_toggle.visibility = View.GONE
            drag_and_drop.visibility = View.VISIBLE
            pop_tips.visibility = View.GONE
            effect_container.removeAllViews()
        } else {
            effect_container.removeAllViews()
            val effectView = LayoutInflater.from(context).inflate(R.layout.layout_motion_shaper, effect_container, false)
            restoreMonitorAreaIfNeeded(effectView)
            monitor_toggle.visibility = View.VISIBLE
            drag_and_drop.visibility = View.GONE
            decideShowPopTips()
        }
    }

    private fun restoreMonitorAreaIfNeeded(effectView: View) {
        if (monitorAreaArray.size > 0) {
            val rect4F = monitorAreaArray[0]
            val width = effect_container.width.toFloat()
            val height = effect_container.height.toFloat()
            val layoutParams = effectView.layoutParams as FrameLayout.LayoutParams
            layoutParams.gravity = Gravity.NO_GRAVITY
            layoutParams.setMargins((width * rect4F.left).toInt(), (height * rect4F.top).toInt(), 0, 0)
            layoutParams.width = Math.max((width * (rect4F.right - rect4F.left)).toInt(), effectView.minimumWidth)
            layoutParams.height = Math.max((height * (rect4F.bottom - rect4F.top)).toInt(), effectView.minimumHeight)
            AppLogger.w("区域侦测:恢复区域为:$rect4F,width:${layoutParams.width},height:${layoutParams.height},container width:$width,container height:$height")
            if (layoutParams.width < monitorWidth || layoutParams.height < monitorHeight) {
//                effectView.findViewById(R.id.effect_hint).visibility = View.GONE
            }
            effectView.layoutParams = layoutParams
        }
        effect_container.addView(effectView)
    }

    private fun decideShowPopTips() {
        var showPopTips = PreferencesUtils.getBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, true)
        if (showPopTips) {
            PreferencesUtils.putBoolean(JConstant.SHOW_MONITOR_AREA_TIPS, false)
            pop_tips.visibility = View.VISIBLE
        }
    }

    @OnClick(R.id.pop_tips)
    fun clickedPopTips() {
        AppLogger.w("点击了 pop tips")
        pop_tips.visibility = View.GONE
    }

    override fun performBackIntercept(willExit: Boolean): Boolean {
        ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        return super.performBackIntercept(willExit)
    }

    @OnClick(R.id.back)
    fun exitToParent() {
        AppLogger.w("点击了返回按钮")
        back.post { ViewUtils.setRequestedOrientation(activity, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) }
        back.post { fragmentManager?.popBackStack() }
    }

    @OnClick(R.id.monitor_toggle)
    fun clickedToggleMonitor() {
        AppLogger.w("点击了切换")
        toggleMonitorAreaMode(true)
    }

    @OnClick(R.id.drag_and_drop)
    fun clickedDropAndDrag() {
        AppLogger.w("点击了进入选择侦测区域按钮")
        toggleMonitorAreaMode(false)
    }

    @OnClick(R.id.finish)
    fun clickedFinish() {
        AppLogger.w("点击了完成按钮")
        val rects = effect_container.motionArea.map { DpMsgDefine.Rect4F(it[0], it[1], it[2], it[3]) }
        AppLogger.w("区域侦测:选择区域为:$rects")
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        presenter.setMonitorArea(uuid, !restoreMonitorLayout, monitorAreaArray)
    }

    override fun onSetMonitorAreaSuccess() {
        AppLogger.w("设置侦测区域成功了")
        ToastUtil.showToast(getString(R.string.PWD_OK_2))
        callBack?.callBack(null)
        exitToParent()
    }

    override fun onSetMonitorAreaError() {
        AppLogger.w("设置侦测区域失败了")
        ToastUtil.showToast(getString(R.string.SETTINGS_FAILED))
    }

    override fun onRestoreMonitorAreaSetting(rects: List<DpMsgDefine.Rect4F>) {
        AppLogger.w("onRestoreMonitorAreaSetting:$rects")
        monitorAreaArray.clear()
        monitorAreaArray.addAll(rects)
        toggleMonitorAreaMode(false)
    }

    override fun onRestoreDefaultMonitorAreaSetting() {
        AppLogger.w("onRestoreDefaultMonitorAreaSetting")
        toggleMonitorAreaMode(true)
    }


    override fun showLoadingBar() {
        load_bar.visibility = View.VISIBLE
    }

    override fun hideLoadingBar() {
        load_bar.visibility = View.GONE
    }

    companion object {
        @JvmStatic
        fun newInstance(uuid: String): MonitorAreaSettingFragment {
            val fragment = MonitorAreaSettingFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }
}