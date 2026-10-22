package com.returnhub.config;

import com.returnhub.model.*;
import com.returnhub.repository.ClaimRepository;
import com.returnhub.repository.ItemRepository;
import com.returnhub.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ItemRepository itemRepository,
            ClaimRepository claimRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
        this.claimRepository = claimRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        // Demo User 1: Alice Smith
        User alice = new User("Alice Smith", "alice@example.com", passwordEncoder.encode("password123"), "+1 (555) 234-5678");
        userRepository.save(alice);

        // Demo User 2: Bob Johnson
        User bob = new User("Bob Johnson", "bob@example.com", passwordEncoder.encode("password123"), "+1 (555) 876-5432");
        userRepository.save(bob);

        // Item 1: Lost iPhone (by Alice)
        Item item1 = new Item();
        item1.setTitle("Apple iPhone 14 Pro - Space Black");
        item1.setDescription("Lost my iPhone 14 Pro with a matte black back and navy blue silicone case. Lock screen shows a landscape wallpaper of snowy mountains.");
        item1.setCategory(Category.ELECTRONICS);
        item1.setType(ItemType.LOST);
        item1.setLocation("University Central Library, 2nd floor silent study area");
        item1.setEventDate(LocalDate.now().minusDays(3));
        item1.setContactPhone("+1 (555) 234-5678");
        item1.setContactEmail("alice@example.com");
        item1.setAdditionalContactInfo("Available by phone or WhatsApp after 4 PM");
        item1.setStatus(ItemStatus.OPEN);
        item1.setUser(alice);
        itemRepository.save(item1);

        // Item 2: Lost Wallet (by Bob)
        Item item2 = new Item();
        item2.setTitle("Brown Leather Fossil Bifold Wallet");
        item2.setDescription("Contains metro transit card, student identification, and gym membership. No large amounts of cash, but the ID cards are very important.");
        item2.setCategory(Category.WALLETS_BAGS);
        item2.setType(ItemType.LOST);
        item2.setLocation("City Subway Station - Platform 3 Southbound");
        item2.setEventDate(LocalDate.now().minusDays(5));
        item2.setContactPhone("+1 (555) 876-5432");
        item2.setContactEmail("bob@example.com");
        item2.setAdditionalContactInfo("Please leave a text message or voicemail");
        item2.setStatus(ItemStatus.OPEN);
        item2.setUser(bob);
        itemRepository.save(item2);

        // Item 3: Found Sony Headphones (found by Bob)
        Item item3 = new Item();
        item3.setTitle("Sony WH-1000XM4 Noise Canceling Headphones");
        item3.setDescription("Found on a cafe table inside a black zippered protective case. In clean condition with charging cable inside.");
        item3.setCategory(Category.ELECTRONICS);
        item3.setType(ItemType.FOUND);
        item3.setLocation("Downtown Coffee Lab, 5th Avenue near Market St");
        item3.setEventDate(LocalDate.now().minusDays(2));
        item3.setContactPhone("+1 (555) 876-5432");
        item3.setContactEmail("bob@example.com");
        item3.setAdditionalContactInfo("Can meet near Downtown Coffee Lab or City Hall");
        item3.setStatus(ItemStatus.CLAIM_PENDING);
        item3.setUser(bob);
        itemRepository.save(item3);

        // Item 4: Found Set of Keys (found by Alice)
        Item item4 = new Item();
        item4.setTitle("Keychain with Toyota Fob and 3 Brass Keys");
        item4.setDescription("Found on a park bench along the running path. Has a blue metallic carabiner and a small red Swiss army pocketknife attached.");
        item4.setCategory(Category.KEYS);
        item4.setType(ItemType.FOUND);
        item4.setLocation("Riverside Park, bench near the 79th Street boat basin");
        item4.setEventDate(LocalDate.now().minusDays(1));
        item4.setContactPhone("+1 (555) 234-5678");
        item4.setContactEmail("alice@example.com");
        item4.setStatus(ItemStatus.OPEN);
        item4.setUser(alice);
        itemRepository.save(item4);

        // Claim on item3 by Alice (Claim Pending)
        Claim claim = new Claim();
        claim.setItem(item3);
        claim.setClaimant(alice);
        claim.setDateLost(LocalDate.now().minusDays(2));
        claim.setProofDescription("The headphones have a tiny scratch on the right hinge near the NFC logo. Inside the mesh pocket of the case is a gold-plated 3.5mm airplane audio adapter.");
        claim.setContactPhone("+1 (555) 234-5678");
        claim.setContactEmail("alice@example.com");
        claim.setMessage("Thank you so much for picking these up! I left them right by the window counter when rushing to catch the bus.");
        claim.setStatus(ClaimStatus.PENDING);
        claimRepository.save(claim);
    }
}
