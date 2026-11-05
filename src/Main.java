import java.time.LocalDate;
import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        BookingService svc = new BookingService();
        LocalDateTime base = LocalDate.now().plusDays(1).atTime(9, 0);

        BookableService consult = new BookableService(1, "Consultation", 50);
        BookableService checkup = new BookableService(2, "Checkup", 80);
        BookableService cleaning = new BookableService(3, "Cleaning", 120);
        svc.addService(consult);
        svc.addService(checkup);
        svc.addService(cleaning);

        Specialist smith = new Specialist(101, "Dr. Smith", "Therapist");
        Specialist lee = new Specialist(102, "Dr. Lee", "Dentist");
        smith.addService(consult);
        smith.addService(checkup);
        lee.addService(cleaning);
        svc.addSpecialist(smith);
        svc.addSpecialist(lee);

        svc.addClient(new Client(1, "Anna"));
        svc.addClient(new Client(2, "Boris"));

        System.out.println("--- Client submits requests ---");
        svc.submit(1, 1, 101, 1, base.plusHours(1), Appointment.Priority.NORMAL);
        svc.submit(2, 2, 101, 2, base.plusHours(2), Appointment.Priority.HIGH);
        svc.submit(3, 2, 102, 3, base.plusHours(3), Appointment.Priority.LOW);
        svc.submit(4, 1, 101, 2, base.plusHours(4), Appointment.Priority.NORMAL);

        System.out.println("--- Edge cases ---");
        attempt("Busy slot", () -> svc.submit(5, 1, 101, 1, base.plusHours(1), Appointment.Priority.NORMAL));
        attempt("Service not offered", () -> svc.submit(6, 1, 102, 2, base.plusHours(6), Appointment.Priority.NORMAL));
        attempt("Unknown client", () -> svc.submit(7, 99, 101, 1, base.plusHours(7), Appointment.Priority.NORMAL));
        attempt("Duplicate id", () -> svc.submit(1, 1, 101, 1, base.plusHours(9), Appointment.Priority.NORMAL));
        attempt("Empty name", () -> new Client(3, " "));

        System.out.println("--- Natural order (Comparable) ---");
        svc.sorted(null).forEach(System.out::println);
        System.out.println("--- By price ---");
        svc.sorted(Appointment.BY_PRICE).forEach(System.out::println);
        System.out.println("--- By client ---");
        svc.sorted(Appointment.BY_CLIENT).forEach(System.out::println);
        System.out.println("--- By priority ---");
        svc.sorted(Appointment.BY_PRIORITY).forEach(System.out::println);

        System.out.println("--- Search ---");
        System.out.println("By id: " + svc.getAppointment(2));
        System.out.println("Dentists: " + svc.bySpecialization("Dentist"));
        System.out.println("Price >= 80: " + svc.filter(a -> a.getPrice() >= 80).size());
        System.out.println("Anna's appointments: " + svc.getClient(1).getAppointments().size());

        System.out.println("--- Specialist processes queue (Dr. Smith) ---");
        System.out.println("In queue: " + svc.queueSize(101));
        for (int i = 0; i < 2; i++) {
            System.out.println("poll() -> " + svc.processNext(101).map(Appointment::toString).orElse("empty"));
        }

        System.out.println("--- Update / Delete ---");
        svc.complete(2);
        svc.cancel(4);
        svc.renameClient(1, "Anna K.");
        svc.delete(3);
        attempt("Complete a CANCELLED one", () -> svc.complete(4));
        System.out.println("Dr. Smith queue: " + svc.queueSize(101));

        svc.printReport();
    }

    private static void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println(label + " -> OK");
        } catch (RuntimeException e) {
            System.out.println(label + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}