package ir.khu.safarban;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private TripAdapter adapter;
    private Context context;
    private Drawable icon;
    private final ColorDrawable background;
    private RecyclerView recyclerView;

    public SwipeToDeleteCallback(TripAdapter adapter, Context context, RecyclerView recyclerView) {
        super(0, ItemTouchHelper.LEFT);
        this.adapter = adapter;
        this.context = context;
        this.recyclerView = recyclerView;
        this.icon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        this.background = new ColorDrawable(Color.RED);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        Trip trip = adapter.getTripAt(position);

        // حذف از لیست لوکال و نوتیفای
        adapter.removeTripAt(position);

        // نمایش Snackbar برای Undo
        Snackbar snackbar = Snackbar.make(recyclerView, "سفر حذف شد", Snackbar.LENGTH_LONG);
        snackbar.setAction("بازگردانی", v -> {
            adapter.restoreTrip(trip, position);
        });
        snackbar.show();

        // حذف از Firestore بعد از تایید کاربر (بعد از Snackbar)
        // در اینجا برای سادگی، حذف Firestore را با تأخیر انجام نمی‌دهیم
        // اگر می‌خواهید حذف Firestore را با تأخیر انجام دهید، می‌توانید از Handler استفاده کنید.
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection("users").document(userId)
                .collection("trips").document(trip.getId())
                .delete();
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                            @NonNull RecyclerView.ViewHolder viewHolder,
                            float dX, float dY,
                            int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        // بک‌گراند قرمز
        background.setBounds(itemView.getRight() + (int) dX - backgroundCornerOffset,
                itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(c);

        // آیکون سطل زباله
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + iconMargin;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
        int iconRight = itemView.getRight() - iconMargin;
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        icon.draw(c);
    }
}
