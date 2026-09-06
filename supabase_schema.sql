-- ==============================================================================
-- MEDICARE HOSPITAL MANAGEMENT SYSTEM - SUPABASE DATABASE SCHEMA
-- ==============================================================================
-- This SQL script sets up the database for the Medicare Android application.
-- It can be safely executed multiple times in the Supabase SQL Editor.
-- It creates:
--   1. profiles (users table extending Supabase auth.users)
--   2. doctors (physicians directory)
--   3. patients (clinical medical records)
--   4. appointments (consultation bookings & clinical notes)
--   5. Row Level Security (RLS) policies for patient, doctor, and admin roles
--   6. Unique slot index preventing duplicate appointments
--   7. Sample doctors data requested for testing
-- ==============================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==============================================================================
-- 1. USERS / PROFILES TABLE
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.profiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    email TEXT NOT NULL,
    full_name TEXT NOT NULL,
    role TEXT NOT NULL DEFAULT 'patient' CHECK (role IN ('patient', 'doctor', 'admin')),
    phone TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now()),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- ==============================================================================
-- 2. DOCTORS TABLE
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    specialization TEXT NOT NULL,
    qualification TEXT NOT NULL,
    experience INT NOT NULL DEFAULT 0,
    hospital_name TEXT NOT NULL DEFAULT 'Medicare Hospital',
    consultation_fee NUMERIC(10, 2) NOT NULL DEFAULT 500.00,
    department TEXT,
    room_number TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- ==============================================================================
-- 3. PATIENTS TABLE
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES auth.users(id) ON DELETE SET NULL,
    name TEXT NOT NULL,
    mrn TEXT UNIQUE NOT NULL,
    age INT,
    gender TEXT,
    blood_group TEXT,
    phone TEXT,
    diagnosis TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- ==============================================================================
-- 4. APPOINTMENTS TABLE
-- ==============================================================================
CREATE TABLE IF NOT EXISTS public.appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id UUID REFERENCES auth.users(id) ON DELETE CASCADE,
    doctor_id UUID REFERENCES public.doctors(id) ON DELETE CASCADE,
    patient_name TEXT NOT NULL,
    doctor_name TEXT NOT NULL,
    department TEXT,
    appointment_date DATE NOT NULL,
    appointment_time TEXT NOT NULL,
    reason TEXT,
    status TEXT NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Confirmed', 'Rejected', 'Completed', 'Cancelled')),
    diagnosis TEXT,
    medicine TEXT,
    dosage TEXT,
    instructions TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT timezone('utc'::text, now())
);

-- ==============================================================================
-- 5. DUPLICATE SLOT PREVENTION (Section 8)
-- ==============================================================================
-- A doctor cannot have more than one 'Pending' or 'Confirmed' appointment
-- for the exact same date and time slot.
CREATE UNIQUE INDEX IF NOT EXISTS idx_unique_doctor_slot
ON public.appointments (doctor_id, appointment_date, appointment_time)
WHERE status IN ('Pending', 'Confirmed');

-- ==============================================================================
-- 6. AUTOMATIC USER PROFILE TRIGGER
-- ==============================================================================
-- Automatically creates a record in public.profiles when an auth.users record is created
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger AS $$
BEGIN
  INSERT INTO public.profiles (id, email, full_name, role, phone)
  VALUES (
    NEW.id,
    NEW.email,
    COALESCE(NEW.raw_user_meta_data->>'full_name', 'Patient'),
    COALESCE(NEW.raw_user_meta_data->>'role', 'patient'),
    COALESCE(NEW.raw_user_meta_data->>'phone', '')
  )
  ON CONFLICT (id) DO UPDATE SET
    email = EXCLUDED.email,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    phone = EXCLUDED.phone,
    updated_at = timezone('utc'::text, now());
  RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
  AFTER INSERT ON auth.users
  FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ==============================================================================
