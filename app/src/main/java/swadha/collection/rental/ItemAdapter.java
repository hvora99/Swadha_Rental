package swadha.collection.rental;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<ItemModel> itemList;

    private String status;
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row, parent, false);
        return new ItemViewHolder(view);
    }




    public ItemAdapter(List<ItemModel> itemList) {
        this.itemList = itemList;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = itemList.get(position);
        holder.tvName.setText(item.getItemName());
        holder.tvNo.setText(item.getItemNo());
        holder.tvRent.setText("Rent: ₹" + item.getRent());
        holder.tvDeposit.setText("Dep: ₹" + item.getDeposit());


        if(item.isLocked()){
            holder.topBar.setBackgroundColor(Color.parseColor("#D32F2F")); // red
            holder.tvStatus.setText("LOCKED");
        }
        else if("booked".equals(item.getStatus())){
            holder.topBar.setBackgroundColor(Color.parseColor("#1976D2")); // blue
            holder.tvStatus.setText("BOOKED");
        }
        else if("washing".equals(item.getStatus())){
            holder.topBar.setBackgroundColor(Color.parseColor("#FB8C00")); // orange
            holder.tvStatus.setText("WASHING");
        }
        else{
            holder.topBar.setBackgroundColor(Color.parseColor("#2E7D32")); // green
            holder.tvStatus.setText("AVAILABLE");
        }


        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(v.getContext(), ItemDetailActivity.class);
            intent.putExtra("itemNo", item.getItemNo());
            intent.putExtra("itemName", item.getItemName());
            intent.putExtra("rent", item.getRent());
            intent.putExtra("deposit", item.getDeposit());
            intent.putExtra("isLocked", item.isLocked());
            v.getContext().startActivity(intent);
        });
    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvNo, tvRent, tvDeposit,tvStatus;
        View topBar;
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRowItemName);
            tvNo = itemView.findViewById(R.id.tvCircleItemNo);
            tvRent = itemView.findViewById(R.id.tvRowRent);
            tvDeposit = itemView.findViewById(R.id.tvRowDeposit);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            topBar = itemView.findViewById(R.id.topBar);

        }
    }
}