    package swadha.collection.rental;

    import android.content.Intent;
    import android.graphics.Color;
    import android.util.Log;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;
    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Calendar;
    import java.util.Date;
    import java.util.List;
    import java.util.Locale;

    public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

        private List<RentalBooking> originalList;
        private List<RentalBooking> filteredList;

        public interface OnItemLongClickListener {
            void onItemLongClick(RentalBooking booking);
        }

        private OnItemLongClickListener longClickListener;

        public void setOnItemLongClickListener(OnItemLongClickListener listener) {
            this.longClickListener = listener;
        }

        public BookingAdapter(List<RentalBooking> bookingList) {
            this.originalList = bookingList;
            this.filteredList = new ArrayList<>(bookingList);
        }


        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            // Ensure this layout file matches the one we fixed with Weights and Ellipsize
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_list, parent, false);
            return new BookingViewHolder(view);
        }

        public void refreshData(List<RentalBooking> newList) {
            originalList = newList;
            filteredList = new ArrayList<>(newList);
            notifyDataSetChanged();
        }


        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            RentalBooking booking = filteredList.get(position);

            long pickupMs = booking.getPickupMs();
            long returnMs = booking.getReturnMs();

            Date pickupDate = new Date(pickupMs);
            Date returnDate = new Date(returnMs);

            SimpleDateFormat dateFormat =
                    new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

            SimpleDateFormat timeFormat =
                    new SimpleDateFormat("HH:mm", Locale.getDefault());

            boolean pickupToday = isToday(pickupDate);
            boolean returnToday = isToday(returnDate);

            String status = booking.getStatus();

            // 1. Set the Data to UI
            // ---------- SET UI ----------

            holder.tvItemCircle.setText(booking.getOrderId()); // order id instead of item

            holder.tvCustomerName.setText(booking.getName());


            holder.tvPickupDate.setText(dateFormat.format(pickupDate));
            holder.tvPickupTime.setText(timeFormat.format(pickupDate));

            holder.tvReturnDate.setText(dateFormat.format(returnDate));
            holder.tvReturnTime.setText(timeFormat.format(returnDate));


            int color = Color.parseColor("#800020"); // default

            if (returnToday) {
                color = Color.parseColor("#D32F2F"); // red
            }
            else if (pickupToday) {
                color = Color.parseColor("#1976D2"); // blue
            }

            holder.brandAccent.setBackgroundColor(color);

            try {

                if (pickupDate == null || returnDate == null) {

                    holder.tvPickupDate.setText("Pickup: -");
                    holder.tvPickupTime.setText("-");

                    holder.tvReturnDate.setText("Return: -");
                    holder.tvReturnTime.setText("-");

                } else {

                    holder.tvPickupDate.setText("Pickup: " + dateFormat.format(pickupDate));
                    holder.tvPickupTime.setText(timeFormat.format(pickupDate));

                    holder.tvReturnDate.setText("Return: " + dateFormat.format(returnDate));
                    holder.tvReturnTime.setText(timeFormat.format(returnDate));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            // 2. Handle Regular Click (Open Detail Activity)
            holder.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(v.getContext(), ReturnDetailActivity.class);

                intent.putExtra("orderId", booking.getOrderId());
                intent.putExtra("items", booking.getItemsString());
                intent.putExtra("itemNo", booking.getFirstItem());

                intent.putExtra("name", booking.getName());
                intent.putExtra("phone", booking.getPhone());

                intent.putExtra("pickupDateTime", booking.getPickupMs());
                intent.putExtra("returnDateTime", booking.getReturnMs());
                intent.putExtra("washDateTime", booking.getWashMs());
                intent.putExtra("actualPickupDateTime", booking.getActualPickupMs());

                intent.putExtra("totalRent", booking.getTotalRent());
                intent.putExtra("deposit", booking.getDeposit());
                intent.putExtra("rentPaid", booking.getRentPaid());
                intent.putExtra("balance", booking.getBalance());

                intent.putExtra("status", booking.getStatus());
                intent.putExtra("timestamp", booking.getTimestamp());

                // ✅ send ItemStatus objects directly
                intent.putExtra(
                        "itemsList",
                        new ArrayList<>(booking.getItems())
                );

                v.getContext().startActivity(intent);
            });

            double totalRent = booking.getTotalRent();
            double rentPaid = booking.getRentPaid();
            double deposit = booking.getDeposit();

            double rentDue = totalRent - rentPaid;

            if (rentDue > 0) {

                holder.tvBalance.setText("Collect ₹" + String.format("%.2f", rentDue));
                holder.tvBalance.setTextColor(Color.parseColor("#D32F2F"));

            } else {

                double refund = deposit + Math.abs(rentDue);

                holder.tvBalance.setText("Refund ₹" + String.format("%.2f", refund));
                holder.tvBalance.setTextColor(Color.parseColor("#2E7D32"));
            }

            // 3. Handle Long Click (For the "Mark as Returned" Popup)
            holder.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(booking);
                }
                return true;
            });
        }

        public void filter(String query) {

            filteredList.clear();

            if (query == null || query.trim().isEmpty()) {

                filteredList.addAll(originalList);

            } else {

                query = query.toLowerCase().trim();

                for (RentalBooking booking : originalList) {

                    String phone = booking.getPhone() == null ? "" : booking.getPhone().toLowerCase();

                    String orderId = booking.getOrderId() == null ? "" : booking.getOrderId().toLowerCase();

                    String name = booking.getName() == null ? "" : booking.getName().toLowerCase();

                    String items = booking.getItemsString() == null ? "" : booking.getItemsString().toLowerCase();

                    // ⭐ Remove prefix so searching "24021" finds "SVD-24021"
                    String orderShort = orderId.replace("svd-", "");

                    if (phone.contains(query)
                            || orderId.contains(query)
                            || orderShort.contains(query)
                            || name.contains(query)
                            || items.contains(query)) {

                        filteredList.add(booking);
                    }
                }

                Log.d("FILTER_DEBUG", "Original Size: " + originalList.size());
                Log.d("FILTER_DEBUG", "Filtered Size: " + filteredList.size());

                notifyDataSetChanged();
            }
        }

        @Override
        public int getItemCount() {
            return filteredList.size();
        }

        public static class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView tvItemCircle, tvCustomerName, tvBalance;
            TextView tvPickupDate, tvPickupTime, tvReturnDate, tvReturnTime;
            View brandAccent;
            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvItemCircle = itemView.findViewById(R.id.tvItemCircle);
                tvCustomerName = itemView.findViewById(R.id.tvRowCustomerName);
                tvPickupDate = itemView.findViewById(R.id.tvRowPickupDate);
                tvPickupTime = itemView.findViewById(R.id.tvRowPickupTime);
                tvReturnDate = itemView.findViewById(R.id.tvRowReturnDate);
                tvReturnTime = itemView.findViewById(R.id.tvRowReturnTime);
                tvBalance = itemView.findViewById(R.id.tvRowBalance);
                brandAccent = itemView.findViewById(R.id.brandAccent);

            }
        }

        private boolean isToday(Date date){

            Calendar today = Calendar.getInstance();
            Calendar compare = Calendar.getInstance();
            compare.setTime(date);

            return today.get(Calendar.YEAR) == compare.get(Calendar.YEAR)
                    && today.get(Calendar.DAY_OF_YEAR) == compare.get(Calendar.DAY_OF_YEAR);
        }

    }