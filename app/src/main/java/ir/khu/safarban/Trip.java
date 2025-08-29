package ir.khu.safarban;

import java.util.List;

public class Trip {
    private String id;
    private String destination;
    private String startDate;  // مثل "1404/05/10"
    private String endDate;

    // قبلاً: List<String>  ❌
    // الان: List<ChecklistItem> ✅
    private List<ChecklistItem> checklist;

    public Trip() {
        // سازنده پیش‌فرض برای Firestore
    }

    public Trip(String id, String destination, String startDate, String endDate, List<ChecklistItem> checklist) {
        this.id = id;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checklist = checklist;
    }

    // --- Getters / Setters ---
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<ChecklistItem> getChecklist() { return checklist; }
    public void setChecklist(List<ChecklistItem> checklist) { this.checklist = checklist; }

    // برای سازگاری با کدهای قبلی:
    // برای سازگاری با کدهای قبلی:
    public String getTitle() { return destination; }
    public String getDate() { return startDate; }

    // تعداد آیتم‌های تیک‌نخورده
    public int getUncheckedCount() {
        if (checklist == null) return 0;

        int count = 0;
        for (ChecklistItem item : checklist) {
            if (item != null && !item.isChecked()) {
                count++;
            }
        }
        return count;
    }
}

