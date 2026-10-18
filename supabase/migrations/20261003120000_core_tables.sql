-- ==========================================
-- PHASE 1: CORE TABLES (REVISED)
-- ==========================================

-- 1. Create the Ministries Table
CREATE TABLE public.ministries (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 2. Create the Members Table
CREATE TABLE public.members (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    full_name TEXT NOT NULL,
    contact_number TEXT,
    email TEXT UNIQUE,
    date_of_birth DATE,
    address TEXT,
    join_date DATE DEFAULT CURRENT_DATE,
    gender TEXT CHECK (gender IN ('Male', 'Female')),
    ministry_id UUID REFERENCES public.ministries(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 3. Add leader_id to Ministries (Links to Members)
ALTER TABLE public.ministries
ADD COLUMN leader_id UUID REFERENCES public.members(id) ON DELETE SET NULL;

-- 4. Create the Events Table
CREATE TABLE public.events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    date DATE NOT NULL,
    event_time TIME NOT NULL,
    venue TEXT,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 5. Update Profiles Table (Fixing S2)
ALTER TABLE public.profiles ADD COLUMN member_id UUID REFERENCES public.members(id) ON DELETE SET NULL;
ALTER TABLE public.profiles ADD COLUMN ministry_id UUID REFERENCES public.ministries(id) ON DELETE SET NULL;

-- ==========================================
-- ENABLE ROW LEVEL SECURITY (RLS)
-- ==========================================
ALTER TABLE public.ministries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;

-- --- MINISTRIES POLICIES ---
-- Everyone can read, only Admin can write (S3)
CREATE POLICY "Ministries read for all auth" ON public.ministries
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Ministries write for Admin" ON public.ministries
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin'));

-- --- MEMBERS POLICIES ---
-- Admin & Pastor can read all. Normal members can only read their own (S1)
CREATE POLICY "Members read for Admin/Pastor" ON public.members
FOR SELECT TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));

CREATE POLICY "Members read own" ON public.members
FOR SELECT TO authenticated
USING (id IN (SELECT member_id FROM public.profiles WHERE profiles.id = auth.uid()));

CREATE POLICY "Members write for Admin/Pastor" ON public.members
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));

-- --- EVENTS POLICIES ---
-- Everyone can read, Admin & Pastor can write (B1)
CREATE POLICY "Events read for all auth" ON public.events
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Events write for Admin/Pastor" ON public.events
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));