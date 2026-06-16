package com.salonflow.api.service;

import com.salonflow.api.entity.*;
import com.salonflow.api.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final CategoryRepository categoryRepository;
    private final SalonRepository salonRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public DatabaseSeeder(CategoryRepository categoryRepository,
                          SalonRepository salonRepository,
                          ServiceRepository serviceRepository,
                          ReviewRepository reviewRepository,
                          UserRepository userRepository,
                          BookingRepository bookingRepository) {
        this.categoryRepository = categoryRepository;
        this.salonRepository = salonRepository;
        this.serviceRepository = serviceRepository;
        this.reviewRepository = reviewRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Guard check to avoid duplicating data streams if database is already initialized
        if (categoryRepository.count() > 0) {
            log.info("[Seeder] Database already populated. Skipping seeding.");
            return;
        }

        log.info("[Seeder] Beginning fresh database seeding operation with weighted review distribution...");
        seedDemoUsers();

        String[] catEn = {"Barbershop", "Massage", "Pet Grooming", "Beauty Salon", "Nails Studio", "Spa & Wellness", "Miscellaneous"};
        String[] catRo = {"Frizerie", "Masaj", "Toaletaj Animale", "Salon Cosmetică", "Manichiură", "Spa & Relaxare", "Diverse"};
        String[] icons = {"scissors", "flower", "dog", "sparkles", "pen", "waves", "layout-template"};

        // Professional premium color scheme palette pattern for background variations
        String[] bgColors = {"1e293b", "0f172a", "1e1b4b", "111827", "0c4a6e", "064e3b", "3f3f46"};

        // Diversified pools for distinct Name/Surname mapping
        String[] firstNames = {"Alexandru", "Mihaela", "Andrei", "Sarah", "Taylor", "Emma", "David", "Ioana", "Oliver", "Sophia", "Matthew", "Daniel", "Raluca", "Cristian", "Olivia", "Bogdan", "Jessica", "James", "Elena", "Marius", "Radu", "Anca", "Cătălin", "Simona", "Vlad"};
        String[] lastNames = {"Popescu", "Ionescu", "Radu", "Dumitrescu", "Vasilescu", "Stan", "Stoica", "Gheorghe", "Matei", "Sandu", "Tudor", "Marcu", "Barbu", "Lazar", "Marin", "Dima", "Dinu", "Diaconescu", "Mocanu"};

        Random random = new Random();
        Salon targetSalonObj = null;
        SalonService targetServiceObj = null;

        List<Review> reviewsToSave = new ArrayList<>();
        List<SalonService> servicesToSave = new ArrayList<>();

        for (int i = 0; i < catEn.length; i++) {
            Category cat = new Category();
            cat.setNameEn(catEn[i]);
            cat.setNameRo(catRo[i]);
            cat.setIconClass(icons[i]);
            Category savedCat = categoryRepository.save(cat);

            // Cycle background color palette based on category index
            String currentBgColor = bgColors[i % bgColors.length];

            for (int j = 1; j <= 4; j++) {
                String styleName = j == 1 ? "Premium" : j == 2 ? "Elite" : j == 3 ? "Express" : "Cozy";
                String salonName = catEn[i] + " " + styleName;

                // Adjust recommended flag mapping to cleanly pop up within front-end configurations
                boolean isRec = (j == 1 && i < 5) || (styleName.equals("Cozy") && catEn[i].contains("Beauty")) || (styleName.equals("Cozy") && catEn[i].contains("Nails"));

                // Dynamic generation scale: Truly random scale bounds between 10 and 200 reviews per salon
                int numReviews = random.nextInt(191) + 10;
                int totalStars = 0;

                List<Review> tempReviews = new ArrayList<>();
                for (int r = 0; r < numReviews; r++) {
                    // Generate biased stars using Zipf's-law-like distribution
                    int stars = generateBiasedStars(random);
                    totalStars += stars;

                    Review review = new Review();

                    // Randomized structural Name & Surname combination injection
                    String randomClientName = firstNames[random.nextInt(firstNames.length)] + " " + lastNames[random.nextInt(lastNames.length)];
                    review.setName(randomClientName);
                    review.setStars(stars);
                    review.setText(getRandomReviewComment(stars, random)); // Populates detailed comments matching the score

                    // Random past date tracking generation stretching up to 3 years max back from today
                    int randomDaysBack = random.nextInt(3 * 365);
                    LocalDateTime randomPastDate = LocalDateTime.now().minusDays(randomDaysBack)
                            .withHour(random.nextInt(12) + 9)
                            .withMinute(random.nextInt(60));

                    // Fixed: Passing the native LocalDateTime directly rather than string mapping
                    review.setDate(randomPastDate);
                    tempReviews.add(review);
                }

                // Average rating computation rounded strictly to one decimal position
                double rating = (double) totalStars / numReviews;
                rating = Math.round(rating * 10.0) / 10.0;

                Salon salon = new Salon();
                salon.setName(salonName);
                salon.setCategoryId(savedCat.getId());

                // DYNAMIC PATTERN IMPLEMENTATION: Safe URL string compilation for placeholder graphics
                String encodedText = URLEncoder.encode(salonName, StandardCharsets.UTF_8);
                String dynamicPlaceholderUrl = "https://placehold.co/600x400/" + currentBgColor + "/ffffff?text=" + encodedText;
                salon.setImageUrl(dynamicPlaceholderUrl);

                salon.setRating(rating);
                salon.setDescriptionEn("Experience exquisite beauty and wellness styling in our comforting " + styleName + " atmosphere.");
                salon.setDescriptionRo("Parcurgeți servicii și tratamente extraordinare de înfrumusețare în ambientul " + styleName + ".");
                salon.setRecommended(isRec);
                salon.setReviewCount(numReviews);

                if (salonName.equals("Barbershop Premium") || salonName.equals("Massage Premium")) {
                    salon.setOwnerContact("salonowner@gmail.com");
                }

                Salon savedSalon = salonRepository.save(salon);

                for (Review rev : tempReviews) {
                    rev.setSalonId(savedSalon.getId());
                    reviewsToSave.add(rev);
                }

                SalonService s1 = new SalonService();
                s1.setNameEn(salonName + " Standard Option");
                s1.setNameRo("Tratament Standard " + salonName);
                s1.setPrice(60 + (j * 15));
                s1.setDurationMinutes(30);
                s1.setSalonId(savedSalon.getId());
                servicesToSave.add(s1);

                if (salonName.equals("Barbershop Premium")) {
                    targetSalonObj = savedSalon;
                    targetServiceObj = s1;
                }

                SalonService s2 = new SalonService();
                s2.setNameEn(salonName + " Executive Care");
                s2.setNameRo("Servicii Complete " + salonName);
                s2.setPrice(120 + (j * 20));
                s2.setDurationMinutes(60);
                s2.setSalonId(savedSalon.getId());
                servicesToSave.add(s2);
            }
        }

        // Optimized execution using bulk batch saving
        if (!reviewsToSave.isEmpty()) {
            reviewRepository.saveAll(reviewsToSave);
        }

        if (!servicesToSave.isEmpty()) {
            List<SalonService> savedServices = serviceRepository.saveAll(servicesToSave);
            if (targetServiceObj != null) {
                final String targetNameEn = targetServiceObj.getNameEn();
                targetServiceObj = savedServices.stream()
                        .filter(service -> service.getNameEn().equals(targetNameEn))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (targetSalonObj != null && targetServiceObj != null) {
            Booking booking = new Booking();
            booking.setSalonId(targetSalonObj.getId());
            booking.setServiceId(targetServiceObj.getId());

            // Fixed: Passing the native LocalDateTime directly rather than string mapping
            booking.setBookingDateTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
            booking.setUserContact("geicualexandru@gmail.com");
            bookingRepository.save(booking);
            log.info("[Seeder] Successfully bound dynamic booking to Salon ID: {}", targetSalonObj.getId());
        }

        log.info("[Seeder] Seeding process successfully completed.");
    }

    private void seedDemoUsers() {
        String demoEmail = "geicualexandru@gmail.com";
        if (userRepository.findByContactIgnoreCase(demoEmail).isEmpty()) {
            User demoUser = new User();
            demoUser.setContact(demoEmail);
            demoUser.setFirstName("Alexandru");
            demoUser.setLastName("Geicu");
            demoUser.setPhone("0722123456");
            demoUser.setBirthday("1994-08-15");
            demoUser.setPhotoUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?q=80&w=150&auto=format&fit=crop");
            userRepository.save(demoUser);
        }

        String partnerEmail = "salonowner@gmail.com";
        if (userRepository.findByContactIgnoreCase(partnerEmail).isEmpty()) {
            User partnerUser = new User();
            partnerUser.setContact(partnerEmail);
            partnerUser.setFirstName("Salon");
            partnerUser.setLastName("Owner");
            partnerUser.setPhone("0750783286");
            partnerUser.setBirthday("1988-12-01");
            partnerUser.setPhotoUrl("https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=150&auto=format&fit=crop");
            partnerUser.setPartnerApproved(true);
            partnerUser.setAppliedForPartner(true);
            userRepository.save(partnerUser);
        }
    }

    private int generateBiasedStars(Random random) {
        int roll = random.nextInt(100);
        if (roll < 55) {
            return 5;
        } else if (roll < 80) {
            return 4;
        } else if (roll < 90) {
            return 3;
        } else if (roll < 96) {
            return 2;
        } else {
            return 1;
        }
    }

    private String getRandomReviewComment(int stars, Random random) {
        String[] fiveStarComments = {
                "O experiență absolut minunată! Recomand cu drag salonul.",
                "Foarte mulțumit de rezultat! Servicii de înaltă calitate și atenție impecabilă.",
                "Cel mai bun salon la care am fost! Personal extrem de profesionist și drăguț.",
                "O experiență minunată de fiecare dată. Curățenie impecabilă și servicii premium.",
                "Profesioniști desăvârșiți! Serviciul a fost incredibil de rapid și calitativ.",
                "Atmosferă excelentă, atenție deosebită acordată clientului, recomand cu căldură!",
                "Recomand cu toată încrederea! Rezultatele au fost dincolo de așteptări."
        };

        String[] fourStarComments = {
                "Servicii bune, personal amabil. Voi reveni cu siguranță pe viitor.",
                "Totul a fost excelent, prețuri corecte pentru serviciile oferite.",
                "Salon foarte curat și atmosferă liniștită. O mică întârziere, dar serviciul a fost perfect.",
                "Am obținut exact ce mi-am dorit. Recomand cu drag.",
                "Foarte bine organizat, servicii rapide și personal foarte atent."
        };

        String[] threeStarComments = {
                "Servicii acceptabile, însă comunicarea personalului ar putea fi îmbunătățită.",
                "Atmosfera a fost plăcută, însă timpul de așteptare a fost cam lung.",
                "Un serviciu de nivel mediu, nimic spectaculos dar își face treaba.",
                "Rezultatul a fost în regulă, deși prețurile mi se par ușor exagerate."
        };

        String[] lowStarComments = {
                "Nu am fost mulțumit de experiența de astăzi.",
                "Personalul a fost destul de rece, iar rezultatul a lăsat de dorit.",
                "Nu recomand. Timpul de așteptare a fost mare, iar calitatea mediocră.",
                "O experiență destul de neplăcută. Nu cred că voi reveni."
        };

        if (stars == 5) {
            return fiveStarComments[random.nextInt(fiveStarComments.length)];
        } else if (stars == 4) {
            return fourStarComments[random.nextInt(fourStarComments.length)];
        } else if (stars == 3) {
            return threeStarComments[random.nextInt(threeStarComments.length)];
        } else {
            return lowStarComments[random.nextInt(lowStarComments.length)];
        }
    }
}