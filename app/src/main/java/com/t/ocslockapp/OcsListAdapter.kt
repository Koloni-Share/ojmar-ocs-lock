package com.t.ocslockapp

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.ondo.ocssmartlibrary.OcsLock
import java.util.*

/**
 * Rental list adapter to show number of rental during till the end
 */

class OcsListAdapter(private val mContext: Context, private val mList: ArrayList<OcsLock?>) :
    RecyclerView.Adapter<OcsListAdapter.ViewHolder>() {
    var mInflater: LayoutInflater
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = mInflater.inflate(R.layout.ocs_inflator_ocs_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            holder.tvOcsLockName.text = "Mac : " + mList[position]?.tag.toString()
            holder.tvOcsLockBattery.text =
                "Battery : " + mList[position]?.battLevel.toString() + "%"
//            holder.tvOcsLockIsUserMode.text = mList[position]?.isInUserMode.toString()
            holder.tvOcsLockNumber.text = "Lock No. "+ mList[position]?.lockNumber.toString()

            if (mList[position]?.lockStatus.toString().equals("1")) {
                holder.tvOcsLockStatus.text = "Lock is Close."
            } else {
                holder.tvOcsLockStatus.text = "Lock is Open."
            }
            holder.tvOcsLockType.text = mList[position]?.lockType.toString()
            holder.tvOcsLockRssi.text = "Rssi : " + mList[position]?.rssi.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        holder.tvOcsLockUnLock.setOnClickListener {

            try {
                val animZoomOut = AnimationUtils.loadAnimation(mContext, R.anim.button_zoom_out_animation)
                holder.tvOcsLockUnLock.startAnimation(animZoomOut)
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }

            if (mContext is ScanActivity) {
                mContext.onConnectToOcsLock(position, mList[position]!!)
            }else if (mContext is SingleScreenOperationActivity) {
                mContext.onConnectToOcsLock(position, mList[position]!!)
            }else if (mContext is SingleScreenOperationActivityT4) {
                mContext.onConnectToOcsLock(position, mList[position]!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvOcsLockName: TextView
        val tvOcsLockBattery: TextView
        val tvOcsLockIsUserMode: TextView
        val tvOcsLockNumber: TextView
        val tvOcsLockStatus: TextView
        val tvOcsLockType: TextView
        val tvOcsLockRssi: TextView
        val tvOcsLockUnLock: AppCompatButton
        val rlMain: RelativeLayout

        init {
            rlMain = itemView.findViewById(R.id.rlMain)
            tvOcsLockName = itemView.findViewById(R.id.tvOcsLockName)
            tvOcsLockBattery = itemView.findViewById(R.id.tvOcsLockBattery)
            tvOcsLockIsUserMode = itemView.findViewById(R.id.tvOcsLockIsUserMode)
            tvOcsLockNumber = itemView.findViewById(R.id.tvOcsLockNumber)
            tvOcsLockStatus = itemView.findViewById(R.id.tvOcsLockStatus)
            tvOcsLockType = itemView.findViewById(R.id.tvOcsLockType)
            tvOcsLockRssi = itemView.findViewById(R.id.tvOcsLockRssi)
            tvOcsLockUnLock = itemView.findViewById(R.id.tvOcsLockUnLock)
        }
    }

    init {
        mInflater = mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}