-- 7. SECURITY & RLS HELPER FUNCTIONS
-- ==============================================================================
CREATE OR REPLACE FUNCTION public.get_user_role()
RETURNS TEXT LANGUAGE sql STABLE SECURITY DEFINER AS $$
    SELECT COALESCE((SELECT role FROM public.profiles WHERE id = auth.uid()), 'patient');
$$;

CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN LANGUAGE sql STABLE SECURITY DEFINER AS $$
    SELECT (public.get_user_role() = 'admin');
$$;

-- ==============================================================================
-- 8. ROW LEVEL SECURITY (RLS) POLICIES (Section 2)
-- ==============================================================================

-- Enable RLS on all tables
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.doctors ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.patients ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;

-- ------------------------------------------------------------------------------
-- A. PROFILES TABLE POLICIES
-- ------------------------------------------------------------------------------
DROP POLICY IF EXISTS "Users can read own profile or admin reads all" ON public.profiles;
CREATE POLICY "Users can read own profile or admin reads all" ON public.profiles
FOR SELECT TO authenticated
USING (auth.uid() = id OR public.is_admin());

DROP POLICY IF EXISTS "Users can update own profile" ON public.profiles;
CREATE POLICY "Users can update own profile" ON public.profiles
FOR UPDATE TO authenticated
USING (auth.uid() = id OR public.is_admin())
WITH CHECK (auth.uid() = id OR public.is_admin());

DROP POLICY IF EXISTS "Insert profile allowed for authenticated user" ON public.profiles;
CREATE POLICY "Insert profile allowed for authenticated user" ON public.profiles
FOR INSERT TO authenticated
WITH CHECK (auth.uid() = id);

-- ------------------------------------------------------------------------------
-- B. DOCTORS TABLE POLICIES
-- ------------------------------------------------------------------------------
-- Patients and users can browse the doctor directory
DROP POLICY IF EXISTS "Anyone can view doctors directory" ON public.doctors;
CREATE POLICY "Anyone can view doctors directory" ON public.doctors
FOR SELECT TO authenticated, anon
USING (true);

-- Only Admin can insert, update, or delete doctors
DROP POLICY IF EXISTS "Admin can insert doctors" ON public.doctors;
CREATE POLICY "Admin can insert doctors" ON public.doctors
FOR INSERT TO authenticated
WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Admin can update doctors" ON public.doctors;
CREATE POLICY "Admin can update doctors" ON public.doctors
FOR UPDATE TO authenticated
USING (public.is_admin())
WITH CHECK (public.is_admin());

DROP POLICY IF EXISTS "Admin can delete doctors" ON public.doctors;
CREATE POLICY "Admin can delete doctors" ON public.doctors
FOR DELETE TO authenticated
USING (public.is_admin());

-- ------------------------------------------------------------------------------
-- C. PATIENTS TABLE POLICIES
-- ------------------------------------------------------------------------------
-- Patient can access their own record, clinical staff/doctor/admin can view all
DROP POLICY IF EXISTS "Patients view own record or clinical staff view" ON public.patients;
CREATE POLICY "Patients view own record or clinical staff view" ON public.patients
FOR SELECT TO authenticated
USING (user_id = auth.uid() OR public.get_user_role() IN ('doctor', 'admin'));

DROP POLICY IF EXISTS "Patients can create own patient profile" ON public.patients;
CREATE POLICY "Patients can create own patient profile" ON public.patients
FOR INSERT TO authenticated
WITH CHECK (user_id = auth.uid() OR public.get_user_role() IN ('doctor', 'admin'));

DROP POLICY IF EXISTS "Clinical staff or patient update patient record" ON public.patients;
CREATE POLICY "Clinical staff or patient update patient record" ON public.patients
FOR UPDATE TO authenticated
USING (user_id = auth.uid() OR public.get_user_role() IN ('doctor', 'admin'))
WITH CHECK (user_id = auth.uid() OR public.get_user_role() IN ('doctor', 'admin'));

