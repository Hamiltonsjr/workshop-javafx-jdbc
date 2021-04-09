package model.entities;

import java.util.Objects;

public class Department {

    private Integer id;
    private String Name;

    public Department(){

    }

    public Department(Integer id, String name) {
        this.id = id;
        Name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return id.equals(that.id) && Name.equals(that.Name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, Name);
    }

    @Override
    public String toString() {
        return "Department{" +
                "id='" + id + '\'' +
                ", Name='" + Name + '\'' +
                '}';
    }
}
