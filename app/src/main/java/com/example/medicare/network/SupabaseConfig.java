package com.example.medicare.network;

/**
 * Supabase Project Configuration for Medicare Android Application
 *
 * HOW TO CONFIGURE YOUR LIVE SUPABASE BACKEND:
 * 1. Go to https://app.supabase.com and log into your project.
 * 2. Navigate to: Project Settings -> API
 * 3. Copy the "Project URL" and paste it into SUPABASE_URL below.
 * 4. Copy the "anon" / "public" API Key and paste it into SUPABASE_ANON_KEY below.
 *
 * SECURITY NOTE:
 * - Never place the Supabase 'service_role' secret key in an Android application.
 * - Only the 'anon' (public) key is required. Security is enforced using Supabase Row Level Security (RLS).
 */
public class SupabaseConfig {

    // Enter your Supabase Project URL (e.g., https://xyzcompany.supabase.co)
    public static String SUPABASE_URL = "https://xyzcompany.supabase.co";

    // Enter your Supabase Publishable / Anonymous Key
    public static String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.placeholderKey";

    /**
     * Checks whether the user has replaced placeholder credentials with valid Supabase project details.
     */
    public static boolean isConfigured() {
        return SUPABASE_URL != null
                && !SUPABASE_URL.trim().isEmpty()
                && !SUPABASE_URL.contains("xyzcompany")
                && SUPABASE_ANON_KEY != null
                && !SUPABASE_ANON_KEY.trim().isEmpty()
                && !SUPABASE_ANON_KEY.contains("placeholderKey");
    }
}
