package com.cylan.jiafeigou.n.view.cam

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cylan.jiafeigou.R
import com.cylan.jiafeigou.base.injector.component.FragmentComponent
import com.cylan.jiafeigou.base.wrapper.BaseFragment
import com.cylan.jiafeigou.dp.DpMsgDefine
import com.cylan.jiafeigou.misc.JConstant
import com.cylan.jiafeigou.n.view.cam.item.FaceListHeaderItem
import com.cylan.jiafeigou.n.view.cam.item.FaceListItem
import com.github.promeg.pinyinhelper.Pinyin
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.adapters.HeaderAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import kotlinx.android.synthetic.main.fragment_facelist.*

/**
 * Created by yanzhendong on 2017/10/9.
 */
class FaceListFragment : BaseFragment<FaceListContact.Presenter>(), FaceListContact.View {

    lateinit var adapter: FastAdapter<*>
    private lateinit var itemAdapter: ItemAdapter<FaceListItem>
    override fun setFragmentComponent(fragmentComponent: FragmentComponent) {
        fragmentComponent.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_facelist, container, false)
        return view
    }

    lateinit var layoutManager: LinearLayoutManager


    override fun initViewAndListener() {
        super.initViewAndListener()
        account = arguments.getString("account")

        when (arguments?.getInt("type", TYPE_ADD_TO)) {
            TYPE_ADD_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
            TYPE_MOVE_TO -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_FACE_MOVE)
            }
            else -> {
                custom_toolbar.setToolbarLeftTitle(R.string.MESSAGES_IDENTIFY_ADD_BTN)
            }
        }

        adapter = FastAdapter<IItem<*, *>>()

        val headerAdapter = HeaderAdapter<FaceListHeaderItem>()
        headerAdapter.withUseIdDistributor(true)
        itemAdapter = ItemAdapter()

        itemAdapter.wrap(headerAdapter.wrap(adapter))
        headerAdapter.add(FaceListHeaderItem())
        itemAdapter.withUseIdDistributor(true)
        adapter.withMultiSelect(false)
        adapter.withAllowDeselection(false)
        itemAdapter.withComparator { item1, item2 ->
            val pinyin1 = Pinyin.toPinyin(item1.faceInformation?.face_name, "") ?: ""
            val pinyin2 = Pinyin.toPinyin(item2.faceInformation?.face_name, "") ?: ""
            return@withComparator pinyin1.compareTo(pinyin2, true)
        }
//        adapter.withOnPreClickListener { v, adapter, item, position ->
//            // consume otherwise radio/checkbox will be deselected
//            return@withOnPreClickListener true
//        }
        itemAdapter.fastAdapter.withEventHook(object : ClickEventHook<FaceListItem>() {

            override fun onBindMany(viewHolder: RecyclerView.ViewHolder): MutableList<View>? {
                if (viewHolder is FaceListItem.FaceListViewHolder) {
                    return mutableListOf(viewHolder.itemView, viewHolder.radio)
                }
                return null
            }

            override fun onClick(v: View?, position: Int, fastAdapter: FastAdapter<FaceListItem>, item: FaceListItem) {
                if (!item.isSelected) {
                    val selections = fastAdapter.selections
                    if (!selections.isEmpty()) {
                        val selectedPosition = selections.iterator().next()
                        fastAdapter.deselect()
                        fastAdapter.notifyItemChanged(selectedPosition)
                    }
                    fastAdapter.select(position)
                }
            }

        })



        layoutManager = LinearLayoutManager(context)
        face_list_items.adapter = adapter


        face_list_items.layoutManager = layoutManager

        //todo just for test
        val items: MutableList<FaceListItem> = mutableListOf()
        for (i in 0..100) {
            val item = FaceListItem()
            val information = DpMsgDefine.FaceInformation()
            information.face_name = "ABC$i"
            item.withFaceInformation(information)
            items.add(item)
        }
        itemAdapter.add(items)
    }

    override fun onStart() {
        super.onStart()
        presenter.loadPersonItems(account!!, uuid)
    }

    var resultCallback: ((a: Any, b: Any, c: Any) -> Unit)? = null

    private var account: String? = null

    companion object {
        const val TYPE_ADD_TO = 1
        const val TYPE_MOVE_TO = 2
        fun newInstance(account: String, uuid: String, type: Int = TYPE_ADD_TO): FaceListFragment {
            val fragment = FaceListFragment()
            val argument = Bundle()
            argument.putString(JConstant.KEY_DEVICE_ITEM_UUID, uuid)
            argument.putInt("type", type)
            argument.putString("account", account)
            fragment.arguments = argument
            return fragment
        }
    }


}