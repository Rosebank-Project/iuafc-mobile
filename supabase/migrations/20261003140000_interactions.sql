-- ==========================================
-- PHASE 3: INTERACTIONS & COMMUNICATION
-- ==========================================

-- 1. Create the Announcements Table
CREATE TABLE public.announcements (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    author_id UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- 2. Create the Event Registrations Table
CREATE TABLE public.event_registrations (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    event_id UUID REFERENCES public.events(id) ON DELETE CASCADE,
    member_id UUID REFERENCES public.members(id) ON DELETE CASCADE,
    status TEXT NOT NULL DEFAULT 'Registered' CHECK (status IN ('Registered', 'Cancelled')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),

    -- A member can only register once for the same event
    UNIQUE(event_id, member_id)
);

-- 3. Create the Ministry Activities Table
CREATE TABLE public.ministry_activities (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    ministry_id UUID REFERENCES public.ministries(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    description TEXT,
    activity_date DATE NOT NULL,
    created_by UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

-- ==========================================
-- ENABLE ROW LEVEL SECURITY (RLS)
-- ==========================================
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.event_registrations ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.ministry_activities ENABLE ROW LEVEL SECURITY;

-- --- ANNOUNCEMENTS POLICIES ---
-- Everyone can read, only Admin/Pastor can write
CREATE POLICY "Announcements read for all auth" ON public.announcements
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Announcements write for Admin/Pastor" ON public.announcements
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));

-- --- EVENT REGISTRATIONS POLICIES ---
-- Admin/Pastor can manage all. Normal members can register themselves and view their own.
CREATE POLICY "Registrations full access for Admin/Pastor" ON public.event_registrations
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor'));

CREATE POLICY "Members manage own registration" ON public.event_registrations
FOR ALL TO authenticated
USING (
    member_id = (SELECT member_id FROM public.profiles WHERE id = auth.uid())
)
WITH CHECK (
    member_id = (SELECT member_id FROM public.profiles WHERE id = auth.uid())
);

-- --- MINISTRY ACTIVITIES POLICIES ---
-- Everyone can read, but only Admin/Pastor/Ministry Leaders can write
CREATE POLICY "Activities read for all auth" ON public.ministry_activities
FOR SELECT TO authenticated USING (true);

CREATE POLICY "Activities write for Leaders" ON public.ministry_activities
FOR ALL TO authenticated
USING (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor', 'Ministry Leader'))
WITH CHECK (public.current_user_role() IN ('Administrator', 'Admin', 'Pastor', 'Ministry Leader'));