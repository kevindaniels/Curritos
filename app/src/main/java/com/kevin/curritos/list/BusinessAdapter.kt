package com.kevin.curritos.list

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.kevin.curritos.R
import com.kevin.curritos.base.Logger
import com.kevin.curritos.model.Business

class BusinessAdapter(
    private val businesses: ArrayList<Business>,
    private val businessClickListener: BusinessClickListener
) :
    RecyclerView.Adapter<BusinessAdapter.BusinessViewHolder>() {

    inner class BusinessViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val nameTV: TextView = itemView.findViewById(R.id.business_name)
        val imageIV: ImageView = itemView.findViewById(R.id.business_image)
        val addressTV: TextView = itemView.findViewById(R.id.business_address)
        val priceAndRatingTV: TextView = itemView.findViewById(R.id.business_price_and_rating)
        val phoneTV: TextView = itemView.findViewById(R.id.business_phone_number)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            if (view == null) return
            businessClickListener.onBusinessClicked(imageIV, businesses[adapterPosition])
        }
    }

    interface BusinessClickListener {
        fun onBusinessClicked(businessImage: ImageView, business: Business)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BusinessViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_business_card, parent, false)
        return BusinessViewHolder(view)
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {
        val business = businesses[position]
        holder.apply {
            // Name
            val name = business.name
            nameTV.apply {
                text = if(name.isNotBlank()) {
                    name
                } else {
                    resources.getString(R.string.business_unknown_name)
                }
            }

            // Photo
            val photoUrl = business.photoUrl
            if(!photoUrl.isNullOrBlank()) {
                Glide.with(nameTV.context)
                    .load(photoUrl)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // If there is an error loading the image, we set the photo URL to null
                            // so that the detail page doesn't bother trying to load, and our burrito
                            // man just shows up. (Search area code 65656 to see this. "Roadhouse Grill
                            // & Pub")
                            business.photoUrl = null
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                    })
                    .placeholder(R.drawable.ic_burrito_man)
                    .into(imageIV)
            } else {
                imageIV.setImageResource(R.drawable.ic_burrito_man)
            }
            ViewCompat.setTransitionName(imageIV, business.id)


            // Address
            val address = business.address
            addressTV.apply {
                if (address.isNotBlank()) {
                    text = address
                    visibility = View.VISIBLE
                } else {
                    visibility = View.INVISIBLE
                }
            }

            // Price/rating
            val price = business.price
            val rating = business.rating
            val priceAndRatingsText = when {
                price.isNotBlank() && rating >= 0 -> "${business.price} • ${business.rating}"
                rating >= 0 -> rating.toString()
                price.isNotBlank() -> price
                else -> ""
            }
            priceAndRatingTV.text = priceAndRatingsText

            // Phone number and price/rating margin
            val phoneNumber = business.phoneNumber
            phoneTV.apply {
                val ratingsLayoutParams: ViewGroup.MarginLayoutParams =
                    priceAndRatingTV.layoutParams as ViewGroup.MarginLayoutParams
                ratingsLayoutParams.marginStart = if (phoneNumber.isNotBlank()) {
                    text = phoneNumber
                    visibility = View.VISIBLE
                    resources.getDimensionPixelSize(R.dimen.business_margin)
                } else {
                    visibility = View.GONE
                    0
                }
            }
        }
    }

    override fun getItemCount() = businesses.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateData(newBusinesses: List<Business>) {
        // logic so we don't unnecessarily update the recycler view with the same data
        var isSame = newBusinesses.size == businesses.size
        if(isSame) {
            for(index in newBusinesses.indices) {
                if(businesses[index].id != newBusinesses[index].id) {
                    isSame = false
                    break
                }
            }
            if(isSame) {
                Logger.d("Same data, not updating recycler view.")
                return
            }
        }
        businesses.apply {
            clear()
            addAll(newBusinesses)
        }
        notifyDataSetChanged()
    }
}

