-- MECCA schema. OWNER: M1 ONLY. Safe to run on every start (IF NOT EXISTS).

CREATE TABLE IF NOT EXISTS clubs (
    club_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL UNIQUE,
    description TEXT,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','APPROVED')),
    engagement_score REAL NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS users (
    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    role TEXT NOT NULL CHECK (role IN ('STUDENT','ORGANIZER','ADMIN')),
    roll_number TEXT,
    admission_type TEXT NOT NULL DEFAULT 'REGULAR' CHECK (admission_type IN ('REGULAR','LATERAL','PWD')),
    managed_club_id INTEGER,
    FOREIGN KEY (managed_club_id) REFERENCES clubs(club_id)
);

CREATE TABLE IF NOT EXISTS venues (
    venue_id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    capacity_limit INTEGER NOT NULL DEFAULT 100,
    location TEXT
);

CREATE TABLE IF NOT EXISTS ktu_categories (
    code TEXT PRIMARY KEY,
    group_no INTEGER NOT NULL CHECK (group_no IN (1,2,3)),
    name TEXT NOT NULL,
    max_points INTEGER NOT NULL,
    points_l1 INTEGER NOT NULL DEFAULT 0,
    points_l2 INTEGER NOT NULL DEFAULT 0,
    points_l3 INTEGER NOT NULL DEFAULT 0,
    points_l4 INTEGER NOT NULL DEFAULT 0,
    points_l5 INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS events (
    event_id INTEGER PRIMARY KEY AUTOINCREMENT,
    club_id INTEGER NOT NULL,
    title TEXT NOT NULL,
    description TEXT,
    date_time TEXT NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    venue_id INTEGER,
    ktu_code TEXT,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level BETWEEN 1 AND 5),
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE','CANCELLED','COMPLETED')),
    FOREIGN KEY (club_id) REFERENCES clubs(club_id),
    FOREIGN KEY (venue_id) REFERENCES venues(venue_id)
);

CREATE TABLE IF NOT EXISTS registrations (
    reg_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    event_id INTEGER NOT NULL,
    reg_date TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED','CANCELLED')),
    UNIQUE (user_id, event_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);

CREATE TABLE IF NOT EXISTS attendance (
    att_id INTEGER PRIMARY KEY AUTOINCREMENT,
    reg_id INTEGER NOT NULL UNIQUE,
    check_in_time TEXT NOT NULL,
    verified INTEGER NOT NULL DEFAULT 1,
    FOREIGN KEY (reg_id) REFERENCES registrations(reg_id)
);

CREATE TABLE IF NOT EXISTS feedback (
    feedback_id INTEGER PRIMARY KEY AUTOINCREMENT,
    event_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comments TEXT,
    UNIQUE (event_id, user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS activity_points (
    ap_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    event_id INTEGER,
    ktu_code TEXT NOT NULL,
    level INTEGER NOT NULL DEFAULT 1 CHECK (level BETWEEN 1 AND 5),
    points INTEGER NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'APPROVED' CHECK (status IN ('PENDING','APPROVED')),
    awarded_on TEXT NOT NULL,
    certificate_note TEXT,
    UNIQUE (user_id, event_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);
