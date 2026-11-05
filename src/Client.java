import java.util.ArrayList;
import java.util.List;

public class Client extends User {
    private final List<Appointment> appointments = new ArrayList<>();   // 1:N

    public Client(int id, String name) { super(id, name); }

    public List<Appointment> getAppointments() { return appointments; }
    public void addAppointment(Appointment a) { appointments.add(a); }
    public void removeAppointment(Appointment a) { appointments.remove(a); }

    @Override
    public String getRole() { return "CLIENT"; }
}