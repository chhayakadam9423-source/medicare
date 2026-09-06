# Medicare - Hospital Management System (Native Android Studio Project)

A native Android application built with **Java 17**, **XML layouts**, **Gradle**, and **Supabase Backend** (Auth + PostgREST + Row Level Security).

## Project Specifications

- **Application Name**: Medicare
- **Package Name**: `com.example.medicare`
- **Target SDK**: 34 (Android 14)
- **Minimum SDK**: 24 (Android 7.0 Nougat)
- **Language**: Java 17
- **UI Toolkit**: Android XML + Google Material Design 3
- **Backend / Database**: Supabase (PostgreSQL, GoTrue Auth, PostgREST API)
- **Architecture**: Asynchronous background threading via `ExecutorService` with main-thread `Handler` callbacks

---

## Screen Flow

1. **Initial Screen (`MainActivity`)**:
   - **MEDICARE**
   - **Hospital Management System**
   - **[Login]** (Navigates to `LoginActivity`)
   - **[Create Account]** (Navigates to `RegisterActivity`)

2. **Login (`LoginActivity`)**:
   - Email & Password input fields with real-time validation
   - Supabase GoTrue authentication
   - Dynamically loads and verifies user's official role from `public.profiles` database table
   - Supports `patient`, `doctor`, and `admin` roles

3. **Patient Registration (`RegisterActivity`)**:
   - Full Name, Email, Phone, and Password
   - Creates Supabase Auth account and provisions profile row with `role = 'patient'`
   - Public registration strictly creates patient accounts (admin/doctor accounts are provisioned securely)

4. **Hospital Dashboard (`DashboardActivity`)**:
   - Displays user welcome banner with active role badge
   - Quick action service cards:
     - **Book Consultation** (`BookAppointmentActivity`)
     - **My Appointments** (`AppointmentsActivity`)
     - **Find Doctors** (`DoctorsActivity`)
     - **Patient Records** (`PatientsActivity`)
   - Recent appointments list with status badges and quick view

5. **Book Consultation (`BookAppointmentActivity`)**:
   - Auto-fills authenticated patient's profile details
   - Dynamic doctor selection populated from Supabase `doctors` table
   - Android DatePickerDialog & Time slot picker
   - **Duplicate slot check**: Proactively prevents double-booking for the same doctor, date, and time

---

## Supabase Backend Setup

### Step 1: Execute SQL in Supabase SQL Editor
1. Log in to your [Supabase Dashboard](https://supabase.com/dashboard) and select your project.
2. Navigate to the **SQL Editor** in the left sidebar.
3. Open the file `supabase_schema.sql` from this repository.
4. Copy its entire content, paste it into the SQL Editor, and click **Run**.
5. The script creates:
   - `public.profiles` (User metadata & roles: `patient`, `doctor`, `admin`)
   - `public.doctors` (Clinician directory, hospital affiliations, consultation fees)
   - `public.patients` (Clinical demographics, blood groups, medical records)
   - `public.appointments` (Scheduling, status lifecycle, prescriptions)
   - Unique slot index `idx_unique_doctor_slot` to prevent double-booking
   - Row Level Security (RLS) policies for patient, doctor, and admin isolation
   - Automated trigger `on_auth_user_created` to sync new signups to `profiles`
   - Pre-seeded doctor and patient records

### Step 2: Configure Android App Credentials
1. In the Supabase Dashboard, open **Project Settings -> API**.
2. Copy your **Project URL** and **anon / public** API key.
3. Open the Android project file:
   `app/src/main/java/com/example/medicare/network/SupabaseConfig.java`
4. Replace the placeholder constants:
   ```java
   public static final String SUPABASE_URL = "https://your-project-ref.supabase.co";
   public static final String SUPABASE_ANON_KEY = "your-anon-public-key";
   ```

*Note: The application includes an offline in-memory fallback. Even before adding your Supabase keys, all UI flows, appointment booking, and list rendering can be previewed immediately.*

---

## Testing & Verification Checklist

1. **Database Schema Verification**:
   - In Supabase **Table Editor**, verify that `profiles`, `doctors`, `patients`, and `appointments` tables exist.
   - Confirm that the seed doctors (Dr. Rahul Sharma, Dr. Priya Patil, Dr. Amit Kulkarni) appear in the `doctors` table.

2. **Creating User Accounts**:
   - In the Android app, tap **Create Account**.
   - Register a new patient account (e.g. `ananya@example.com`).
   - Check Supabase **Authentication -> Users** to see the new Auth user and **Table Editor -> profiles** to see the created profile with `role = 'patient'`.

3. **Verifying Login**:
   - Tap **Login** in the app and sign in with the registered credentials.
   - Verify that the dashboard displays the user's name and role badge `PATIENT`.

4. **Booking an Appointment**:
   - Tap **Book Consultation**.
   - Select doctor "Dr. Rahul Sharma", choose a date and time slot (e.g. `2025-11-10` at `10:00 AM`).
   - Tap **Confirm Appointment Booking**.
   - Confirm the appointment appears under **My Appointments** with status `PENDING` or `CONFIRMED`.

5. **Verifying Duplicate Slot Prevention**:
   - Attempt to book another appointment with the same doctor, on the same date, and at the exact same time.
   - Verify that the app blocks the duplicate and displays the message:
     *"This time slot is already booked for this doctor. Please choose a different time."*

6. **Viewing Appointments in UI**:
   - Navigate to **My Appointments** to view the appointment cards with doctor name, department, date, time, and colored status badge.
   - Use the filter chips (`All`, `Upcoming`, `Completed`) to test status filtering.
