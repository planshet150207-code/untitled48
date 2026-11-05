import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BookingService {
    private final List<Appointment> appointments = new ArrayList<>();                    // A
    private final Map<Integer, Client> clients = new HashMap<>();                        // B: по id
    private final Map<Integer, Specialist> specialists = new HashMap<>();
    private final Map<Integer, BookableService> services = new HashMap<>();
    private final Map<Integer, Appointment> appointmentsById = new HashMap<>();
    private final Map<String, List<Specialist>> bySpecialization = new TreeMap<>();      // B: категории
    private final Map<Integer, Queue<Appointment>> queues = new HashMap<>();             // D: очереди

    // ---------- CREATE ----------
    public synchronized void addClient(Client c) { put(clients, c.getId(), c); }
    public synchronized void addService(BookableService s) { put(services, s.getId(), s); }

    public synchronized void addSpecialist(Specialist s) {
        put(specialists, s.getId(), s);
        bySpecialization.computeIfAbsent(s.getSpecialization(), k -> new ArrayList<>()).add(s);
        queues.put(s.getId(), new PriorityQueue<>(Appointment.BY_PRIORITY));
    }

    /** Роль клиента: подать заявку (попадает в список, индекс, связи и очередь). */
    public synchronized Appointment submit(int id, int clientId, int specId, int serviceId,
                                           LocalDateTime when, Appointment.Priority priority) {
        if (appointmentsById.containsKey(id)) throw new IllegalStateException("Duplicate appointment id: " + id);
        Client c = find(clients, clientId);
        Specialist s = find(specialists, specId);
        BookableService svc = find(services, serviceId);
        if (!s.getServices().contains(svc)) throw new IllegalStateException(s + " does not provide " + svc);
        boolean busy = s.getAppointments().stream().anyMatch(a ->
                a.getStatus() != Appointment.Status.CANCELLED && a.getDateTime().equals(when));
        if (busy) throw new IllegalStateException("Time slot is taken: " + when);

        Appointment a = new Appointment(id, c, s, svc, when, priority);
        appointments.add(a);
        appointmentsById.put(id, a);
        c.addAppointment(a);
        s.addAppointment(a);
        queues.get(specId).offer(a);
        return a;
    }

    // ---------- READ ----------
    public Appointment getAppointment(int id) { return find(appointmentsById, id); }   // O(1)
    public Client getClient(int id) { return find(clients, id); }

    public List<Specialist> bySpecialization(String spec) {
        return bySpecialization.getOrDefault(spec, List.of());
    }

    public List<Appointment> filter(Predicate<Appointment> criteria) {
        return appointments.stream().filter(criteria).collect(Collectors.toList());
    }

    public List<Appointment> sorted(Comparator<Appointment> cmp) {
        List<Appointment> copy = new ArrayList<>(appointments);
        copy.sort(cmp);   // null -> естественный порядок (Comparable)
        return copy;
    }

    public int queueSize(int specId) { return queues.get(specId).size(); }

    // ---------- UPDATE ----------
    public synchronized void renameClient(int id, String newName) { find(clients, id).setName(newName); }

    /** Роль специалиста: взять следующую заявку из очереди. */
    public synchronized Optional<Appointment> processNext(int specId) {
        find(specialists, specId);
        Appointment a = queues.get(specId).poll();
        if (a != null) a.setStatus(Appointment.Status.CONFIRMED);
        return Optional.ofNullable(a);
    }

    public synchronized void complete(int id) {
        Appointment a = find(appointmentsById, id);
        if (a.getStatus() != Appointment.Status.CONFIRMED)
            throw new IllegalStateException("Only CONFIRMED appointments can be completed");
        a.getPayment().pay();
        a.setStatus(Appointment.Status.COMPLETED);
    }

    public synchronized void cancel(int id) {
        Appointment a = find(appointmentsById, id);
        if (a.getStatus() == Appointment.Status.COMPLETED || a.getStatus() == Appointment.Status.CANCELLED)
            throw new IllegalStateException("Cannot cancel a " + a.getStatus() + " appointment");
        queues.get(a.getSpecialist().getId()).remove(a);
        a.setStatus(Appointment.Status.CANCELLED);
    }

    // ---------- DELETE (синхронно из всех коллекций) ----------
    public synchronized void delete(int id) {
        Appointment a = find(appointmentsById, id);
        appointments.remove(a);
        appointmentsById.remove(id);
        a.getClient().removeAppointment(a);
        a.getSpecialist().removeAppointment(a);
        queues.get(a.getSpecialist().getId()).remove(a);
    }

    // ---------- ANALYTICS ----------
    public void printReport() {
        System.out.println("=== REPORT ===");
        System.out.println("Total: " + appointments.size());
        System.out.println("By status: " + appointments.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, TreeMap::new, Collectors.counting())));
        System.out.println("Revenue: " + appointments.stream()
                .filter(a -> a.getStatus() == Appointment.Status.COMPLETED)
                .mapToDouble(Appointment::getPrice).sum());
        System.out.println("Specialists by specialization: " + bySpecialization);
    }

    // ---------- helpers ----------
    private <T> void put(Map<Integer, T> map, int id, T value) {
        if (map.putIfAbsent(id, value) != null) throw new IllegalStateException("Duplicate id: " + id);
    }

    private <T> T find(Map<Integer, T> map, int id) {
        T value = map.get(id);
        if (value == null) throw new NoSuchElementException("Not found: id=" + id);
        return value;
    }
}