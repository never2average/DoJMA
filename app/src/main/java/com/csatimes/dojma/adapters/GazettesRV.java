package com.csatimes.dojma.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.csatimes.dojma.R;
import com.csatimes.dojma.models.GazetteItem;
import com.csatimes.dojma.utilities.CircleImageDrawable;
import com.facebook.drawee.view.SimpleDraweeView;

import io.realm.RealmResults;

/**
 * Adapter to handle the data in the rv
 */

public class GazettesRV extends RecyclerView.Adapter<GazettesRV.GazetteItemViewHolder> {

    public static final int SINGLE_CLICK = 0;
    public static final int LONG_CLICK = 1;

    private RealmResults<GazetteItem> gazetteItems;
    private onGazetteItemClickedListener onGazetteItemClickedListener;

    public GazettesRV(RealmResults<GazetteItem> gazetteItems) {
        this.gazetteItems = gazetteItems;
        this.onGazetteItemClickedListener = null;
    }

    @Override
    public GazettesRV.GazetteItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GazetteItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout
                .item_format_gazette, parent, false));
    }

    @Override
    public void onBindViewHolder(GazettesRV.GazetteItemViewHolder holder, int position) {
        holder.title.setText(gazetteItems.get(position).getReleaseDateFormatted());
        holder.image.setImageURI(gazetteItems.get(position).getImageUrl());
    }

    @Override
    public int getItemCount() {
        return gazetteItems.size();
    }

    public void setOnGazetteItemClickedListener(GazettesRV.onGazetteItemClickedListener onGazetteItemClickedListener) {
        this.onGazetteItemClickedListener = onGazetteItemClickedListener;
    }

    public interface onGazetteItemClickedListener {
        void onClicked(GazetteItem gi, int clickType);
    }

    class GazetteItemViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public SimpleDraweeView image;

        GazetteItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.item_format_gazette_title);
            image = (SimpleDraweeView) itemView.findViewById(R.id.item_format_gazette_image);
            image.getHierarchy().setProgressBarImage(new CircleImageDrawable());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onGazetteItemClickedListener != null) {
                        onGazetteItemClickedListener.onClicked(gazetteItems.get(getAdapterPosition()), SINGLE_CLICK);
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onGazetteItemClickedListener != null) {
                        onGazetteItemClickedListener.onClicked(gazetteItems.get(getAdapterPosition()), LONG_CLICK);
                    }
                    return false;
                }
            });
        }
    }


}
