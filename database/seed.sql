INSERT INTO doctors (full_name, specialty, license_number) VALUES
    ('Dr. Laura Martinez', 'GENERAL_MEDICINE', 'RM-10234'),
    ('Dr. Andres Gomez', 'GENERAL_MEDICINE', 'RM-10871'),
    ('Dr. Camila Rojas', 'CARDIOLOGY', 'RM-20415'),
    ('Dr. Felipe Herrera', 'DERMATOLOGY', 'RM-30562'),
    ('Dr. Valentina Ruiz', 'PEDIATRICS', 'RM-40198')
ON CONFLICT (license_number) DO NOTHING;
