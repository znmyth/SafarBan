package ir.khu.safarban;

import java.util.List;

public class Trip {
    private String id;
    private String destination;
    private String startDate;  // فرمت مثلا "1404/05/10"
    private String endDate;
    private List<String> checklist;  // چک‌لیست سفر

    public Trip() {
        // سازنده پیش‌فرض لازم برای Firebase Firestore
    }

    public Trip(String id, String destination, String startDate, String endDate, List<String> checklist) {
        this.id = id;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.checklist = checklist;
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public List<String> getChecklist() { return checklist; }
    public void setChecklist(List<String> checklist) { this.checklist = checklist; }

    // متدهای اضافی برای سازگاری با Adapter و کدهای دیگر:

    // این متد بجای getTitle صدا زده می‌شود، عنوان سفر می‌شود مقصد
    public String getTitle() {
        return destination;
    }

    // این متد بجای getDate صدا زده می‌شود، تاریخ شروع سفر می‌شود
    public String getDate() {
        return startDate;
    }
}
