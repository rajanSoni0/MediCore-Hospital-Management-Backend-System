# 🏥 Hospital Management System

A scalable, production-ready backend built with **Spring Boot 3**, secured with **Spring Security + JWT**, and built with **Gradle (Kotlin DSL)**.

---

## 📁 Project Structure

```
hospital-management-system/
├── build.gradle.kts                             ← Gradle build (Kotlin DSL)
├── settings.gradle.kts                          ← Project name
├── gradlew / gradlew.bat                        ← Gradle wrapper scripts
├── gradle/wrapper/
│   ├── gradle-wrapper.jar
│   └── gradle-wrapper.properties                ← Gradle 8.7
└── src/
    ├── main/
    │   ├── java/com/hospital/
    │   │   ├── HospitalManagementApplication.java
    │   │   ├── auth/
    │   │   │   ├── AuthController.java
    │   │   │   ├── AuthDTO.java
    │   │   │   └── AuthService.java
    │   │   ├── config/
    │   │   │   ├── SecurityConfig.java
    │   │   │   └── WebhookConfig.java
    │   │   ├── controller/
    │   │   │   ├── AdminController.java
    │   │   │   ├── AppointmentController.java
    │   │   │   ├── DoctorController.java
    │   │   │   └── PatientController.java
    │   │   ├── dto/
    │   │   │   ├── AppointmentDTO.java
    │   │   │   ├── DoctorDTO.java
    │   │   │   └── PatientDTO.java
    │   │   ├── exception/
    │   │   │   ├── BadRequestException.java
    │   │   │   ├── DuplicateResourceException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ResourceNotFoundException.java
    │   │   ├── model/
    │   │   │   ├── Appointment.java
    │   │   │   ├── Doctor.java
    │   │   │   ├── Patient.java
    │   │   │   ├── Role.java
    │   │   │   └── User.java
    │   │   ├── repository/
    │   │   │   ├── AppointmentRepository.java
    │   │   │   ├── DoctorRepository.java
    │   │   │   ├── PatientRepository.java
    │   │   │   └── UserRepository.java
    │   │   ├── security/
    │   │   │   ├── CustomAccessDeniedHandler.java
    │   │   │   ├── CustomUserDetailsService.java
    │   │   │   ├── JwtAuthEntryPoint.java
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   └── JwtUtil.java
    │   │   └── service/
    │   │       ├── AppointmentService.java
    │   │       ├── DoctorService.java
    │   │       ├── PatientService.java
    │   │       └── WebhookService.java
    │   └── resources/
    │       ├── application.properties
    │       └── log4j2.xml
    └── test/
        ├── java/com/hospital/
        │   └── HospitalManagementApplicationTests.java
        └── resources/
            └── application-test.properties
```

---

## ⚙️ Tech Stack

| Technology              | Purpose                               |
|-------------------------|---------------------------------------|
| Java 17                 | Language                              |
| Spring Boot 3.2.4       | Framework                             |
| Gradle 8.7 (Kotlin DSL) | Build system                          |
| Spring Data JPA         | Database abstraction (JpaRepository)  |
| Spring Security 6       | Authentication & Authorization        |
| JJWT 0.12.5             | JWT generation & validation           |
| BCrypt (cost 12)        | Password hashing                      |
| MySQL 8                 | Production database                   |
| H2                      | In-memory DB for tests                |
| Log4j2                  | Structured logging                    |
| Lombok                  | Boilerplate reduction                 |

---

## 🚀 Setup & Run

### Prerequisites
- Java 17+
- MySQL 8 running locally
- No Gradle installation needed — use the included `./gradlew` wrapper

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/your-username/hospital-management-system.git
cd hospital-management-system

# 2. Configure MySQL credentials
#    Edit: src/main/resources/application.properties
#    Change: spring.datasource.username and spring.datasource.password

# 3. Build
./gradlew build

# 4. Run
./gradlew bootRun

