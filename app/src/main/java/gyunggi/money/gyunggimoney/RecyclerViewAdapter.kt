package gyunggi.money.gyunggimoney

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.gyunggimoney.databinding.StoreItemListBinding

class RecyclerViewAdapter(
    var storeList: MutableList<Store>?
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(), Filterable {

    var storeLists: MutableList<Store>? = storeList

    inner class ViewHolder(private val binding: StoreItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(data: Store) {
            binding.store = data
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            StoreItemListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(storeList!![position])
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    override fun getItemCount(): Int = storeList?.size ?: 0

    interface OnItemClickListener {
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener) {
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener: OnItemClickListener

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charString = constraint.toString().replace(" ", "")
                storeList = if (charString.isEmpty()) {
                    storeLists
                } else {
                    val filterList = mutableListOf<Store>()
                    if (storeLists != null) {
                        for (name in storeLists!!) {
                            if (name.storeName.replace(" ", "")
                                    .contains(charString) || name.storeLocation.replace(" ", "")
                                    .contains(
                                        charString
                                    ) || name.storeLocationDoro.contains(charString)
                            ) {
                                filterList.add(name)
                            }
                        }
                    }
                    filterList
                }
                val filterResults = FilterResults()
                filterResults.values = storeList
                return filterResults
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                storeList = results?.values as MutableList<Store>?
                notifyDataSetChanged()
            }
        }
    }
}