-- ------------------------------------------------------------------------------
-- D. APPOINTMENTS TABLE POLICIES
-- ------------------------------------------------------------------------------
-- 1. SELECT:
--    - Patient can view their own appointments (patient_id = auth.uid())
--    - Doctor can view appointments assigned to that doctor
--    - Admin can view all appointments
DROP POLICY IF EXISTS "Appointments select policy" ON public.appointments;
CREATE POLICY "Appointments select policy" ON public.appointments
FOR SELECT TO authenticated
USING (
    patient_id = auth.uid()
    OR doctor_id IN (SELECT id FROM public.doctors WHERE user_id = auth.uid())
    OR public.is_admin()
);

-- 2. INSERT:
--    - Patient can create their own appointments
--    - Admin can create appointments
DROP POLICY IF EXISTS "Appointments insert policy" ON public.appointments;
CREATE POLICY "Appointments insert policy" ON public.appointments
FOR INSERT TO authenticated
WITH CHECK (
    patient_id = auth.uid()
    OR public.is_admin()
);

-- 3. UPDATE:
--    - Patient can cancel/modify their own appointment
--    - Doctor can update assigned appointments (diagnosis, medicine, dosage, instructions, status)
--    - Admin can update any appointment
DROP POLICY IF EXISTS "Appointments update policy" ON public.appointments;
CREATE POLICY "Appointments update policy" ON public.appointments
FOR UPDATE TO authenticated
USING (
    patient_id = auth.uid()
    OR doctor_id IN (SELECT id FROM public.doctors WHERE user_id = auth.uid())
    OR public.is_admin()
)
WITH CHECK (
    patient_id = auth.uid()
    OR doctor_id IN (SELECT id FROM public.doctors WHERE user_id = auth.uid())
    OR public.is_admin()
);

-- 4. DELETE:
--    - Admin can delete appointments
DROP POLICY IF EXISTS "Appointments delete policy" ON public.appointments;
CREATE POLICY "Appointments delete policy" ON public.appointments
FOR DELETE TO authenticated
USING (public.is_admin());

-- ==============================================================================
-- 9. SEED DATABASE TEST DATA (Section 6)
-- ==============================================================================
-- Sample doctors specified in User Requirements:
--   1. Dr. Rahul Sharma (Cardiology, MBBS MD, Exp: 10, Medicare Hospital, Fee: 500)
--   2. Dr. Priya Patil (Dermatology, MBBS MD, Exp: 7, City Care Hospital, Fee: 400)
--   3. Dr. Amit Kulkarni (Orthopedics, MBBS MS, Exp: 8, LifeLine Hospital, Fee: 450)
INSERT INTO public.doctors (name, specialization, qualification, experience, hospital_name, consultation_fee, department, room_number)
VALUES
    ('Dr. Rahul Sharma', 'Cardiology', 'MBBS, MD', 10, 'Medicare Hospital', 500.00, 'Cardiology', 'Room 101'),
    ('Dr. Priya Patil', 'Dermatology', 'MBBS, MD', 7, 'City Care Hospital', 400.00, 'Dermatology', 'Room 203'),
    ('Dr. Amit Kulkarni', 'Orthopedics', 'MBBS, MS', 8, 'LifeLine Hospital', 450.00, 'Orthopedics', 'Room 305')
ON CONFLICT DO NOTHING;

-- Sample Patients
INSERT INTO public.patients (name, mrn, age, gender, blood_group, phone, diagnosis)
VALUES
    ('Ananya Deshmukh', 'MRN-10021', 28, 'Female', 'O+', '+91 98230 11223', 'Mild Skin Allergy'),
    ('Ramesh Shinde', 'MRN-10022', 54, 'Male', 'B+', '+91 98220 44556', 'Joint Stiffness & Hypertension'),
    ('Suresh Joshi', 'MRN-10023', 46, 'Male', 'AB+', '+91 98210 77889', 'Post-Op Knee Checkup')
ON CONFLICT DO NOTHING;
