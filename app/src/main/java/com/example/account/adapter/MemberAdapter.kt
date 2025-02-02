package com.example.account.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.account.R
import com.example.account.data.Member

class MemberAdapter(
    private var members: List<Member>,
    private val onDelete: (Int) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.tvName)
        val emailText: TextView = view.findViewById(R.id.tvEmail)
        val deleteButton: Button = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        holder.nameText.text = member.name
        holder.emailText.text = member.email

        holder.deleteButton.setOnClickListener {
            onDelete(member.id)
        }
    }

    override fun getItemCount(): Int = members.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateMembers(newMembers: List<Member>) {
        members = newMembers
        notifyDataSetChanged()
    }
}