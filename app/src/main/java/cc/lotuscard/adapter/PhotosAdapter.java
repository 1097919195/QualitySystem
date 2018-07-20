package cc.lotuscard.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cc.lotuscard.identificationcardtest.R;

/**
 * Created by Administrator on 2018/7/18 0018.
 */

public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.MyViewHolder> {
    private Context mContext;
    private List<String> data;
    private OnItemClickListener onItemClickListener;
    private List<String> type;

    public PhotosAdapter(Context mContext, List<String> data) {
        this.mContext = mContext;
        this.data = data;
    }

    public PhotosAdapter(Context mContext, List<String> data , List<String> type) {
        this.mContext = mContext;
        this.data = data;
        this.type = type;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_take_photos, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String str = data.get(position);
        String typeStr = type.get(position);
        holder.text.setText(str);

        holder.text.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onClick(position,typeStr);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView text;
        public MyViewHolder(View itemView) {
            super(itemView);
//            text = itemView.findViewById(R.id.num);
        }
    }

    public interface OnItemClickListener {
        void onClick(int position ,String type);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


}
