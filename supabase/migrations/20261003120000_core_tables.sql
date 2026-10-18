-- ==========================================
-- PHASE 1: CORE TABLES (REVISED v3)
-- ==========================================

-- 1. Create the Ministries Table (Fixed R3)
CREATE TABLE public.ministries (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL UNIQUE,
    description TEXT NOT NULL DEFAULT '',
    leader_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 2. Create the Members Table (Fixed R4)
CREATE TABLE public.members (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    full_name TEXT NOT NULL,
    contact_number TEXT,
    email TEXT UNIQUE,
    date_of_birth DATE,
    address TEXT,
    join_date DATE NOT NULL DEFAULT CURRENT_DATE,
    gender TEXT NOT NULL CHECK (gender IN ('Male', 'Female')),
    ministry_id UUID REFERENCES public.ministries(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 3. Create the Events Table (Fixed R2)
CREATE TABLE public.events (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    venue TEXT,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 4. Update Profiles Table Foreign Keys (Fixed R1)
-- We don't ADD the columns, they exist. We just add the constraints.
ALTER TABLE public.profiles
ADD CONSTRAINT profiles_member_id_fkey FOREIGN KEY (member_id) REFERENCES public.members(id) ON DELETE SET NULL,
ADD CONSTRAINT profiles_ministry_id_fkey FOREIGN KEY (ministry_id) REFERENCES public.ministries(id) ON DELETE SET NULL;

-- ==========================================
-- ENABLE ROW LEVEL SECURITY (RLS)
-- ==========================================
ALTER TABLE public.ministries ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.members ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.events ENABLE ROW LEVEL SECURITY;

-- --- MINISTRIES POLICIES ---
CREATE POLICY "Ministries read for all auth" ON public.ministries
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Ministries write for Admin" ON public.ministries
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin'));

-- --- MEMBERS POLICIES ---
CREATE POLICY "Members read for Admin/Pastor" ON public.members
FOR SELECT TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));

CREATE POLICY "Members read own" ON public.members
FOR SELECT TO authenticated
USING (id IN (SELECT member_id FROM public.profiles WHERE profiles.id = auth.uid()));

-- Fixed R5: Only Admin can write members
CREATE POLICY "Members write for Admin" ON public.members
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin'));

-- --- EVENTS POLICIES ---
CREATE POLICY "Events read for all auth" ON public.events
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Events write for Admin/Pastor" ON public.events
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));