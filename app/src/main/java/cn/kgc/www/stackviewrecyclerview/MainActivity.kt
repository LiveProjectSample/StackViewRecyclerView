package cn.kgc.www.stackviewrecyclerview

import android.content.Context
import android.graphics.Canvas
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_recyclerview.view.*

class MainActivity : AppCompatActivity() {

    lateinit var mAdapter: CardViewAdapter
    var heightSpace = 0

    class CardViewAdapter(context: Context): RecyclerView.Adapter<CardViewAdapter.Companion.ViewHolder>() {
        val context = context
        var topPosition = 0


        val dataList = List(5,{it->it})

        override fun getItemCount(): Int {
            return dataList.size
        }

        override fun onBindViewHolder(holder: CardViewAdapter.Companion.ViewHolder, position: Int) {
            holder.tvTitle.setText("card no is $position")
            holder.btnButton.setText("card no is $position")
            holder.btnButton.setOnClickListener {
                Toast.makeText(context, "card no is $position", Toast.LENGTH_LONG).show()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewAdapter.Companion.ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_recyclerview, parent, false))
        }

        companion object {
            class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
                val tvTitle = itemView.textView
                val btnButton = itemView.button
            }
        }
    }

    inner class SwipCardLayoutManager: RecyclerView.LayoutManager() {
        override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
            return RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        override fun onLayoutCompleted(state: RecyclerView.State?) {
            super.onLayoutCompleted(state)
            for(childIndex in 2 downTo 0) {
                val child = getChildAt(childIndex)
                child.translationY = 0f

                child.scaleX = 1 - 0.1f * (2 - childIndex)
                child.scaleY = 1 - 0.1f * (2 - childIndex)

                val deltaY = child.measuredHeight*(1-child.scaleY)/2f

                if (deltaY != 0f) {
                    child.y = child.y - deltaY - heightSpace
                }
            }
        }

        override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
            super.onLayoutChildren(recycler, state)

            detachAndScrapAttachedViews(recycler)

            val topPosition = mAdapter.topPosition
            val itemCount = itemCount
            for(i in 0..2){
                val position = (topPosition + i)%itemCount
                val view = recycler!!.getViewForPosition(position)
                addView(view,0)
                measureChildWithMargins(view, 0, 0)
//                measureChild(view, 0, 0)
                val widthSpace = width - getDecoratedMeasuredWidth(view)
                heightSpace = height -getDecoratedMeasuredHeight(view)
                layoutDecorated(view, widthSpace / 2, heightSpace,
                        widthSpace / 2 + getDecoratedMeasuredWidth(view), heightSpace + getDecoratedMeasuredHeight(view))
            }
        }
    }

    inner class SwipeCardCallBack: ItemTouchHelper.SimpleCallback{
        constructor():super(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT)

        override fun onMove(recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, target: RecyclerView.ViewHolder?): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder?, direction: Int) {
            mAdapter.topPosition = (mAdapter.topPosition + 1)%mAdapter.itemCount
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildDraw(c: Canvas?, recyclerView: RecyclerView?, viewHolder: RecyclerView.ViewHolder?, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            val maxDistance = recyclerView!!.width * 0.5f
            val distance = Math.abs(dX)
            //动画执行的百分比
            var percent = distance / maxDistance
            if (percent > 1) {
                percent = 1.0f
            }
            for(childIndex in 1 downTo 0){
                val zoom = 0.1f*percent
                val child = recyclerView.getChildAt(childIndex)
                child.translationY = 0f

                child.scaleX = 1 - 0.1f * (2 - childIndex) + zoom
                child.scaleY = 1 - 0.1f * (2 - childIndex) + zoom

                if(childIndex == 1) {
                    val deltaY = (1-child.scaleY)/2*(1-percent)*child.height
                    child.y = child.y - deltaY - heightSpace * (1 - percent)
                }else{
                    val deltaY = (1-child.scaleY)/2*child.height
                    child.y = child.y - deltaY - heightSpace
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val itemTouchHelper = ItemTouchHelper(SwipeCardCallBack())
        itemTouchHelper.attachToRecyclerView(recycleView)
        val layoutManager = SwipCardLayoutManager()
        recycleView.layoutManager = layoutManager
        mAdapter = CardViewAdapter(this)
        recycleView.adapter = mAdapter

        val itemAnimator = DefaultItemAnimator()
        itemAnimator.removeDuration = 5000
        recycleView.itemAnimator = itemAnimator
    }
}
