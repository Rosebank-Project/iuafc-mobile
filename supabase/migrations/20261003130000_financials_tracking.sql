-- ==========================================
-- PHASE 2: FINANCIALS & TRACKING
-- ==========================================

-- 1. Create the Donations Table
CREATE TABLE public.donations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    member_id UUID REFERENCES public.members(id) ON DELETE SET NULL,
    amount NUMERIC(10,2) NOT NULL CHECK (amount > 0),
    donation_type TEXT NOT NULL CHECK (donation_type IN ('Tithe', 'Offering', 'Donation', 'Building Fund')),
    payment_method TEXT NOT NULL CHECK (payment_method IN ('Cash', 'Card', 'Transfer')),
    donation_date DATE NOT NULL DEFAULT CURRENT_DATE,
    recorded_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 2. Create the Attendance Table
CREATE TABLE public.attendance (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    event_id UUID REFERENCES public.events(id) ON DELETE CASCADE,
    member_id UUID REFERENCES public.members(id) ON DELETE CASCADE,
    status TEXT NOT NULL CHECK (status IN ('Present', 'Absent', 'Late')) DEFAULT 'Present',
    recorded_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    -- This prevents recording the same member twice for the same event
    UNIQUE(event_id, member_id)
);

-- ==========================================
-- ENABLE ROW LEVEL SECURITY (RLS)
-- ==========================================
ALTER TABLE public.donations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.attendance ENABLE ROW LEVEL SECURITY;

-- --- DONATIONS POLICIES ---
-- 1. Only Finance Officers and Admins can see ALL donations
CREATE POLICY "Donations full access for Finance/Admin" ON public.donations
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Finance Officer'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Finance Officer'));

-- 2. Normal Church Members can only see their OWN donations
CREATE POLICY "Members can see own donations" ON public.donations
FOR SELECT TO authenticated
USING (
    member_id = (SELECT member_id FROM public.profiles WHERE id = auth.uid())
);

-- --- ATTENDANCE POLICIES ---
-- 1. Admins, Pastors, and Ministry Leaders can manage all attendance
CREATE POLICY "Attendance full access for Leaders" ON public.attendance
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor', 'Ministry Leader'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor', 'Ministry Leader'));

-- 2. Normal Church Members can only view their OWN attendance history
CREATE POLICY "Members can see own attendance" ON public.attendance
FOR SELECT TO authenticated
USING (
    member_id = (SELECT member_id FROM public.profiles WHERE id = auth.uid())
);