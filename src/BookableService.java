import java.util.HashSet;
import java.util.Set;

public class BookableService {
    private final int id;
    private final String name;
    private final double price;
    private final Set<Specialist> specialists = new HashSet<>();

    public BookableService(int id, String name, double price) {
        if (id <= 0 || name == null || name.isBlank() || price < 0)
            throw new IllegalArgumentException("Invalid service data");
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public double getPrice() { return price; }
    public Set<Specialist> getSpecialists() { return specialists; }

    @Override
    public boolean equals(Object o) {
        return o instanceof BookableService s && id == s.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() { return name; }
}
