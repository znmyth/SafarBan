package ir.khu.safarban;

import com.google.firebase.firestore.Exclude;

public class ChecklistItem {
    private String text;
    private boolean checked;

    // 🔹 لازم برای Firestore (بدون این کرش می‌کنه)
    public ChecklistItem() { }

    // 🔹 سازنده کامل
    public ChecklistItem(String text, boolean checked) {
        this.text = text;
        this.checked = checked;
    }

    // 🔹 سازنده ساده (پیش‌فرض بدون تیک)
    public ChecklistItem(String text) {
        this.text = text;
        this.checked = false;
    }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isChecked() { return checked; }
    public void setChecked(boolean checked) { this.checked = checked; }

    // 🔹 فقط برای دیباگ و نمایش در لاگ
    @Exclude
    @Override
    public String toString() {
        return "ChecklistItem{" +
                "text='" + text + '\'' +
                ", checked=" + checked +
                '}';
    }
}
