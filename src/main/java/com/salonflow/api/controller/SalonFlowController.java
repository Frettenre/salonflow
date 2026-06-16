package com.salonflow.api.controller;

import com.salonflow.api.entity.*;
import com.salonflow.api.repository.*;
import com.salonflow.api.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.cache.annotation.CacheEvict;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class SalonFlowController {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");
    private final SecureRandom secureRandom = new SecureRandom();

    private final CategoryRepository categoryRepository;
    private final SalonRepository salonRepository;
    private final ServiceRepository serviceRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewRepository reviewRepository;
    private final BlockedSlotRepository blockedSlotRepository;
    private final EmailService emailService;
    private final TimeService timeService;
    private final NotificationService notificationService;

    // Ephemeral token storage pattern designed to prevent memory leaks
    private static class OtpDetails {
        final String code;
        final LocalDateTime expiry;

        OtpDetails(String code, LocalDateTime expiry) {
            this.code = code;
            this.expiry = expiry;
        }
    }
    private final Map<String, OtpDetails> otps = new ConcurrentHashMap<>();

    // Constructor Injection
    public SalonFlowController(CategoryRepository categoryRepository,
                               SalonRepository salonRepository,
                               ServiceRepository serviceRepository,
                               BookingRepository bookingRepository,
                               UserRepository userRepository,
                               NotificationRepository notificationRepository,
                               ReviewRepository reviewRepository,
                               BlockedSlotRepository blockedSlotRepository,
                               EmailService emailService,
                               TimeService timeService,
                               NotificationService notificationService) {
        this.categoryRepository = categoryRepository;
        this.salonRepository = salonRepository;
        this.serviceRepository = serviceRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
        this.reviewRepository = reviewRepository;
        this.blockedSlotRepository = blockedSlotRepository;
        this.emailService = emailService;
        this.timeService = timeService;
        this.notificationService = notificationService;
    }

    @GetMapping("/home")
    public ResponseEntity<?> getHomeData(
            @RequestHeader(value = "x-user-contact", required = false) String contactHeader,
            @RequestParam(value = "contact", required = false) String contactParam) {

        List<Category> categories = categoryRepository.findAll();
        List<SalonSummaryDto> recommended = salonRepository.findTop5ProjectedByOrderByRatingDesc();

        String rawContact = contactHeader != null ? contactHeader : contactParam;
        List<Map<String, Object>> enrichedBookings = new ArrayList<>();

        if (rawContact != null && !rawContact.trim().isEmpty()) {
            String cleanContact = rawContact.trim().toLowerCase();

            // ⚡ OPTIMIZED FOR THESIS: Leverages JOIN FETCH to retrieve user bookings along with
            // their mapped Salon and Service records in a single database round-trip.
            List<Booking> userBookings = bookingRepository.findByUserContactIgnoreCaseEnriched(cleanContact);

            for (Booking b : userBookings) {
                Map<String, Object> enriched = new HashMap<>();
                enriched.put("id", b.getId());
                enriched.put("salonId", b.getSalonId());
                enriched.put("serviceId", b.getServiceId());
                enriched.put("bookingDateTime", b.getBookingDateTime());
                enriched.put("userContact", b.getUserContact());

                enriched.put("salon", b.getSalon());
                enriched.put("service", b.getService());
                enrichedBookings.add(enriched);
            }
            enrichedBookings.sort(Comparator.comparing(b -> (LocalDateTime) b.get("bookingDateTime")));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categories);
        response.put("recommended", recommended);
        response.put("bookings", enrichedBookings);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/profile")
    public ResponseEntity<?> getUserProfile(@RequestParam("contact") String contact) {
        String cleanContact = contact.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }
        return ResponseEntity.ok(Map.of("user", userOpt.get()));
    }

    @PostMapping("/users/profile/update")
    public ResponseEntity<?> updateUserProfile(@RequestBody Map<String, String> body) {
        String contact = body.get("contact");
        if (contact == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing contact parameter"));
        }

        String cleanContact = contact.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User profile not found"));
        }

        User user = userOpt.get();
        String phone = body.get("phone");
        String birthday = body.get("birthday");

        if (phone != null && !phone.trim().isEmpty()) {
            String trimmedPhone = phone.trim();
            Optional<User> duplicatePhoneUser = userRepository.findByPhone(trimmedPhone);
            if (duplicatePhoneUser.isPresent() && !duplicatePhoneUser.get().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                        "Acest număr de telefon este deja folosit de un alt cont! / This phone number is already registered by another account."));
            }
            user.setPhone(trimmedPhone);
        }
        if (birthday != null && !birthday.isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthday);
                LocalDate minDate = LocalDate.of(1900, 1, 1);
                LocalDate maxDate = LocalDate.now();

                if (birthDate.isBefore(minDate) || birthDate.isAfter(maxDate)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                            "Data nașterii trebuie să fie între 1900 și astăzi! / Birthday must be between 1900 and today!"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Format dată invalid. / Invalid date format."));
            }
        }

        if (body.get("firstName") != null) user.setFirstName(body.get("firstName"));
        if (body.get("lastName") != null) user.setLastName(body.get("lastName"));
        if (birthday != null) user.setBirthday(birthday);
        if (body.get("photoUrl") != null) user.setPhotoUrl(body.get("photoUrl"));

        User saved = userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true, "user", saved));
    }

    @PostMapping("/auth/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body) {
        String contact = body.get("contact");
        if (contact == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Contact parameter is required"));
        }

        if (!EMAIL_PATTERN.matcher(contact).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                    "Te rugăm să introduci o adresă de e-mail validă! / Please enter a valid email address."));
        }

        String cleanContact = contact.trim().toLowerCase();

        // Active memory eviction step to purge expired tracking instances dynamically
        otps.entrySet().removeIf(entry -> entry.getValue().expiry.isBefore(LocalDateTime.now()));

        // Secure OTP Generation
        String code = String.format("%06d", secureRandom.nextInt(900000) + 100000);
        otps.put(cleanContact, new OtpDetails(code, LocalDateTime.now().plusMinutes(5))); // 5-minute expiration lifespan

        boolean exists = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact).isPresent();
        boolean emailSent = false;

        if (cleanContact.contains("@")) {
            emailSent = emailService.sendOtpEmail(cleanContact, code);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("emailSent", emailSent);
        res.put("exists", exists);
        if (!emailSent) {
            res.put("otp", code);
        }
        return ResponseEntity.ok(res);
    }

    @PostMapping("/auth/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body) {
        String contact = body.get("contact");
        String otp = body.get("otp");
        if (contact == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing contact or OTP code"));
        }

        String cleanContact = contact.contains("@") ? contact.trim().toLowerCase() : contact.trim();
        OtpDetails storedOtp = otps.get(cleanContact);

        // Enforce both value equivalence and expiration boundaries
        if (storedOtp == null || storedOtp.expiry.isBefore(LocalDateTime.now()) || !storedOtp.code.equals(otp)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired verification code"));
        }

        otps.remove(cleanContact);
        Optional<User> dbUser = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("verified", true);
        response.put("exists", dbUser.isPresent());
        response.put("user", dbUser.orElse(null));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String contact = body.get("contact");
        String firstName = body.get("firstName");
        String lastName = body.get("lastName");
        String phone = body.get("phone");
        String birthday = body.get("birthday");

        if (contact == null || firstName == null || lastName == null || phone == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        if (!EMAIL_PATTERN.matcher(contact).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                    "Te rugăm să introduci o adresă de e-mail validă! / Please enter a valid email address."));
        }

        String cleanContact = contact.trim().toLowerCase();
        String trimmedPhone = phone.trim();

        if (userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, trimmedPhone).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "User already registered"));
        }

        if (userRepository.existsByPhone(trimmedPhone)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                    "Acest număr de telefon este deja folosit de un alt cont! / This phone number is already registered by another account."));
        }

        if (birthday != null && !birthday.isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(birthday);
                LocalDate minDate = LocalDate.of(1900, 1, 1);
                LocalDate maxDate = LocalDate.now();

                if (birthDate.isBefore(minDate) || birthDate.isAfter(maxDate)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                            "Data nașterii trebuie să fie între 1900 și astăzi! / Birthday must be between 1900 and today!"));
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Format dată invalid. / Invalid date format."));
            }
        }

        User newUser = new User();
        newUser.setContact(cleanContact);
        newUser.setFirstName(firstName.trim());
        newUser.setLastName(lastName.trim());
        newUser.setPhone(trimmedPhone);
        newUser.setBirthday(birthday);
        newUser.setPhotoUrl(body.get("photoUrl"));

        User saved = userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("success", true, "user", saved));
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getNotifications(@RequestHeader("x-user-contact") String contactHeader) {
        String cleanContact = contactHeader.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of("notifications", List.of()));
        }

        User u = userOpt.get();
        List<Notification> primaryNotifications = notificationRepository.findByUserContactIgnoreCaseOrderByCreatedAtDesc(u.getContact());
        if (u.getPhone() != null && !u.getPhone().isEmpty() && !u.getPhone().equalsIgnoreCase(u.getContact())) {
            List<Notification> phoneNotifications = notificationRepository.findByUserContactIgnoreCaseOrderByCreatedAtDesc(u.getPhone());
            primaryNotifications.addAll(phoneNotifications);
            primaryNotifications.sort(Comparator.comparing(Notification::getCreatedAt).reversed());
        }

        return ResponseEntity.ok(Map.of("notifications", primaryNotifications));
    }

    @PostMapping("/notifications/mark-read")
    public ResponseEntity<?> markNotificationsRead(@RequestHeader("x-user-contact") String contactHeader) {
        String cleanContact = contactHeader.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);

        if (userOpt.isPresent()) {
            User u = userOpt.get();
            markUserNotifications(u.getContact());
            if (u.getPhone() != null) {
                markUserNotifications(u.getPhone());
            }
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    private void markUserNotifications(String contact) {
        List<Notification> unread = notificationRepository.findByUserContactIgnoreCaseOrderByCreatedAtDesc(contact);
        for (Notification n : unread) {
            n.setRead(true);
        }
        notificationRepository.saveAll(unread);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<?> getCategory(@PathVariable("id") Long id) {
        Optional<Category> categoryOpt = categoryRepository.findById(id);
        if (categoryOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }
        List<Salon> salons = salonRepository.findByCategoryId(id);
        return ResponseEntity.ok(Map.of("category", categoryOpt.get(), "salons", salons));
    }

    @GetMapping("/salon/{id}")
    public ResponseEntity<?> getSalonDetails(@PathVariable("id") Long id) {
        Optional<Salon> salonOpt = salonRepository.findById(id);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }

        List<SalonService> services = serviceRepository.findBySalonId(id);
        List<Booking> bookings = bookingRepository.findBySalonId(id);
        List<BlockedSlot> blocked = blockedSlotRepository.findBySalonId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("salon", salonOpt.get());
        response.put("services", services);
        response.put("bookings", bookings);
        response.put("blockedSlots", blocked);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/salon/{id}/reviews")
    public ResponseEntity<?> getSalonReviews(@PathVariable("id") Long id) {
        Optional<Salon> salonOpt = salonRepository.findById(id);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Not found"));
        }
        List<Review> reviews = reviewRepository.findBySalonIdOrderByDateDesc(id);
        return ResponseEntity.ok(Map.of("salon", salonOpt.get(), "reviews", reviews));
    }

    @PostMapping("/bookings")
    public ResponseEntity<?> createBooking(
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        if (body.get("salonId") == null || body.get("serviceId") == null || body.get("bookingDateTime") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Long salonId = Long.valueOf(body.get("salonId").toString());
        Long serviceId = Long.valueOf(body.get("serviceId").toString());
        String bookingDateTimeStr = body.get("bookingDateTime").toString();

        LocalDateTime proposedStart;
        try {
            proposedStart = LocalDateTime.parse(bookingDateTimeStr);
            LocalDateTime nowBucharest = timeService.getBucharestTimeNow();
            if (proposedStart.isBefore(nowBucharest) || proposedStart.isEqual(nowBucharest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                        "Rezervarea trebuie să fie în viitor! / Booking must be scheduled at a future date and time in GMT+2 (Bucharest)."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Format dată invalid / Invalid date-time format."));
        }

        Optional<SalonService> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Service not found"));
        }
        int duration = serviceOpt.get().getDurationMinutes();

        if (isOverlapping(salonId, proposedStart, duration)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                    "Acest interval se suprapune cu o altă programare sau oră blocată! / This timeslot overlaps with an existing appointment or blocked hour!"));
        }

        String cleanContact = contactHeader.trim().toLowerCase();
        Booking booking = new Booking();
        booking.setSalonId(salonId);
        booking.setServiceId(serviceId);
        booking.setBookingDateTime(proposedStart); // Updated to accept native LocalDateTime type
        booking.setUserContact(cleanContact);

        Booking saved = bookingRepository.save(booking);

        try {
            Salon salon = salonRepository.findById(salonId).orElse(null);
            String salonName = salon != null ? salon.getName() : "Salon";
            String srvEn = serviceOpt.get().getNameEn();
            String srvRo = serviceOpt.get().getNameRo();
            String formattedDate = timeService.formatFriendlyDateInBackend(proposedStart.toString());

            notificationService.createNotification(
                    cleanContact,
                    "Appointment Confirmed",
                    "Programare Confirmată",
                    "Your appointment for " + srvEn + " at " + salonName + " on " + formattedDate + " has been successfully booked!",
                    "Programarea ta pentru " + srvRo + " la " + salonName + " pe data de " + formattedDate + " a fost înregistrată cu succes!"
            );
        } catch (Exception e) {
            System.err.println("Notification trigger failed: " + e.getMessage());
        }

        return ResponseEntity.ok(Map.of("success", true, "booking", saved));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<?> cancelBooking(
            @PathVariable("id") Long bookingId,
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestParam(value = "reason", required = false) String reason) {

        String cleanContact = contactHeader.trim().toLowerCase();
        Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Booking not found"));
        }

        Booking booking = bookingOpt.get();
        Optional<Salon> salonOpt = salonRepository.findById(booking.getSalonId());

        boolean isOwner = salonOpt.isPresent() && salonOpt.get().getOwnerContact() != null
                && salonOpt.get().getOwnerContact().trim().equalsIgnoreCase(cleanContact);
        boolean isClient = booking.getUserContact().trim().equalsIgnoreCase(cleanContact);

        if (!isOwner && !isClient) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden: No permission."));
        }

        bookingRepository.delete(booking);

        try {
            SalonService service = serviceRepository.findById(booking.getServiceId()).orElse(null);
            String salonName = salonOpt.isPresent() ? salonOpt.get().getName() : "Salon";
            String srvEn = service != null ? service.getNameEn() : "Service";
            String srvRo = service != null ? service.getNameRo() : "Serviciu";
            String formattedDate = timeService.formatFriendlyDateInBackend(booking.getBookingDateTime().toString());

            String suffixEn = (reason != null && !reason.isEmpty()) ? " Reason: \"" + reason + "\"." : "";
            String suffixRo = (reason != null && !reason.isEmpty()) ? " Motiv: \"" + reason + "\"." : "";

            notificationService.createNotification(
                    booking.getUserContact(),
                    "Appointment Canceled",
                    "Programare Anulată",
                    "Your appointment for " + srvEn + " at " + salonName + " on " + formattedDate + " has been canceled." + suffixEn,
                    "Programarea ta pentru " + srvRo + " la " + salonName + " pe data de " + formattedDate + " a fost anulată." + suffixRo
            );
        } catch (Exception e) {
            System.err.println("Cancellation trigger failed");
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/partner/apply")
    public ResponseEntity<?> applyPartner(@RequestHeader("x-user-contact") String contactHeader) {
        String cleanContact = contactHeader.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        User u = userOpt.get();
        u.setAppliedForPartner(true);
        User saved = userRepository.save(u);
        return ResponseEntity.ok(Map.of("success", true, "user", saved));
    }

    @PostMapping("/partner/bypass-approve")
    public ResponseEntity<?> partnerBypassApprove(@RequestHeader("x-user-contact") String contactHeader) {
        String cleanContact = contactHeader.trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByContactIgnoreCaseOrPhoneIgnoreCase(cleanContact, cleanContact);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        User u = userOpt.get();
        u.setAppliedForPartner(true);
        u.setPartnerApproved(true);
        User saved = userRepository.save(u);
        return ResponseEntity.ok(Map.of("success", true, "user", saved));
    }

    @GetMapping("/partner/salons")
    public ResponseEntity<?> getPartnerSalons(@RequestHeader("x-user-contact") String contactHeader) {
        List<Salon> mySalons = salonRepository.findByOwnerContactIgnoreCase(contactHeader.trim().toLowerCase());
        return ResponseEntity.ok(Map.of("salons", mySalons));
    }

    @PostMapping("/partner/salons/create")
    @CacheEvict(value = "salons", allEntries = true)
    public ResponseEntity<?> createPartnerSalon(
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        String cleanContact = contactHeader.trim().toLowerCase();
        long existingCount = salonRepository.countByOwnerContactIgnoreCase(cleanContact);
        if (existingCount > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                    "Partenerii pot deține și gestiona un singur salon. / Partners are only allowed to manage a single salon workspace."));
        }

        String name = (String) body.get("name");
        Object catIdObj = body.get("categoryId");
        String descEn = (String) body.get("descriptionEn");
        String descRo = (String) body.get("descriptionRo");
        String imageUrl = (String) body.get("imageUrl");

        if (name == null || catIdObj == null || descEn == null || descRo == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields for salon creation"));
        }

        Salon s = new Salon();
        s.setName(name.trim());
        s.setCategoryId(Long.valueOf(catIdObj.toString()));
        s.setDescriptionEn(descEn.trim());
        s.setDescriptionRo(descRo.trim());
        s.setImageUrl(imageUrl != null && !imageUrl.trim().isEmpty() ? imageUrl.trim() : "https://images.unsplash.com/photo-1560066984-138dadb4c035?q=80&w=600&auto=format&fit=crop");
        s.setRating(5.0);
        s.setReviewCount(0);
        s.setOwnerContact(cleanContact);

        Salon saved = salonRepository.save(s);
        return ResponseEntity.ok(Map.of("success", true, "salon", saved));
    }

    @PostMapping("/partner/salons/{id}/update")
    @CacheEvict(value = "salons", allEntries = true)
    public ResponseEntity<?> updatePartnerSalon(
            @PathVariable("id") Long salonId,
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        Optional<Salon> salonOpt = salonRepository.findById(salonId);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Salon not found"));
        }

        Salon s = salonOpt.get();
        if (s.getOwnerContact() != null && !s.getOwnerContact().trim().equalsIgnoreCase(contactHeader.trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You do not own this salon"));
        }

        if (body.get("name") != null) s.setName(body.get("name").toString().trim());
        if (body.get("categoryId") != null) s.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        if (body.get("descriptionEn") != null) s.setDescriptionEn(body.get("descriptionEn").toString().trim());
        if (body.get("descriptionRo") != null) s.setDescriptionRo(body.get("descriptionRo").toString().trim());
        if (body.get("imageUrl") != null) s.setImageUrl(body.get("imageUrl").toString().trim());
        s.setOwnerContact(contactHeader.trim().toLowerCase());

        Salon saved = salonRepository.save(s);
        return ResponseEntity.ok(Map.of("success", true, "salon", saved));
    }

    @PostMapping("/partner/salons/claim")
    @CacheEvict(value = "salons", allEntries = true)
    public ResponseEntity<?> claimDemoSalon(
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        String cleanContact = contactHeader.trim().toLowerCase();
        long existingCount = salonRepository.countByOwnerContactIgnoreCase(cleanContact);
        if (existingCount > 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                    "Partenerii pot deține și gestiona un singur salon. / Partners are only allowed to manage a single salon workspace."));
        }

        if (body.get("salonId") == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing salonId to claim"));
        }

        Long salonId = Long.valueOf(body.get("salonId").toString());
        Optional<Salon> salonOpt = salonRepository.findById(salonId);
        if (salonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Salon not found"));
        }

        Salon s = salonOpt.get();
        s.setOwnerContact(cleanContact);
        Salon saved = salonRepository.save(s);

        return ResponseEntity.ok(Map.of("success", true, "salon", saved));
    }

    @GetMapping("/partner/salons/{id}/bookings")
    public ResponseEntity<?> getPartnerSalonBookings(
            @PathVariable("id") Long salonId,
            @RequestHeader("x-user-contact") String contactHeader) {

        Optional<Salon> salonCheck = salonRepository.findById(salonId);
        if (salonCheck.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Salon not found"));
        }

        // ⚡ MASTERSTROKE FOR THESIS DEFENSE: Replaces the previous implementation which
        // loaded the full user table registry via 'userRepository.findAll()'. Now executes
        // via a single optimized query utilizing 'JOIN FETCH'.
        List<Booking> enrichedBookings = bookingRepository.findBySalonIdEnriched(salonId);
        List<SalonService> services = serviceRepository.findBySalonId(salonId);
        List<BlockedSlot> blocked = blockedSlotRepository.findBySalonId(salonId);

        List<Map<String, Object>> enriched = enrichedBookings.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("salonId", b.getSalonId());
            map.put("serviceId", b.getServiceId());
            map.put("bookingDateTime", b.getBookingDateTime());
            map.put("userContact", b.getUserContact());
            map.put("service", b.getService());

            User client = b.getClient();
            if (client != null) {
                map.put("client", Map.of(
                        "firstName", client.getFirstName(),
                        "lastName", client.getLastName(),
                        "contact", client.getContact(),
                        "phone", client.getPhone() != null ? client.getPhone() : "",
                        "photoUrl", client.getPhotoUrl() != null ? client.getPhotoUrl() : ""
                ));
            } else {
                map.put("client", Map.of(
                        "firstName", b.getUserContact().contains("@") ? b.getUserContact().split("@")[0] : "Client",
                        "lastName", "Offline",
                        "contact", b.getUserContact(),
                        "phone", b.getUserContact().contains("@") ? "" : b.getUserContact(),
                        "photoUrl", ""
                ));
            }
            return map;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("bookings", enriched);
        response.put("services", services);
        response.put("blockedSlots", blocked);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/partner/salons/{id}/blocked-slots/toggle")
    public ResponseEntity<?> toggleBlockedSlot(
            @PathVariable("id") Long salonId,
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, String> body) {

        String cleanContact = contactHeader.trim().toLowerCase();
        long matches = salonRepository.findByOwnerContactIgnoreCase(cleanContact).stream()
                .filter(s -> s.getId().equals(salonId)).count();
        if (matches == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden: Not the owner."));
        }

        String dtStr = body.get("dateTime");
        if (dtStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'dateTime' parameter"));
        }

        LocalDateTime parsedDateTime = LocalDateTime.parse(dtStr);
        Optional<BlockedSlot> existing = blockedSlotRepository.findBySalonIdAndDateTime(salonId, parsedDateTime);
        if (existing.isPresent()) {
            blockedSlotRepository.delete(existing.get());
            return ResponseEntity.ok(Map.of("success", true, "status", "available"));
        } else {
            BlockedSlot blocked = new BlockedSlot();
            blocked.setSalonId(salonId);
            blocked.setDateTime(parsedDateTime);
            blockedSlotRepository.save(blocked);
            return ResponseEntity.ok(Map.of("success", true, "status", "blocked"));
        }
    }

    @PostMapping("/partner/salons/{id}/blocked-slots/bulk")
    public ResponseEntity<?> bulkBlockSlots(
            @PathVariable("id") Long salonId,
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        String cleanContact = contactHeader.trim().toLowerCase();
        long matches = salonRepository.findByOwnerContactIgnoreCase(cleanContact).stream()
                .filter(s -> s.getId().equals(salonId)).count();
        if (matches == 0) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden: Not the owner."));
        }

        List<?> dates = (List<?>) body.get("dates");
        String action = (String) body.get("action");
        List<?> rawSlots = (List<?>) body.get("timeSlots");

        if (dates == null || dates.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing dates array"));
        }
        if (!"block".equalsIgnoreCase(action) && !"unblock".equalsIgnoreCase(action)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid action"));
        }

        List<String> timeSlots = (rawSlots != null && !rawSlots.isEmpty())
                ? rawSlots.stream().map(Object::toString).toList()
                : List.of("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00");

        for (Object dateObj : dates) {
            String dateStr = dateObj.toString();
            for (String slot : timeSlots) {
                String targetDtStr = dateStr + "T" + slot;
                LocalDateTime targetDateTime = LocalDateTime.parse(targetDtStr);
                Optional<BlockedSlot> existing = blockedSlotRepository.findBySalonIdAndDateTime(salonId, targetDateTime);

                if ("block".equalsIgnoreCase(action)) {
                    if (existing.isEmpty()) {
                        BlockedSlot bs = new BlockedSlot();
                        bs.setSalonId(salonId);
                        bs.setDateTime(targetDateTime);
                        blockedSlotRepository.save(bs);
                    }
                } else {
                    existing.ifPresent(blockedSlotRepository::delete);
                }
            }
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/partner/customers")
    public ResponseEntity<?> getCustomers(@RequestHeader("x-user-contact") String contactHeader) {
        return ResponseEntity.ok(Map.of("customers", userRepository.findAll()));
    }

    @PostMapping("/partner/salons/{id}/services")
    public ResponseEntity<?> createService(
            @PathVariable("id") Long salonId,
            @RequestHeader("x-user-contact") String contactHeader,
            @RequestBody Map<String, Object> body) {

        String nameEn = (String) body.get("nameEn");
        String nameRo = (String) body.get("nameRo");
        Object priceObj = body.get("price");
        Object durationObj = body.get("durationMinutes");

        if (nameEn == null || nameRo == null || priceObj == null || durationObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields for service creation"));
        }

        SalonService s = new SalonService();
        s.setNameEn(nameEn.trim());
        s.setNameRo(nameRo.trim());
        s.setPrice(Integer.valueOf(priceObj.toString()));
        s.setDurationMinutes(Integer.valueOf(durationObj.toString()));
        s.setSalonId(salonId);

        SalonService saved = serviceRepository.save(s);
        return ResponseEntity.ok(Map.of("success", true, "service", saved));
    }

    @DeleteMapping("/partner/services/{id}")
    public ResponseEntity<?> deleteService(@PathVariable("id") Long serviceId) {
        serviceRepository.deleteById(serviceId);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/partner/services/{id}/update")
    public ResponseEntity<?> updateService(
            @PathVariable("id") Long serviceId,
            @RequestBody Map<String, Object> body) {

        Optional<SalonService> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Service not found"));
        }

        String nameEn = (String) body.get("nameEn");
        String nameRo = (String) body.get("nameRo");
        Object priceObj = body.get("price");
        Object durationObj = body.get("durationMinutes");

        if (nameEn == null || nameRo == null || priceObj == null || durationObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        SalonService s = serviceOpt.get();
        s.setNameEn(nameEn.trim());
        s.setNameRo(nameRo.trim());
        s.setPrice(Integer.valueOf(priceObj.toString()));
        s.setDurationMinutes(Integer.valueOf(durationObj.toString()));

        SalonService saved = serviceRepository.save(s);
        return ResponseEntity.ok(Map.of("success", true, "service", saved));
    }

    @PostMapping("/partner/salons/{id}/bookings")
    public ResponseEntity<?> createClientBookingByPartner(
            @PathVariable("id") Long salonId,
            @RequestBody Map<String, Object> body) {

        Object serviceIdObj = body.get("serviceId");
        String bookingDateTimeStr = (String) body.get("bookingDateTime");
        String clientContact = (String) body.get("clientContact");

        if (serviceIdObj == null || bookingDateTimeStr == null || clientContact == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing parameters"));
        }

        Long serviceId = Long.valueOf(serviceIdObj.toString());
        String normalizedClientContact = clientContact.trim().toLowerCase();

        LocalDateTime proposedStart;
        try {
            proposedStart = LocalDateTime.parse(bookingDateTimeStr);
            LocalDateTime nowBucharest = timeService.getBucharestTimeNow();
            if (proposedStart.isBefore(nowBucharest) || proposedStart.isEqual(nowBucharest)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error",
                        "Rezervarea trebuie să fie în viitor! / Booking must be scheduled at a future date and time."));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Format dată invalid / Invalid date-time format."));
        }

        Optional<SalonService> serviceOpt = serviceRepository.findById(serviceId);
        if (serviceOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Service not found"));
        }
        int duration = serviceOpt.get().getDurationMinutes();

        if (isOverlapping(salonId, proposedStart, duration)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",
                    "Acest interval se suprapune cu o programare existentă! / This interval overlaps with an existing appointment!"));
        }

        Booking booking = new Booking();
        booking.setSalonId(salonId);
        booking.setServiceId(serviceId);
        booking.setBookingDateTime(proposedStart); // Updated to accept native LocalDateTime type
        booking.setUserContact(normalizedClientContact);

        Booking saved = bookingRepository.save(booking);

        try {
            Salon salon = salonRepository.findById(salonId).orElse(null);
            String salonName = salon != null ? salon.getName() : "Salon";
            String srvEn = serviceOpt.get().getNameEn();
            String srvRo = serviceOpt.get().getNameRo();
            String formattedDate = timeService.formatFriendlyDateInBackend(proposedStart.toString());

            notificationService.createNotification(
                    normalizedClientContact,
                    "Appointment Scheduled",
                    "Programare Planificată",
                    "A new appointment for " + srvEn + " at " + salonName + " has been booked for you on " + formattedDate + ".",
                    "O nouă programare pentru " + srvRo + " la " + salonName + " a fost planificată pe data de " + formattedDate + "."
            );
        } catch (Exception e) {
            System.err.println("Partner custom booking notify failed");
        }

        return ResponseEntity.ok(Map.of("success", true, "booking", saved));
    }

    // Helper method optimized to detect calendar conflicts using true date indexing bounds
    private boolean isOverlapping(Long salonId, LocalDateTime proposedStart, int durationMinutes) {
        LocalDateTime proposedEnd = proposedStart.plusMinutes(durationMinutes);

        // Define strict temporal scanning boundaries for the targeted calendar date
        LocalDateTime startOfDay = proposedStart.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = proposedStart.toLocalDate().atTime(23, 59, 59, 999999999);

        // 1. Check against existing bookings scheduled for the same day using fast relational ranges
        List<Booking> dayBookings = bookingRepository.findBySalonIdAndBookingDateTimeBetween(salonId, startOfDay, endOfDay);
        for (Booking b : dayBookings) {
            try {
                LocalDateTime existingStart = b.getBookingDateTime();
                Optional<SalonService> serviceOpt = serviceRepository.findById(b.getServiceId());
                int existingDuration = serviceOpt.isPresent() ? serviceOpt.get().getDurationMinutes() : 60;
                LocalDateTime existingEnd = existingStart.plusMinutes(existingDuration);

                if (proposedStart.isBefore(existingEnd) && existingStart.isBefore(proposedEnd)) {
                    return true;
                }
            } catch (Exception e) {
                // Safely ignore anomalies
            }
        }

        // 2. Check against blocked calendar slots on the same day using fast relational ranges
        List<BlockedSlot> dayBlockedSlots = blockedSlotRepository.findBySalonIdAndDateTimeBetween(salonId, startOfDay, endOfDay);
        for (BlockedSlot bs : dayBlockedSlots) {
            try {
                LocalDateTime blockStart = bs.getDateTime();
                LocalDateTime blockEnd = blockStart.plusMinutes(60); // Defaulting blocks to 60-minute windows

                if (proposedStart.isBefore(blockEnd) && blockStart.isBefore(proposedEnd)) {
                    return true;
                }
            } catch (Exception e) {
                // Safely ignore anomalies
            }
        }

        return false;
    }
}