package ir.khu.safarban;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import saman.zamani.persiandate.PersianDate;

import java.util.ArrayList;
import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {

    private Context context;
    private List<Trip> trips;           // لیست قابل نمایش (فیلتر شده)
    private List<Trip> fullTripsList;   // لیست کامل همه سفرها (بدون فیلتر)

    public TripAdapter(Context context, List<Trip> trips) {
        this.context = context;
        this.trips = new ArrayList<>(trips);
        this.fullTripsList = new ArrayList<>(trips);
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trips.get(position);

        // عنوان سفر
        holder.tvTripTitle.setText(trip.getDestination() != null ? trip.getDestination() : "بدون عنوان");

        // خواندن تعداد آیتم‌های چک نشده از SharedPreferences
        int uncheckedCount = getUncheckedCountFromPrefs(trip.getId());
        holder.tvUncheckedItems.setText("آیتم‌های باقی‌مانده: " + uncheckedCount);

        // محاسبه روزهای باقی‌مانده
        String startDateStr = trip.getStartDate();
        String daysRemainingText;
        boolean isPast = false;

        if (startDateStr != null) {
            int daysRemaining = calculateDaysRemaining(startDateStr);
            if (daysRemaining == Integer.MIN_VALUE) {
                daysRemainingText = "تاریخ نامعتبر";
            } else if (daysRemaining < 0) {
                daysRemainingText = "زمان این سفر گذشته!";
                isPast = true;
            } else {
                daysRemainingText = daysRemaining + " روز مانده تا سفر";
            }
        } else {
            daysRemainingText = "تاریخ نامشخص";
        }

        holder.tvTripRemaining.setText(daysRemainingText);

        // رنگ کارت بسته به گذشته بودن سفر
        if (isPast) {
            holder.cardView.setCardBackgroundColor(Color.LTGRAY);
            holder.tvTripTitle.setTextColor(Color.DKGRAY);
            holder.tvUncheckedItems.setTextColor(Color.DKGRAY);
            holder.tvTripRemaining.setTextColor(Color.DKGRAY);
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
            holder.tvTripTitle.setTextColor(Color.BLACK);
            holder.tvUncheckedItems.setTextColor(Color.GRAY);
            holder.tvTripRemaining.setTextColor(Color.GRAY);
        }

        // کلیک روی کارت برای باز کردن جزئیات سفر
        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TripDetailsActivity.class);
            intent.putExtra("trip_id", trip.getId());
            context.startActivity(intent);
        });

        // دکمه ویرایش (فعلا مخفی)
        holder.btnEdit.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return trips.size();
    }

    // ✅ متد حذف آیتم از لیست
    public void removeTripAt(int position) {
        if (position >= 0 && position < trips.size()) {
            trips.remove(position);
            notifyItemRemoved(position);
        }
    }

    // ✅ متد دریافت سفر خاص
    public Trip getTripAt(int position) {
        if (position >= 0 && position < trips.size()) {
            return trips.get(position);
        }
        return null;
    }

    // ✅ متد بازگردانی آیتم (Undo)
    public void restoreTrip(Trip trip, int position) {
        if (position >= 0 && position <= trips.size() && trip != null) {
            trips.add(position, trip);
            notifyItemInserted(position);
        }
    }

    // متد برای خواندن تعداد آیتم‌های چک نشده از SharedPreferences
    private int getUncheckedCountFromPrefs(String tripId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt("unchecked_count_" + tripId, 0);
    }

    // محاسبه روزهای باقی‌مانده از تاریخ شروع سفر
    private int calculateDaysRemaining(String dateStr) {
        try {
            String[] parts = dateStr.split("/");
            if (parts.length != 3) return Integer.MIN_VALUE;

            int y = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int d = Integer.parseInt(parts[2]);

            PersianDate today = new PersianDate();
            PersianDate startDate = new PersianDate();
            startDate.setShYear(y);
            startDate.setShMonth(m);
            startDate.setShDay(d);

            long diffMillis = startDate.getTime() - today.getTime();
            return (int) (diffMillis / (1000 * 60 * 60 * 24));
        } catch (Exception e) {
            e.printStackTrace();
            return Integer.MIN_VALUE;
        }
    }

    // متد جدید برای فیلتر کردن سفرها بر اساس متن جستجو
    public void filterTrips(String query) {
        trips.clear();
        if (TextUtils.isEmpty(query)) {
            trips.addAll(fullTripsList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Trip trip : fullTripsList) {
                if (trip.getDestination() != null && trip.getDestination().toLowerCase().contains(lowerQuery)) {
                    trips.add(trip);
                }
            }
        }
        notifyDataSetChanged();
    }

    // متد جدید برای به روزرسانی کامل لیست سفرها
    public void updateTrips(List<Trip> newTrips) {
        fullTripsList.clear();
        fullTripsList.addAll(newTrips);

        trips.clear();
        trips.addAll(newTrips);

        notifyDataSetChanged();
    }

    // ویو هولدر
    static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tvTripTitle, tvTripRemaining, tvUncheckedItems;
        CardView cardView;
        ImageButton btnEdit;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTripTitle = itemView.findViewById(R.id.tvTripTitle);
            tvTripRemaining = itemView.findViewById(R.id.tvTripRemaining);
            tvUncheckedItems = itemView.findViewById(R.id.tvUncheckedItems);
            cardView = (CardView) itemView;
            btnEdit = itemView.findViewById(R.id.btnEdit);
        }
    }
}
