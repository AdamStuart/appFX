package database.todo;

public class Todo {

    private int id;
    private String name;
    private String desc;
	
    public Todo () {
    }
		
    public Todo (int i, String s1, String s2) {

        id = i;
        name = s1;
        desc = s2;
    }
	
    public Todo (String s1, String s2) {

        name = s1;
        desc = s2;
    }

    int getId() {
	
        return id;
    }
    void setId(int i) {
	
        id = i;
    }
	
    String getName() {
	
        return name;
    }
    void setName(String s) {
	
        name = s;
    }
	
    String getDesc() {
	
        return desc;
    }
    void setDesc(String s) {
	
        desc = s;
    }
	
    @Override
    public String toString() {
	
        return name;
    }
}