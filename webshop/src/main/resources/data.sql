-- Kategorije
INSERT INTO categories (name, description, image_path) VALUES
                                                           ('Smoothie', 'Razni zdravi napici i smoothie paketi!', 'smoothie.jpg'),
                                                           ('Sokovi', 'Svježi sokovi od voća i povrća', 'sokovi.jpg'),
                                                           ('Smoothie paketići', 'Paketići za pripremu smoothie napitaka', 'paketici.jpg');

-- Korisnici (password je enkriptiran)
INSERT INTO users (username, email, password, role) VALUES
                                                        ('admin', 'admin1@webshop.hr', '$2a$10$FRH28VcX3qgV8tIDnhoelueo6t1zRL4zCItnFXpEKCGhLb5YjQcE6', 'ROLE_ADMIN'),
                                                        ('lorena', 'lorena@test12', '$2a$10$0cDC2wfOdf4vXgDTBzJ2QOkkF/45XWAzXyGGi2jCdawzA/f097jSi', 'ROLE_USER');

-- Proizvodi
INSERT INTO products (name, description, price, quantity, image_path, category_id) VALUES
                                                                                       ('Berry Blast', 'A refreshing mix of berries for a revitalizing smoothie.', 4.99, 33, 'smoothie_berry_blast.jpg', 1),
                                                                                       ('Exotic Cranberry', 'A tangy blend of cranberries with a tropical twist.', 5.99, 54, 'smoothie_exotic_cranberry.jpg', 1),
                                                                                       ('Morning Glory', 'Start your day with this energizing fruit smoothie.', 6.99, 79, 'smoothie_morning_glory.jpg', 1),
                                                                                       ('Multi Smoothie', 'A smoothie with a mix of superfoods for every occasion.', 3.99, 38, 'smoothie_multi_smoothie.jpg', 1);