package ir.khu.safarban;
public class Experience {
    private String title;
    private String checklist;
    private String comments;

    // سازنده بدون پارامتر برای Firebase
    public Experience() {}

    // 🔧 این رو اضافه کن برای ساختن تجربه فقط با عنوان
    public Experience(String title) {
        this.title = title;
        this.checklist = "";
        this.comments = "";
    }

    // گتر و سترها
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}

