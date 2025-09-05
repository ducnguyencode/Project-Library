// Models/Subject.java
// POJO nhỏ cho SUBJECTS – dùng cho combobox chọn thể loại
package Model;

public class Subject {
    public int    subjectCode;
    public String subjectName;

    @Override public String toString() {
        return subjectCode + " - " + subjectName;
    }
}
