import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Specialist extends User {
    private final String specialization;
    private final Set<BookableService> services = new HashSet<>();      // N:M, агрегация
    private final List<Appointment> appointments = new ArrayList<>();   // 1:N

    public Specialist(int id, String name, String specialization) {
        super(id, name);
        if (specialization == null || specialization.isBlank())
            throw new IllegalArgumentException("Specialization must not be empty");
        this.specialization = specialization;
    }

    public String getSpecialization() { return specialization; }
    public Set<BookableService> getServices() { return services; }
    public List<Appointment> getAppointments() { return appointments; }
    public void addAppointment(Appointment a) { appointments.add(a); }
    public void removeAppointment(Appointment a) { appointments.remove(a); }

    public void addService(BookableService s) {   // обновляем обе стороны N:M
        services.add(s);
        s.getSpecialists().add(this);
    }

    @Override
    public String getRole() { return "SPECIALIST"; }

    @Override
    public String toString() { return getName() + " (" + specialization + ")"; }
}