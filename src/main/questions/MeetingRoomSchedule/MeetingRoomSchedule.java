// UML-style class structure for Meeting and Room Reservation System (with SOLID + Design Patterns)

import java.util.*;

// Strategy Pattern for availability logic
interface Schedulable {
    boolean isAvailable(TimeSlot slot);
}

// Observer Pattern for notification
interface Observer {
    void update(String message);
}

class User implements Observer {
    private int userId;
    private String name;
    private String email;
    private List<Meeting> meetings = new ArrayList<>();

    public void scheduleMeeting(Meeting meeting) {
        meetings.add(meeting);
    }

    public void cancelMeeting(Meeting meeting) {
        meetings.remove(meeting);
    }

    public List<Meeting> getMeetings() {
        return meetings;
    }

    @Override
    public void update(String message) {
        System.out.println("Email to " + name + " (" + email + "): " + message);
    }
}

class Room implements Schedulable {
    private int roomId;
    private String name;
    private int capacity;
    private Location location;
    private List<TimeSlot> bookings = new ArrayList<>();

    public boolean isAvailable(TimeSlot slot) {
        for (TimeSlot bookedSlot : bookings) {
            if (bookedSlot.overlaps(slot)) {
                return false;
            }
        }
        return true;
    }

    public int getCapacity() {
        return capacity;
    }

    public void bookRoom(TimeSlot slot) {
        bookings.add(slot);
    }
}

class Location {
    private String building;
    private String floor;
}

class TimeSlot {
    Date startTime;
    Date endTime;

    boolean overlaps(TimeSlot other) {
        return !(this.endTime.before(other.startTime) || this.startTime.after(other.endTime));
    }
}

class Meeting {
    int meetingId;
    String title;
    User organizer;
    List<User> attendees;
    TimeSlot timeSlot;
    Room room;
}

interface AvailabilityChecker {
    boolean isAvailable(User user, TimeSlot slot);
}

class CalendarService implements AvailabilityChecker {
    private Map<User, List<Meeting>> userMeetings = new HashMap<>();

    public boolean isAvailable(User user, TimeSlot slot) {
        List<Meeting> meetings = userMeetings.getOrDefault(user, new ArrayList<>());
        for (Meeting meeting : meetings) {
            if (meeting.timeSlot.overlaps(slot)) {
                return false;
            }
        }
        return true;
    }

    public void recordMeeting(User user, Meeting meeting) {
        userMeetings.putIfAbsent(user, new ArrayList<>());
        userMeetings.get(user).add(meeting);
    }
}

class RoomManager {
    private List<Room> rooms = new ArrayList<>();

    public void addRoom(Room room) {
        rooms.add(room);
    }

    public Room findAvailableRoom(TimeSlot slot, int requiredCapacity) {
        for (Room room : rooms) {
            if (room.getCapacity() >= requiredCapacity && room.isAvailable(slot)) {
                return room;
            }
        }
        return null;
    }
}

class MeetingScheduler {
    private RoomManager roomManager;
    private CalendarService calendarService;
    private NotificationService notificationService;

    public MeetingScheduler(RoomManager roomManager, CalendarService calendarService, NotificationService notificationService) {
        this.roomManager = roomManager;
        this.calendarService = calendarService;
        this.notificationService = notificationService;
    }

    public Meeting scheduleMeeting(User organizer, List<User> attendees, TimeSlot slot, String title) {
        int totalPeople = attendees.size() + 1;
        Room room = roomManager.findAvailableRoom(slot, totalPeople);
        if (room == null) return null;

        for (User user : attendees) {
            if (!calendarService.isAvailable(user, slot)) return null;
        }

        Meeting meeting = new Meeting();
        meeting.organizer = organizer;
        meeting.attendees = attendees;
        meeting.timeSlot = slot;
        meeting.room = room;
        meeting.title = title;

        // Register meeting
        organizer.scheduleMeeting(meeting);
        calendarService.recordMeeting(organizer, meeting);
        for (User user : attendees) {
            user.scheduleMeeting(meeting);
            calendarService.recordMeeting(user, meeting);
        }

        room.bookRoom(slot);

        // Notify participants
        List<Observer> observers = new ArrayList<>(attendees);
        observers.add(organizer);
        notificationService.notifyUsers(observers, "Meeting scheduled: " + title);

        return meeting;
    }
}

class NotificationService {
    public void notifyUsers(List<Observer> users, String message) {
        for (Observer user : users) {
            user.update(message);
        }
    }
}
