package com.izontechnology.dcapp.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.izontechnology.dcapp.utils.RecyclerViewLayoutManager.LINEAR
import com.izontechnology.dcapp.utils.RecyclerViewLayoutManager.STAGGERED


fun <T : Any?, U : ViewDataBinding> RecyclerView.setUpRecyclerView_Binding(
    @LayoutRes singleItemView: Int,
    itemList: ArrayList<T?>,
    @RecyclerViewLayoutManager.LayoutManager layoutManager: Int,
    @RecyclerViewLinearLayout.Orientation orientation: Int,
    builder: RecyclerViewBuilder_Binding<T, U>.() -> Unit
) = RecyclerViewBuilder_Binding<T, U>(
    this,
    singleItemView,
    itemList,
    layoutManager,
    orientation
).apply(builder)


class RecyclerViewBuilder_Binding<T : Any?, U : ViewDataBinding>
/**
 * @param - Recycler View
 * @param - LIst to bind with RecyclerView
 **/(
    val recyclerView: RecyclerView,
    val singleItemView: Int,
    val mItems: ArrayList<T?>,
    @RecyclerViewLayoutManager.LayoutManager val layoutManager: Int,
    @RecyclerViewLinearLayout.Orientation val orientation: Int
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_ITEM = 1011
    private val VIEW_TYPE_LOADER = 1021

    //    var itemView: Int = 0
    var isLoading = false
    var hasMore = false
    var contentBindingListener: ((T, U, Int) -> Unit)? = null
    var loadMoreListener: (() -> Unit)? = null

    fun contentBinder(l: (T, U, Int) -> Unit) {
        contentBindingListener = l
    }

    var spanCount: (() -> Int)? = { 1 }
        set(value) {
            (recyclerView.layoutManager as GridLayoutManager).spanCount = value!!.invoke()
        }

    var itemDisplay:Int =mItems.size

    var staggeredSpanCount: (() -> Int)? = { 1 }
        set(value) {
            (recyclerView.layoutManager as StaggeredGridLayoutManager).spanCount = value!!.invoke()
        }

    var gapStrategy: (() -> Int)? = { StaggeredGridLayoutManager.GAP_HANDLING_NONE }
        set(value) {
            (recyclerView.layoutManager as StaggeredGridLayoutManager).gapStrategy =
                value!!.invoke()
        }

    var isNestedScrollingEnabled: Boolean = false
        set(value) {
            recyclerView.isNestedScrollingEnabled = value
        }

    init {
        setHasStableIds(true)
        if (layoutManager == LINEAR)
            recyclerView.layoutManager =
                LinearLayoutManager(recyclerView.context, orientation, false)
        else if (layoutManager == RecyclerViewLayoutManager.GRID)
            recyclerView.layoutManager =
                spanCount?.invoke()?.let {
                    GridLayoutManager(recyclerView.context,
                        it, orientation, false)
                }
        else if (layoutManager == STAGGERED) {
            recyclerView.layoutManager =
                staggeredSpanCount?.invoke()?.let { StaggeredGridLayoutManager(it, orientation) }
            gapStrategy?.invoke()
        }
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<U>(inflater, singleItemView, parent, false)
        return MyContentViewHolder(binding)
    }

    override fun getItemCount(): Int = itemDisplay

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is RecyclerViewBuilder_Binding<*, *>.MyContentViewHolder) {
//            holder.binding.setVariable(BR.vm, mItems[position])
            holder.binding.executePendingBindings()
            mItems?.get(position)
                ?.let { contentBindingListener?.invoke(it, holder.binding as U, position) }
        }
    }

    override fun getItemId(position: Int): Long {
        return mItems[position].hashCode().toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }

    inner class MyContentViewHolder(val binding: U) : RecyclerView.ViewHolder(binding.root){

    }
    inner class LoaderViewHolder(val itemView: View) : RecyclerView.ViewHolder(itemView)

    fun enableLoadMore(l: () -> Unit) {
        loadMoreListener = l
        hasMore = true

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                var totalItemCount: Int? = recyclerView?.layoutManager?.itemCount
                var visibleItemCount: Int? = recyclerView?.layoutManager?.childCount
                var pastVisiblesItems: Int? =
                    (recyclerView?.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                if (!isLoading) {
                    if (totalItemCount!! <= (pastVisiblesItems!! + 2)) {
                        isLoading = true
                        if (loadMoreListener != null)
                            loadMoreListener?.invoke()
                    }
                }
            }
        })
    }

    fun enableLoadMore(mNestedScrollView: NestedScrollView, l: () -> Unit) {
        loadMoreListener = l
        hasMore = true

        mNestedScrollView.setOnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (v?.getChildAt(v.getChildCount() - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.getChildCount() - 1)
                        .getMeasuredHeight() - v.getMeasuredHeight())) &&
                    scrollY > oldScrollY
                ) {
                    val visibleItemCount: Int? = recyclerView.layoutManager?.childCount
                    val totalItemCount: Int? = recyclerView.layoutManager?.itemCount
                    val pastVisiblesItems: Int? =
                        (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

                    if (!isLoading) {
                        if ((pastVisiblesItems?.let { visibleItemCount?.plus(it) })!! >= totalItemCount!!) {
                            isLoading = true
                            if (loadMoreListener != null)
                                loadMoreListener?.invoke()
                        }
                    }
                }
            }
        }
    }

    fun setSwipeToRefresh(
        activity: Activity,
        swipeToRefresh: SwipeRefreshLayout,
        onRefreshListener: () -> Unit
    ) {
        swipeToRefresh.setOnRefreshListener {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            onRefreshListener.invoke()
        }
    }

    fun stopSwipeRefresh(
        activity: Activity,
        swipeToRefresh: SwipeRefreshLayout
    ) {
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        swipeToRefresh.isRefreshing = false
    }

//    fun showLoader() {
//        val pModel = POJOModel().apply { isLoader = true } as T
//        mItems.add(pModel)
//        notifyItemInserted(itemCount - 1)
//    }

//    fun hideLoader() {
//        removeLastItem()
//        stopLoader()
//    }
//
//    fun stopLoader() {
//        isLoading = false
//    }
//
//    fun removeLastItem() {
//        mItems.removeAt(itemCount - 1)
//        notifyDataSetChanged()
//    }

    fun moveItem(from: Int, to: Int) {
        val temp = mItems[from]
        mItems[from] = mItems[to]
        mItems.set(to, temp)
        this.notifyItemChanged(from)
        this.notifyItemChanged(to)
    }


    fun addAll(list: ArrayList<T?>) {
        val position = itemCount
        mItems.addAll(position, list)
        notifyDataSetChanged()
    }

    fun remove(item: T) {
        mItems.remove(item)
        notifyDataSetChanged()
    }

    fun removeAll() {
        mItems.clear()
        notifyDataSetChanged()
    }

    fun addItem(item: T) {
        mItems.add(0, item)
        notifyItemInserted(0)
    }

    fun removeItem(item: T) {
        mItems.remove(item)
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        mItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
    }
}