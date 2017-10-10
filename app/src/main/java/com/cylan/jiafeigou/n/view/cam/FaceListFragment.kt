package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.view.JFGPresenter
import com.cylan.jiafeigou.base.view.JFGView
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceItem
import com.cylan.jiafeigou.support.log.AppLogger
import com.github.promeg.pinyinhelper.Pinyin
import com.mikepenz.fastadapter.commons.adapters.FastItemAdapter
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<JFGPresenter<JFGView>>() {

    lateinit var adapter: FastItemAdapter<FaceItem>
    override fun setFragmentComponent(fragmentComponent: FragmentComponent?) {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager
    override fun initViewAndListener() {
        super.initViewAndListener()
        adapter = FastItemAdapter()
        adapter.itemAdapter.withComparator { a, b ->
            return@withComparator Pinyin.toPinyin(a.text, "").compareTo(Pinyin.toPinyin(b.text, ""))
        }

        layoutManager = LinearLayoutManager(context)
        face_list_items.adapter = adapter
        face_list_items.layoutManager = layoutManager
        face_list_items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                //TODO 更新右边的 slider
                val itemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                val faceItem = adapter.getItem(itemPosition)
                val pinyin = Pinyin.toPinyin(faceItem.text, "")



//                face_list_slider.setIndex()
            }

        })
        face_list_slider.setOnTouchLetterChangeListenner { isTouch, letter ->
            AppLogger.w("isTouch:$isTouch,letter:$letter")

        }

    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    companion object {

        fun newInstance(uuid: String): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            fragment.arguments = argument
            return fragment
        }
    }


}