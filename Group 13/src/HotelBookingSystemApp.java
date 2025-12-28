import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

class Room {
    private int roomNumber;
    private String type;
    private boolean available;
    private double price;

    public Room(int roomNumber, String type, double price) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.price = price;
        this.available = true;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        return "Room " + roomNumber + " (" + type + ") - " +
                (available ? "Available" : "Booked") +
                " - $" + price;
    }
}

class Booking {
    private String customerName;
    private Room room;
    private int nights;

    public Booking(String customerName, Room room, int nights) {
        this.customerName = customerName;
        this.room = room;
        this.nights = nights;
        this.room.setAvailable(false);
    }

    public String getCustomerName() {
        return customerName;
    }

    public Room getRoom() {
        return room;
    }

    public double calculateTotal() {
        return nights * room.getPrice();
    }

    @Override
    public String toString() {
        return "Booking: " + customerName +
                " -> Room " + room.getRoomNumber() +
                " for " + nights + " nights. Total: $" + calculateTotal();
    }
}

class HotelBookingSystem {
    private Room[] rooms;
    private Booking[] bookings;
    private int bookingCount;

    public HotelBookingSystem() {
        rooms = new Room[]{
                new Room(301, "Classic Single Room", 100),
                new Room(302, "Classic Double Room", 130),
                new Room(303, "Classic Suite", 200),
                new Room(401, "Semi-Luxury Single Room", 180),
                new Room(402, "Semi-Luxury Double Room", 210),
                new Room(403, "Semi-Luxury Suite", 280),
                new Room(501, "Luxury Single Room", 250),
                new Room(502, "Luxury Double Room", 280),
                new Room(503, "Luxury Suite", 350)
        };

        bookings = new Booking[100];
        bookingCount = 0;
    }

    public Room[] getRooms() {
        return rooms;
    }

    public Booking[] getBookings() {
        return bookings;
    }

    public int getBookingCount() {
        return bookingCount;
    }

    public Booking bookRoom(String customerName, int roomNumber, int nights) throws Exception {
        for (Room room : rooms) {
            if (room.getRoomNumber() == roomNumber) {
                if (!room.isAvailable()) {
                    throw new Exception("Room already booked!");
                }
                Booking booking = new Booking(customerName, room, nights);
                bookings[bookingCount++] = booking;
                return booking;
            }
        }
        throw new Exception("Room not found!");
    }

    public void cancelBooking(Booking booking) {
        booking.getRoom().setAvailable(true);
        for (int i = 0; i < bookingCount; i++) {
            if (bookings[i] == booking) {
                for (int j = i; j < bookingCount - 1; j++) {
                    bookings[j] = bookings[j + 1];
                }
                bookings[--bookingCount] = null;
                break;
            }
        }
    }

    public double calculateTotalRevenue() {
        double total = 0;
        for (int i = 0; i < bookingCount; i++) {
            total += bookings[i].calculateTotal();
        }
        return total;
    }
}

public class HotelBookingSystemApp extends JFrame {
    private HotelBookingSystem system;
    private JTextArea displayArea;
    private JTextField nameField, roomField, nightsField;

    public HotelBookingSystemApp(HotelBookingSystem system) {
        this.system = system;

        setTitle("Hotel Room Booking System");
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new GridLayout(9, 2, 5, 5));

        panel.add(new JLabel("Customer Name:"));
        nameField = new JTextField();
        panel.add(nameField);

        panel.add(new JLabel("Room Number:"));
        roomField = new JTextField();
        panel.add(roomField);

        panel.add(new JLabel("Nights:"));
        nightsField = new JTextField();
        panel.add(nightsField);

        JButton bookButton = new JButton("Book Room");
        JButton viewButton = new JButton("View Rooms");
        JButton bookingsButton = new JButton("View Bookings");
        JButton cancelButton = new JButton("Cancel Booking");
        JButton revenueButton = new JButton("View Total Revenue");
        JButton searchButton = new JButton("Search Booking");

        panel.add(bookButton);
        panel.add(viewButton);
        panel.add(bookingsButton);
        panel.add(cancelButton);
        panel.add(revenueButton);
        panel.add(searchButton);

        displayArea = new JTextArea();
        displayArea.setEditable(false);
        displayArea.setFont(new Font("Monospaced", Font.PLAIN, 14));

        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        bookButton.addActionListener(e -> {
            String customerName = nameField.getText().trim();
            try {
                int roomNumber = Integer.parseInt(roomField.getText().trim());
                int nights = Integer.parseInt(nightsField.getText().trim());
                Booking booking = system.bookRoom(customerName, roomNumber, nights);
                displayArea.setText("Booking successful!\n\n" + booking);

                nameField.setText("");
                roomField.setText("");
                nightsField.setText("");

            } catch (Exception ex) {
                displayArea.setText("Error: " + ex.getMessage());
            }
        });

        viewButton.addActionListener(_ -> showAvailableRooms());

        roomField.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showAvailableRooms();
            }
        });

        bookingsButton.addActionListener(e -> {
            StringBuilder sb = new StringBuilder("Current Bookings:\n\n");
            if (system.getBookingCount() == 0) {
                sb.append("No bookings yet.");
            } else {
                for (int i = 0; i < system.getBookingCount(); i++) {
                    sb.append(system.getBookings()[i]).append("\n");
                }
            }
            displayArea.setText(sb.toString());
        });

        cancelButton.addActionListener(e -> {
            String customerName = nameField.getText().trim();
            if (customerName.isEmpty()) {
                displayArea.setText("Please enter the customer name to cancel bookings.");
                return;
            }

            boolean found = false;
            StringBuilder sb = new StringBuilder("Booking(s) Cancelled Successfully!\n\n");
            double totalRefund = 0;

            for (int i = 0; i < system.getBookingCount(); i++) {
                Booking b = system.getBookings()[i];
                if (b.getCustomerName().equalsIgnoreCase(customerName)) {
                    system.cancelBooking(b);
                    found = true;
                    totalRefund += b.calculateTotal();
                    sb.append("Cancelled: ").append(b).append("\n\n");
                    i--;
                }
            }

            if (!found) {
                displayArea.setText("No bookings found for customer: " + customerName);
            } else {
                sb.append("Total Refunded: $").append(totalRefund);
                displayArea.setText(sb.toString());
            }
        });

        revenueButton.addActionListener(e -> {
            double revenue = system.calculateTotalRevenue();
            displayArea.setText("Total Hotel Revenue: $" + revenue);
        });

        searchButton.addActionListener(e -> {
            String customerName = nameField.getText().trim();
            StringBuilder sb = new StringBuilder("Search Results:\n\n");
            boolean found = false;

            if (customerName.isEmpty()) {
                displayArea.setText("Please enter a customer name to search.");
                return;
            }

            for (int i = 0; i < system.getBookingCount(); i++) {
                Booking b = system.getBookings()[i];
                if (b.getCustomerName().equalsIgnoreCase(customerName)) {
                    sb.append(b).append("\n");
                    found = true;
                }
            }

            if (!found) {
                sb.append("No bookings found for ").append(customerName);
            }

            displayArea.setText(sb.toString());
        });

        setVisible(true);
    }

    private void showAvailableRooms() {
        StringBuilder sb = new StringBuilder("Available Rooms:\n\n");
        for (Room room : system.getRooms()) {
            if (room.isAvailable()) {
                sb.append(room).append("\n");
            }
        }
        displayArea.setText(sb.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HotelBookingSystemApp(new HotelBookingSystem()));
    }
}
