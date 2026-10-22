# Return Hub — Lost and Found Web Application

**Return Hub** is a modern, full-stack Lost and Found web application built with **Java 21**, **Spring Boot**, **Thymeleaf**, **Spring Data JPA**, **MySQL**, **Bean Validation**, **Spring Security**, **HTML5**, **CSS3**, and **JavaScript**.

It features a clean, responsive **blue-and-white** user interface designed to help communities safely reunite with lost belongings while **protecting user privacy** and shielding personal contact details from public exposure.

---

## 🌟 Key Features

1. **Modern, Responsive Blue-and-White Design**:
   - Clean, accessible typography using Google Inter font.
   - Polished cards, badge pills, and quick action banners.
   - Fully responsive on mobile, tablet, and desktop screens with a hamburger navigation toggle.

2. **Homepage**:
   - Live statistics counter (Total reports, Lost items, Found items, Successfully reunited items).
   - Instant search bar with filters for keywords, item types, and categories.
   - Recently reported lost items and recently reported found items.
   - "How Return Hub Works" 3-step safety guide.

3. **Item Directories & Search**:
   - **Lost Items Directory** (`/lost-items`): Filter missing belongings by category, status, and keyword.
   - **Found Items Directory** (`/found-items`): Browse found items ready to be claimed.
   - **Keyword & Category Search** (`/search`): Instant search across title, description, and location.

4. **Reporting Lost & Found Items**:
   - Dedicated forms for **Report Lost Item** (`/items/report/lost`) and **Report Found Item** (`/items/report/found`).
   - Server-side **Bean Validation** with clear user-friendly error messages.
   - Optional item picture upload (supports JPG, PNG, WEBP with local file storage) or external image URL.
   - Date picker with past/present validation.

5. **🛡️ Strict Contact Information Privacy**:
   - **Zero Public Exposure**: Reporter phone numbers and email addresses are **never** displayed on public item listings.
   - **Privacy Shield**: Public viewers only see item details, photos, and approximate locations.
   - Private contact details are unlocked **only** when a submitted ownership claim is verified and approved by the item reporter.

6. **Claim Item Feature**:
   - Users can file a formal claim on any found item (`/items/{id}/claim`).
   - Claimants must provide proof of ownership (e.g. unique scratches, serial numbers, password patterns, or bag contents).
   - Item status automatically transitions to `CLAIM_PENDING`.

7. **My Reports & Claims Dashboard (`/my-reports`)**:
   - **Items I Reported**: View all items you posted. See received claims and review proof with one-click **Approve** or **Reject** actions.
   - **Claims I Submitted**: Track your filed claims. When your claim is approved, the reporter's contact details are revealed so you can arrange safe pickup!

8. **User Authentication & Security**:
   - Spring Security form login (`/login`) and custom registration (`/register`).
   - Strong password hashing with **BCrypt**.
   - CSRF protection and role-based access control.

---

## 🛠️ Technology Stack

| Component | Technology |
|---|---|
| **Language** | Java 21 LTS |
| **Framework** | Spring Boot (Spring Web MVC, Spring Data JPA, Spring Security, Validation) |
| **Template Engine** | Thymeleaf + `thymeleaf-extras-springsecurity6` |
| **Database** | MySQL (with H2 in-memory support for zero-config automated testing) |
| **ORM** | Hibernate / Jakarta Persistence (JPA) |
| **Frontend** | HTML5, Modern CSS3 (Custom Properties, Flexbox, Grid), Vanilla JavaScript |
| **Icons & Fonts** | FontAwesome 6, Google Fonts (Inter) |
| **Build Tool** | Apache Maven (Maven Wrapper included) |

---

## 📋 Prerequisites

Before running the application, make sure you have:
1. **Java 21 JDK** or newer installed. Check with:
   ```bash
   java -version
   ```
2. **MySQL Server** (version 8.0 or newer) installed and running.

---

## ⚙️ Database Configuration

### Step 1: Create the MySQL Database
Log into your MySQL command-line client or MySQL Workbench and run:

```sql
CREATE DATABASE IF NOT EXISTS returnhub_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

*(Optional) Create a dedicated user for Return Hub:*
```sql
CREATE USER 'returnhub_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON returnhub_db.* TO 'returnhub_user'@'localhost';
FLUSH PRIVILEGES;
```

### Step 2: Configure Environment Variables
You can configure database credentials using standard environment variables:

#### On Windows (PowerShell):
```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/returnhub_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

#### On Linux / macOS (Bash / Zsh):
```bash
export DB_URL="jdbc:mysql://localhost:3306/returnhub_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
export DB_USERNAME="root"
export DB_PASSWORD="your_mysql_password"
```

Alternatively, you can edit `src/main/resources/application.properties` directly, or copy `application.properties.example` to `application.properties`.

---

## 🚀 How to Run the Application

### 1. Build and Run using the Maven Wrapper

### 1. Build and Run the Application

