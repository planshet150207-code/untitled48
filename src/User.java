public abstract class User {
    private final int id;
    private String name;

    protected User(int id, String name) {
        if (id <= 0) throw new IllegalArgumentException("id must be positive");
        this.id = id;
        setName(name);
    }

    public int getId() { return id; }
    public String getName() { return name; }

    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be empty");
        this.name = name;
    }

    public abstract String getRole();

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && id == ((User) o).id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() { return name; }
}