# App starts at http://localhost:8080
```

### Windows

```cmd
gradlew.bat bootRun
```

### Other useful Gradle tasks

```bash
./gradlew test              # Run tests only
./gradlew bootJar           # Build executable JAR → build/libs/hospital-management-system.jar
./gradlew dependencies      # Print dependency tree
./gradlew clean build       # Full clean rebuild
java -jar build/libs/hospital-management-system.jar  # Run the JAR directly
```

---

## 🔐 Authentication — JWT Flow

```
1. POST /api/auth/register  { fullName, email, password, role }
   → Password BCrypt-hashed → User saved → JWT returned

2. POST /api/auth/login     { email, password }
   → Spring Security validates credentials → JWT returned

3. Any secured endpoint:
   Header: Authorization: Bearer <token>
   → JwtAuthFilter validates token → sets SecurityContext
   → SecurityConfig role rules applied → request granted or 403
```

---

## 🔌 API Reference

### Auth (public — no token needed)

| Method | Endpoint               | Description              |
|--------|------------------------|--------------------------|
| POST   | `/api/auth/register`   | Create account + get JWT |
| POST   | `/api/auth/login`      | Login + get JWT          |
| GET    | `/api/auth/me`         | Get current user info    |

### Patients — `/api/patients`

| Method | Endpoint                       | Role Required      |
|--------|--------------------------------|--------------------|
| POST   | `/api/patients`                | ADMIN              |
| GET    | `/api/patients`                | ADMIN, DOCTOR      |
| GET    | `/api/patients?search=rajan`   | ADMIN, DOCTOR      |
| GET    | `/api/patients/{id}`           | ADMIN, DOCTOR      |
| PUT    | `/api/patients/{id}`           | ADMIN              |
| PATCH  | `/api/patients/{id}/status`    | ADMIN              |
| DELETE | `/api/patients/{id}`           | ADMIN              |

### Doctors — `/api/doctors`

| Method | Endpoint                           | Role Required      |
|--------|------------------------------------|--------------------|
| POST   | `/api/doctors`                     | ADMIN              |
| GET    | `/api/doctors`                     | Any authenticated  |
| GET    | `/api/doctors?available=true`      | Any authenticated  |
| PUT    | `/api/doctors/{id}`                | ADMIN              |
| PATCH  | `/api/doctors/{id}/status`         | ADMIN              |
| DELETE | `/api/doctors/{id}`                | ADMIN              |

### Appointments — `/api/appointments`

| Method | Endpoint                                 | Role Required             |
|--------|------------------------------------------|---------------------------|
| POST   | `/api/appointments`                      | ADMIN, DOCTOR, PATIENT    |
| GET    | `/api/appointments`                      | ADMIN, DOCTOR, PATIENT    |
| GET    | `/api/appointments?patientId=1`          | ADMIN, DOCTOR, PATIENT    |
| PATCH  | `/api/appointments/{id}/status`          | ADMIN, DOCTOR             |
| PATCH  | `/api/appointments/{id}/reschedule`      | ADMIN, DOCTOR             |
| DELETE | `/api/appointments/{id}`                 | ADMIN                     |

### Admin — `/api/admin`

| Method | Endpoint              | Role Required |
|--------|-----------------------|---------------|
| GET    | `/api/admin/users`    | ADMIN         |
| DELETE | `/api/admin/users/{id}` | ADMIN       |

---

## 📦 Postman Examples

**Register**
```json
POST /api/auth/register
{
  "fullName": "Rajan Sharma",
  "email": "rajan@hospital.com",
  "password": "secret123",
  "role": "ADMIN"
}
```

**Login**
```json
POST /api/auth/login
{ "email": "rajan@hospital.com", "password": "secret123" }
```
→ Response: `{ "token": "eyJ...", "tokenType": "Bearer", "role": "ADMIN" }`

**Use token on any secured endpoint**
```
GET /api/patients
Authorization: Bearer eyJ...
```

---

## 📋 Logging (Log4j2)

| File                      | Contains                    |
|---------------------------|-----------------------------|
| `logs/hospital-app.log`   | All application logs        |
| `logs/hospital-error.log` | ERROR level only            |
| `logs/hospital-webhook.log` | Webhook delivery audit    |

---

## 👨‍💻 Author

**Rajan** — CSE Student, GCET Greater Noida  
Built as a portfolio project demonstrating Spring Boot + JWT + Gradle backend development.
