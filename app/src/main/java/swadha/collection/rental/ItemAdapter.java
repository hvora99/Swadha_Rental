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
        if (item.isLocked()) {
            holder.itemView.setAlpha(0.5f);
        }
        if (!item.isAvailable()) {
            holder.itemView.setAlpha(0.4f);   // faded item
        } else {
            holder.itemView.setAlpha(1f);
        }


        if (item.isLocked()) {

            holder.tvStatus.setText("🔒");
            holder.tvStatus.setTextColor(Color.RED);

        }
        else if (item.isWashing()) {

            holder.tvStatus.setText("🧼");
            holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));

        }
        else if (item.isBooked()) {

            holder.tvStatus.setText("🔴");
            holder.tvStatus.setTextColor(Color.parseColor("#D32F2F"));

        }
        else {

            holder.tvStatus.setText("🟢");
            holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"));
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

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvRowItemName);
            tvNo = itemView.findViewById(R.id.tvCircleItemNo);
            tvRent = itemView.findViewById(R.id.tvRowRent);
            tvDeposit = itemView.findViewById(R.id.tvRowDeposit);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}