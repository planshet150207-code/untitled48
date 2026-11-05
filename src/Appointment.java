import java.time.LocalDateTime;
import java.util.Comparator;

public class Appointment implements Comparable<Appointment> {
    public enum Status { PENDING, CONFIRMED, COMPLETED, CANCELLED }
    public enum Priority { HIGH, NORMAL, LOW }

    // Композиция: Payment создаётся только внутри Appointment
    public static class Payment {
        private final double amount;
        private boolean paid;
        private Payment(double amount) { this.amount = amount; }
        public double getAmount() { return amount; }
        public boolean isPaid() { return paid; }
        public void pay() { paid = true; }
    }

    private static long counter = 0;

    private final int id;
    private final long order = ++counter;   // порядок поступления (для FIFO)
    private final Client client;
    private final Specialist specialist;
    private final BookableService service;
    private final Priority priority;
    private final LocalDateTime dateTime;
    private final Payment payment;
    private Status status = Status.PENDING;

    public Appointment(int id, Client client, Specialist specialist, BookableService service,
                       LocalDateTime dateTime, Priority priority) {
        if (id <= 0 || dateTime == null || priority == null)
            throw new IllegalArgumentException("Invalid appointment data");
        this.id = id;
        this.client = client;
        this.specialist = specialist;
        this.service = service;
        this.dateTime = dateTime;
        this.priority = priority;
        this.payment = new Payment(service.getPrice());
    }

    public int getId() { return id; }
    public Client getClient() { return client; }
    public Specialist getSpecialist() { return specialist; }
    public LocalDateTime getDateTime() { return dateTime; }
    public Priority getPriority() { return priority; }
    public Status getStatus() { return status; }
    public Payment getPayment() { return payment; }
    public double getPrice() { return service.getPrice(); }
    void setStatus(Status status) { this.status = status; }

    // Три Comparator
    public static final Comparator<Appointment> BY_PRICE =
            Comparator.comparingDouble(Appointment::getPrice);
    public static final Comparator<Appointment> BY_CLIENT =
            Comparator.comparing((Appointment a) -> a.client.getName()).thenComparing(Appointment::getDateTime);
    public static final Comparator<Appointment> BY_PRIORITY =   // приоритет, затем FIFO
            Comparator.comparing(Appointment::getPriority).thenComparingLong(a -> a.order);

    @Override
    public int compareTo(Appointment o) {   // естественный порядок: дата, затем id
        int c = dateTime.compareTo(o.dateTime);
        return c != 0 ? c : Integer.compare(id, o.id);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Appointment a && id == a.id;
    }

    @Override
    public int hashCode() { return Integer.hashCode(id); }

    @Override
    public String toString() {
        return "#" + id + " " + dateTime.toLocalTime() + " " + client + " -> " + specialist
                + " [" + service + ", " + status + ", " + priority + "]";
    }
}