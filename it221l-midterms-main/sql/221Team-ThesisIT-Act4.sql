-- ============================================================
-- 221Team-ThesisIT-Act4.sql
-- Facility/Equipment Borrowing System
-- Physical Schema + Sample Data
-- ============================================================

DROP DATABASE IF EXISTS borrowing_db;
CREATE DATABASE borrowing_db;
USE borrowing_db;

-- ============================================================
-- TABLE DEFINITIONS
-- ============================================================

CREATE TABLE PERSON (
    person_id       VARCHAR(20)     NOT NULL,
    last_name       VARCHAR(50)     NOT NULL,
    first_name      VARCHAR(50)     NOT NULL,
    middle_name     VARCHAR(50),
    person_type     ENUM('STUDENT','FACULTY','STAFF') NOT NULL,
    email           VARCHAR(100),
    contact_no      VARCHAR(20),
    CONSTRAINT pk_person PRIMARY KEY (person_id)
);

CREATE TABLE USER_ACCOUNT (
    account_id      INT             NOT NULL AUTO_INCREMENT,
    person_id       VARCHAR(20)     NOT NULL,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    role            ENUM('ADMIN','CUSTODIAN','BORROWER') NOT NULL,
    is_active       TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_account       PRIMARY KEY (account_id),
    CONSTRAINT fk_account_person FOREIGN KEY (person_id)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE EQUIPMENT (
    barcode         VARCHAR(30)     NOT NULL,
    item_name       VARCHAR(100)    NOT NULL,
    category        ENUM('EQUIPMENT','PERIPHERAL','ACCESSORY') NOT NULL,
    brand           VARCHAR(50),
    model           VARCHAR(50),
    status          ENUM('AVAILABLE','BORROWED','DAMAGED','DECOMMISSIONED') NOT NULL DEFAULT 'AVAILABLE',
    acquisition_date DATE,
    remarks         VARCHAR(255),
    CONSTRAINT pk_equipment PRIMARY KEY (barcode)
);

CREATE TABLE LAB_CLASS (
    class_id        VARCHAR(20)     NOT NULL,
    course_code     VARCHAR(20)     NOT NULL,
    section         VARCHAR(10)     NOT NULL,
    semester        VARCHAR(20)     NOT NULL,
    school_year     VARCHAR(10)     NOT NULL,
    room            VARCHAR(20),
    schedule        VARCHAR(50),
    faculty_id      VARCHAR(20)     NOT NULL,
    CONSTRAINT pk_lab_class     PRIMARY KEY (class_id),
    CONSTRAINT fk_class_faculty FOREIGN KEY (faculty_id)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE CLASS_ENROLLMENT (
    class_id        VARCHAR(20)     NOT NULL,
    student_id      VARCHAR(20)     NOT NULL,
    CONSTRAINT pk_enrollment        PRIMARY KEY (class_id, student_id),
    CONSTRAINT fk_enroll_class      FOREIGN KEY (class_id)
        REFERENCES LAB_CLASS(class_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_enroll_student    FOREIGN KEY (student_id)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE ACTIVITY_REQUEST (
    request_id      INT             NOT NULL AUTO_INCREMENT,
    activity_name   VARCHAR(100)    NOT NULL,
    activity_type   ENUM('TRAINING','MEETING','RECRUITMENT','CERTIFICATION','OTHER') NOT NULL,
    activity_date   DATE            NOT NULL,
    location        VARCHAR(100),
    requested_by    VARCHAR(20)     NOT NULL,
    approved_by     VARCHAR(20),
    status          ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    notes           VARCHAR(255),
    CONSTRAINT pk_request           PRIMARY KEY (request_id),
    CONSTRAINT fk_request_requester FOREIGN KEY (requested_by)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_request_approver  FOREIGN KEY (approved_by)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE BORROW_TRANSACTION (
    transaction_id  INT             NOT NULL AUTO_INCREMENT,
    borrower_id     VARCHAR(20)     NOT NULL,
    custodian_id    VARCHAR(20)     NOT NULL,
    class_id        VARCHAR(20),
    request_id      INT,
    borrow_date     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expected_return DATE            NOT NULL,
    return_date     DATETIME,
    return_custodian VARCHAR(20),
    transaction_status ENUM('BORROWED','RETURNED','RETURNED_WITH_ISSUE','OVERDUE') NOT NULL DEFAULT 'BORROWED',
    CONSTRAINT pk_transaction       PRIMARY KEY (transaction_id),
    CONSTRAINT fk_trans_borrower    FOREIGN KEY (borrower_id)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trans_custodian   FOREIGN KEY (custodian_id)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_trans_class       FOREIGN KEY (class_id)
        REFERENCES LAB_CLASS(class_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_trans_request     FOREIGN KEY (request_id)
        REFERENCES ACTIVITY_REQUEST(request_id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_trans_ret_custodian FOREIGN KEY (return_custodian)
        REFERENCES PERSON(person_id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE TABLE BORROW_ITEM (
    transaction_id  INT             NOT NULL,
    barcode         VARCHAR(30)     NOT NULL,
    item_condition_out  VARCHAR(100) NOT NULL DEFAULT 'Good',
    item_condition_in   VARCHAR(100),
    damage_notes    VARCHAR(255),
    CONSTRAINT pk_borrow_item       PRIMARY KEY (transaction_id, barcode),
    CONSTRAINT fk_bi_transaction    FOREIGN KEY (transaction_id)
        REFERENCES BORROW_TRANSACTION(transaction_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_bi_equipment      FOREIGN KEY (barcode)
        REFERENCES EQUIPMENT(barcode)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- ============================================================
-- SAMPLE DATA
-- ============================================================

-- PERSONS (students, faculty, staff)
INSERT INTO PERSON VALUES
('2021-00001', 'Santos',    'Maria',    'Cruz',     'STUDENT', 'santos.mc@slu.edu.ph',    '09171234501'),
('2021-00002', 'Reyes',     'John',     'dela',     'STUDENT', 'reyes.jd@slu.edu.ph',     '09171234502'),
('2021-00003', 'Garcia',    'Ana',      'Lopez',    'STUDENT', 'garcia.al@slu.edu.ph',    '09171234503'),
('2021-00004', 'Mendoza',   'Carlos',   'Bautista', 'STUDENT', 'mendoza.cb@slu.edu.ph',   '09171234504'),
('2021-00005', 'Flores',    'Liza',     'Tan',      'STUDENT', 'flores.lt@slu.edu.ph',    '09171234505'),
('2021-00006', 'Torres',    'Rico',     'Aquino',   'STUDENT', 'torres.ra@slu.edu.ph',    '09171234506'),
('2021-00007', 'Villanueva','Grace',    'Abad',     'STUDENT', 'villanueva.ga@slu.edu.ph','09171234507'),
('2021-00008', 'Cruz',      'Mark',     'Rivera',   'STUDENT', 'cruz.mr@slu.edu.ph',      '09171234508'),
('2021-00009', 'Ramos',     'Joy',      'Ocampo',   'STUDENT', 'ramos.jo@slu.edu.ph',     '09171234509'),
('2021-00010', 'Navarro',   'Ben',      'Salazar',  'STUDENT', 'navarro.bs@slu.edu.ph',   '09171234510'),
('FAC-001',    'Domingo',   'Rafael',   'Ignacio',  'FACULTY', 'domingo.ri@slu.edu.ph',   '09181234501'),
('FAC-002',    'Castillo',  'Elena',    'Morales',  'FACULTY', 'castillo.em@slu.edu.ph',  '09181234502'),
('FAC-003',    'Padilla',   'Jerome',   'Lim',      'FACULTY', 'padilla.jl@slu.edu.ph',   '09181234503'),
('STAFF-001',  'Hernandez', 'Miguel',   'Santos',   'STAFF',   'hernandez.ms@slu.edu.ph', '09191234501'),
('STAFF-002',  'Aguilar',   'Rose',     'Valdez',   'STAFF',   'aguilar.rv@slu.edu.ph',   '09191234502');

-- USER ACCOUNTS
INSERT INTO USER_ACCOUNT (person_id, username, password_hash, role) VALUES
('STAFF-001',  'custodian1',  'hashed_pass_1', 'CUSTODIAN'),
('STAFF-002',  'admin1',      'hashed_pass_2', 'ADMIN'),
('2021-00001', 'santos_mc',   'hashed_pass_3', 'BORROWER'),
('2021-00002', 'reyes_jd',    'hashed_pass_4', 'BORROWER'),
('2021-00003', 'garcia_al',   'hashed_pass_5', 'BORROWER'),
('FAC-001',    'domingo_ri',  'hashed_pass_6', 'BORROWER'),
('FAC-002',    'castillo_em', 'hashed_pass_7', 'BORROWER');

-- EQUIPMENT
INSERT INTO EQUIPMENT VALUES
('EQ-0001', 'Laptop',               'EQUIPMENT',  'Dell',     'Inspiron 15',  'AVAILABLE',  '2022-06-01', NULL),
('EQ-0002', 'Laptop',               'EQUIPMENT',  'Dell',     'Inspiron 15',  'BORROWED',   '2022-06-01', NULL),
('EQ-0003', 'Laptop',               'EQUIPMENT',  'HP',       'ProBook 450',  'AVAILABLE',  '2022-06-01', NULL),
('EQ-0004', 'Projector',            'EQUIPMENT',  'Epson',    'EB-X41',       'AVAILABLE',  '2021-01-15', NULL),
('EQ-0005', 'Projector',            'EQUIPMENT',  'Epson',    'EB-X41',       'DAMAGED',    '2021-01-15', 'Lamp defective'),
('EQ-0006', 'Wireless Mouse',       'PERIPHERAL', 'Logitech', 'M305',         'AVAILABLE',  '2023-03-10', NULL),
('EQ-0007', 'Wireless Mouse',       'PERIPHERAL', 'Logitech', 'M305',         'BORROWED',   '2023-03-10', NULL),
('EQ-0008', 'Wireless Mouse',       'PERIPHERAL', 'Logitech', 'M305',         'AVAILABLE',  '2023-03-10', NULL),
('EQ-0009', 'HDMI Cable',           'ACCESSORY',  'Generic',  '1.5m',         'AVAILABLE',  '2022-09-01', NULL),
('EQ-0010', 'HDMI Cable',           'ACCESSORY',  'Generic',  '1.5m',         'AVAILABLE',  '2022-09-01', NULL),
('EQ-0011', 'USB Hub',              'PERIPHERAL', 'Anker',    '7-Port',       'AVAILABLE',  '2023-01-20', NULL),
('EQ-0012', 'USB Hub',              'PERIPHERAL', 'Anker',    '7-Port',       'BORROWED',   '2023-01-20', NULL),
('EQ-0013', 'Extension Cord',       'ACCESSORY',  'Meralco',  '3-Gang 5m',    'AVAILABLE',  '2021-11-05', NULL),
('EQ-0014', 'Webcam',               'PERIPHERAL', 'Logitech', 'C920',         'AVAILABLE',  '2022-07-15', NULL),
('EQ-0015', 'Webcam',               'PERIPHERAL', 'Logitech', 'C920',         'AVAILABLE',  '2022-07-15', NULL),
('EQ-0016', 'Network Switch',       'EQUIPMENT',  'TP-Link',  'TL-SG108',     'AVAILABLE',  '2020-05-30', NULL),
('EQ-0017', 'Ethernet Cable',       'ACCESSORY',  'Generic',  'Cat6 5m',      'AVAILABLE',  '2022-01-10', NULL),
('EQ-0018', 'Ethernet Cable',       'ACCESSORY',  'Generic',  'Cat6 5m',      'BORROWED',   '2022-01-10', NULL),
('EQ-0019', 'Keyboard',             'PERIPHERAL', 'Dell',     'KB216',        'AVAILABLE',  '2023-02-14', NULL),
('EQ-0020', 'Keyboard',             'PERIPHERAL', 'Dell',     'KB216',        'DAMAGED',    '2023-02-14', 'Several keys not working');

-- LAB CLASSES
INSERT INTO LAB_CLASS VALUES
('IT221L-A', 'IT 221L', 'A', '2nd Semester', '2025-2026', 'CIS Lab 1', 'MWF 7:30-9:00',  'FAC-001'),
('IT221L-B', 'IT 221L', 'B', '2nd Semester', '2025-2026', 'CIS Lab 1', 'MWF 9:00-10:30', 'FAC-001'),
('IT211L-A', 'IT 211L', 'A', '2nd Semester', '2025-2026', 'CIS Lab 2', 'TTH 10:30-12:00','FAC-002'),
('CS201L-A', 'CS 201L', 'A', '2nd Semester', '2025-2026', 'CIS Lab 3', 'TTH 1:00-2:30',  'FAC-003');

-- CLASS ENROLLMENT
INSERT INTO CLASS_ENROLLMENT VALUES
('IT221L-A', '2021-00001'),
('IT221L-A', '2021-00002'),
('IT221L-A', '2021-00003'),
('IT221L-A', '2021-00004'),
('IT221L-A', '2021-00005'),
('IT221L-B', '2021-00006'),
('IT221L-B', '2021-00007'),
('IT221L-B', '2021-00008'),
('IT211L-A', '2021-00009'),
('IT211L-A', '2021-00010'),
('CS201L-A', '2021-00001'),
('CS201L-A', '2021-00003');

-- ACTIVITY REQUESTS
INSERT INTO ACTIVITY_REQUEST (activity_name, activity_type, activity_date, location, requested_by, approved_by, status, notes) VALUES
('CIS Recruitment Drive',       'RECRUITMENT',   '2026-03-15', 'Gym',          'FAC-001', 'FAC-003', 'APPROVED', 'Annual recruitment for incoming freshmen'),
('AWS Certification Seminar',   'CERTIFICATION', '2026-04-10', 'AVR 2',        '2021-00001','FAC-001','APPROVED','Batch exam simulation'),
('Faculty Team Meeting',        'MEETING',       '2026-04-08', 'Faculty Room', 'FAC-002', 'FAC-003', 'APPROVED', NULL),
('Python Training Workshop',    'TRAINING',      '2026-04-20', 'CIS Lab 2',    '2021-00005','FAC-002','PENDING', 'Needs 10 laptops'),
('Campus Job Fair',             'OTHER',         '2026-05-05', 'Covered Court','FAC-003', NULL,      'PENDING', NULL);

-- BORROW TRANSACTIONS
INSERT INTO BORROW_TRANSACTION (borrower_id, custodian_id, class_id, request_id, borrow_date, expected_return, return_date, return_custodian, transaction_status) VALUES
('2021-00001', 'STAFF-001', 'IT221L-A', NULL,  '2026-04-01 08:00:00', '2026-04-01', '2026-04-01 10:30:00', 'STAFF-001', 'RETURNED'),
('2021-00002', 'STAFF-001', 'IT221L-A', NULL,  '2026-04-01 08:00:00', '2026-04-01', '2026-04-01 10:30:00', 'STAFF-001', 'RETURNED_WITH_ISSUE'),
('FAC-001',    'STAFF-001', NULL,        1,     '2026-03-15 07:00:00', '2026-03-15', '2026-03-15 17:00:00', 'STAFF-001', 'RETURNED'),
('2021-00003', 'STAFF-001', 'IT221L-A', NULL,  '2026-04-03 08:00:00', '2026-04-03', NULL, NULL, 'OVERDUE'),
('2021-00006', 'STAFF-001', 'IT221L-B', NULL,  '2026-04-05 09:00:00', '2026-04-05', NULL, NULL, 'BORROWED'),
('FAC-002',    'STAFF-001', NULL,        3,     '2026-04-08 08:30:00', '2026-04-08', NULL, NULL, 'BORROWED'),
('2021-00001', 'STAFF-001', NULL,        2,     '2026-04-07 09:00:00', '2026-04-10', NULL, NULL, 'BORROWED');

-- BORROW ITEMS
INSERT INTO BORROW_ITEM VALUES
(1, 'EQ-0006', 'Good',    'Good',    NULL),
(1, 'EQ-0009', 'Good',    'Good',    NULL),
(2, 'EQ-0007', 'Good',    'Damaged', 'Left button unresponsive upon return'),
(3, 'EQ-0004', 'Good',    'Good',    NULL),
(3, 'EQ-0013', 'Good',    'Good',    NULL),
(4, 'EQ-0002', 'Good',    NULL,      NULL),
(4, 'EQ-0018', 'Good',    NULL,      NULL),
(5, 'EQ-0012', 'Good',    NULL,      NULL),
(6, 'EQ-0011', 'Good',    NULL,      NULL),
(6, 'EQ-0010', 'Good',    NULL,      NULL),
(7, 'EQ-0001', 'Good',    NULL,      NULL),
(7, 'EQ-0019', 'Good',    NULL,      NULL);
