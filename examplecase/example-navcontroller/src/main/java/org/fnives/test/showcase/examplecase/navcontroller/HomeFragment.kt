package org.fnives.test.showcase.examplecase.navcontroller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(view.context)
        recycler.adapter = Adapter(onClick = {
            if (findNavController().currentDestination?.id != R.id.homeFragment) return@Adapter
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToDetailFragment(it))
        })
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        constructor(parent: ViewGroup) : this(LayoutInflater.from(parent.context).inflate(R.layout.item_home, parent, false))
    }

    class Adapter(
        private val count: Int = 30,
        private val onClick: (Int) -> Unit,
    ) : RecyclerView.Adapter<ViewHolder>() {
        override fun getItemCount(): Int = count

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(parent)

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val context = holder.itemView.context
            (holder.itemView as Button).text = context.getString(R.string.home_item, position)
            holder.itemView.setOnClickListener { onClick(position) }
        }
    }
}
