package com.ccinfoms17grp2.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing doctor availability schedules stored as JSON.
 * Supports weekly recurring format: {"day_of_week": "Monday", "start_time": "08:00", "end_time": "17:00"}
 */
public class AvailabilityParser {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Represents a single availability time slot for a specific day.
     */
    public static class AvailabilitySlot {
        private final DayOfWeek dayOfWeek;
        private final LocalTime startTime;
        private final LocalTime endTime;

        public AvailabilitySlot(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
            this.dayOfWeek = dayOfWeek;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public LocalTime getStartTime() {
            return startTime;
        }

        public LocalTime getEndTime() {
            return endTime;
        }

        /**
         * Check if a given datetime falls within this availability slot.
         */
        public boolean isAvailableAt(LocalDateTime datetime) {
            if (datetime.getDayOfWeek() != dayOfWeek) {
                return false;
            }
            LocalTime time = datetime.toLocalTime();
            return !time.isBefore(startTime) && !time.isAfter(endTime);
        }

        @Override
        public String toString() {
            return String.format("%s %s-%s", dayOfWeek, startTime.format(TIME_FORMATTER), endTime.format(TIME_FORMATTER));
        }
    }

    /**
     * Parse JSON availability string into a list of availability slots.
     *
     * @param jsonString JSON array string from database
     * @return List of availability slots
     */
    public static List<AvailabilitySlot> parseAvailability(String jsonString) {
        List<AvailabilitySlot> slots = new ArrayList<>();
        
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return slots;
        }

        try {
            JsonArray jsonArray = JsonParser.parseString(jsonString).getAsJsonArray();
            
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject obj = jsonArray.get(i).getAsJsonObject();
                
                String dayStr = obj.get("day_of_week").getAsString();
                String startStr = obj.get("start_time").getAsString();
                String endStr = obj.get("end_time").getAsString();
                
                DayOfWeek day = parseDayOfWeek(dayStr);
                LocalTime start = LocalTime.parse(startStr, TIME_FORMATTER);
                LocalTime end = LocalTime.parse(endStr, TIME_FORMATTER);
                
                slots.add(new AvailabilitySlot(day, start, end));
            }
        } catch (Exception e) {
            System.err.println("Error parsing availability JSON: " + e.getMessage());
        }
        
        return slots;
    }

    /**
     * Check if a doctor is available at a specific datetime.
     *
     * @param availabilityJson JSON availability string
     * @param datetime The datetime to check
     * @return true if available, false otherwise
     */
    public static boolean isAvailableAt(String availabilityJson, LocalDateTime datetime) {
        List<AvailabilitySlot> slots = parseAvailability(availabilityJson);
        return slots.stream().anyMatch(slot -> slot.isAvailableAt(datetime));
    }

    /**
     * Get all available time slots for a specific date.
     *
     * @param availabilityJson JSON availability string
     * @param date The date to check
     * @return List of time ranges available on that date
     */
    public static List<AvailabilitySlot> getAvailableSlotsForDate(String availabilityJson, LocalDate date) {
        List<AvailabilitySlot> allSlots = parseAvailability(availabilityJson);
        DayOfWeek targetDay = date.getDayOfWeek();
        
        List<AvailabilitySlot> daySlots = new ArrayList<>();
        for (AvailabilitySlot slot : allSlots) {
            if (slot.getDayOfWeek() == targetDay) {
                daySlots.add(slot);
            }
        }
        return daySlots;
    }

    private static DayOfWeek parseDayOfWeek(String dayStr) {
        switch (dayStr.toLowerCase()) {
            case "monday": return DayOfWeek.MONDAY;
            case "tuesday": return DayOfWeek.TUESDAY;
            case "wednesday": return DayOfWeek.WEDNESDAY;
            case "thursday": return DayOfWeek.THURSDAY;
            case "friday": return DayOfWeek.FRIDAY;
            case "saturday": return DayOfWeek.SATURDAY;
            case "sunday": return DayOfWeek.SUNDAY;
            default: throw new IllegalArgumentException("Invalid day of week: " + dayStr);
        }
    }
}
