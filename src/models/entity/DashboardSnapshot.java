package models.entity;

import java.util.ArrayList;
import java.util.List;

/** Data transfer object returned by the dashboard DAO. */
public class DashboardSnapshot {
    public int totalPatients;
    public int appointmentsToday;
    public int pendingAppointments;
    public double revenue;
    public final List<Object[]> patients = new ArrayList<>();
    public final List<Object[]> appointmentNotifications = new ArrayList<>();
    public final List<Object[]> paymentNotifications = new ArrayList<>();
    public final List<String> days = new ArrayList<>();
    public final List<Double> dailyRevenue = new ArrayList<>();
    public final List<String> services = new ArrayList<>();
    public final List<Double> serviceRevenue = new ArrayList<>();
}
