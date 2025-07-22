package ir.khu.safarban;

public class Experience {
    private String title;
    private String checklist;
    private String comments;

    public Experience() {
        // نیاز برای Firebase
    }

    public Experience(String title, String checklist, String comments) {
        this.title = title;
        this.checklist = checklist;
        this.comments = comments;
    }

    public String getTitle() {
        return title;
    }

    public String getChecklist() {
        return checklist;
    }

    public String getComments() {
        return comments;
    }
}