#### Option A: Quick 1-Click Launch (Windows)
Double-click [`run.bat`](file:///c:/Users/akhil/Downloads/return-hub/run.bat) in the project directory, or run in Command Prompt:
```cmd
run.bat
```
*(Automatically sets JDK 21, configures the environment, and starts Spring Boot!)*

#### Option B: PowerShell
```powershell
.\run.ps1
```
or
```powershell
.\mvnw.cmd spring-boot:run
```

#### Option C: Linux / macOS / Git Bash
```bash
./mvnw spring-boot:run
```

### 2. Access the Application
Open your web browser and navigate to:
```
http://localhost:8080
```

---

## 🧪 Running Automated Tests

Return Hub includes automated unit tests, slice tests, and web integration tests (MockMvc). The test suite automatically uses an isolated **H2 in-memory database** (`src/test/resources/application.properties`), meaning **no active MySQL database is required to run the test suite**.

Execute the tests with:

```cmd
test.bat
```
or
```powershell
.\mvnw.cmd test
```

### Verified Test Suite:
- `UserServiceTest`: Registration validation, BCrypt password hashing, duplicate email rejection.
- `ItemServiceTest`: Item creation, searching with multi-field filters, ownership authorization checks.
- `ClaimServiceTest`: Claim submission, status state transitions, and the **Privacy Guard** verification.
- `ItemWebIntegrationTest`: MockMvc integration tests verifying homepage rendering, public directories, login redirection, registration flows, and **HTML contact privacy shielding**.

---

## 👥 Demo Accounts (Pre-loaded Seed Data)

For quick demonstration and testing, the application initializes demo data on its first run:

| Full Name | Email Address | Password | Role | Description |
|---|---|---|---|---|
| **Alice Smith** | `alice@example.com` | `password123` | User | Reported a lost iPhone and found a set of keys. Submitted a claim on Bob's found headphones. |
| **Bob Johnson** | `bob@example.com` | `password123` | User | Reported a lost wallet and found Sony headphones. Has a pending claim from Alice to review. |

You can also register any new account on the `/register` page!

---

## 📁 Project Directory Structure

```
return-hub/
├── pom.xml                               # Maven project dependencies & plugins
├── application.properties.example        # Example database configuration without credentials
├── README.md                             # Exact setup and walkthrough instructions
├── src/
│   ├── main/
│   │   ├── java/com/returnhub/
│   │   │   ├── ReturnHubApplication.java # Spring Boot entry point
│   │   │   ├── config/                   # WebMvc & DataInitializer beans
│   │   │   ├── controller/               # Spring MVC Controllers (Home, Auth, Item, Claim, Report)
│   │   │   ├── dto/                      # Validation DTOs (UserRegistration, ItemReport, Claim)
│   │   │   ├── model/                    # JPA Entities (User, Item, Claim) & Enums (Category, Status, Type)
│   │   │   ├── repository/               # Spring Data JPA Repositories
│   │   │   ├── security/                 # Spring Security Configuration & CustomUserDetailsService
│   │   │   └── service/                  # Business logic (UserService, ItemService, ClaimService, FileStorage)
│   │   └── resources/
│   │       ├── application.properties    # MySQL database config driven by environment variables
│   │       ├── static/
│   │       │   ├── css/style.css         # Modern blue-and-white custom styling
│   │       │   └── js/main.js            # Responsive navbar toggle & UI interactions
│   │       └── templates/
│   │           ├── home.html             # Landing page with hero search & stats
│   │           ├── login.html            # Authentication login page
│   │           ├── register.html         # User registration form
│   │           ├── error.html            # User-friendly 404/500 error page
│   │           ├── fragments/layout.html # Reusable header, navbar, alerts, footer
│   │           ├── items/
│   │           │   ├── list.html         # Lost & Found catalog with filter bar
│   │           │   ├── details.html      # Item details with Privacy Guard
│   │           │   ├── report.html       # Report Lost / Found / Edit item form
│   │           │   └── claim.html        # Ownership claim submission form
│   │           └── reports/
│   │               └── my-reports.html   # User dashboard for reports & received claims
│   └── test/
│       ├── java/com/returnhub/           # Unit and Integration test classes
│       └── resources/
│           └── application.properties    # H2 in-memory test configuration
```

---

## 🔒 Privacy & Safety Implementation Details

- **Contact Phone & Email Privacy**:
  - The `contactPhone` and `contactEmail` fields stored in MySQL are **never** rendered on public item pages.
  - In `ItemController` and `ClaimService.canViewPrivateContact(item, currentUser)`, access to contact info is strictly permitted only if:
    1. The viewer is the owner who reported the item, OR
    2. The viewer has an `APPROVED` claim for that item, OR
    3. The viewer is an administrator.
  - All other visitors see a reassuring **Privacy Shield Box** prompting them to file a verified ownership claim.
- **Ownership Verification**:
  - The reporter reviews the claimant's proof of ownership privately.
  - Once verified, the reporter clicks **Approve Claim**. The item status transitions to `RESOLVED`, other pending claims are closed, and mutual contact details are made available to both parties for handoff.

---

## 📜 License
This project is open-source and available under the standard MIT License.
