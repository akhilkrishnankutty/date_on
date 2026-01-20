-- Password for all users is "password" -> $2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG

INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Akhil M', 'akhil@example.com', 9876543210, 'Male', 26, 'TechCorp', 'Loves coding and coffee', 'Kochi', 0.85, 'Coding,Reading,Travel', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Salini', 'salini@example.com', 9876543211, 'Female', 24, 'DesignStudio', 'Creative soul', 'Kochi', 0.83, 'Art,Music,Design', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('John Doe', 'john@example.com', 9876543212, 'Male', 30, 'BizInc', 'Ambitious and driven', 'Bangalore', 0.75, 'Business,Stock Market,Golf', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Jane Smith', 'jane@example.com', 9876543213, 'Female', 28, 'EduCare', 'Passionate about teaching', 'Chennai', 0.90, 'Teaching,Books,Yoga', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Alice Wonderland', 'alice@example.com', 9876543214, 'Female', 22, 'Student', 'Exploring the world', 'Mumbai', 0.60, 'Travel,Photography,Food', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Bob Builder', 'bob@example.com', 9876543215, 'Male', 35, 'BuildIt', 'Can we fix it?', 'Delhi', 0.70, 'DIY,Construction,Football', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Charlie Chaplin', 'charlie@example.com', 9876543216, 'Male', 40, 'ComedyClub', 'Making people smile', 'Pune', 0.95, 'Movies,Comedy,History', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Delta Force', 'delta@example.com', 9876543217, 'Female', 29, 'FitGym', 'Fitness enthusiast', 'Hyderabad', 0.80, 'Gym,Running,Nutrition', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Echo Base', 'echo@example.com', 9876543218, 'Male', 25, 'MusicLabel', 'Rhythm of life', 'Goa', 0.65, 'Music,Guitar,Concerts', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);
--INSERT INTO users (name, mail, number, gender, age, workplace, bio, location, compatibility_score, interests, "lock", password, loid) VALUES ('Foxtrot Uniform', 'foxtrot@example.com', 9876543219, 'Female', 27, 'FashionWeek', 'Style icon', 'Mumbai', 0.88, 'Fashion,Shopping,Blogging', false, '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 0);

-- Quiz Questions
INSERT INTO quiz_question (question_text, options) VALUES ('What is your ideal weekend?', 'Hiking,Reading,Partying,Sleeping');
INSERT INTO quiz_question (question_text, options) VALUES ('Which pet do you prefer?', 'Dog,Cat,Bird,None');
INSERT INTO quiz_question (question_text, options) VALUES ('Coffee or Tea?', 'Coffee,Tea,Neither,Both');
INSERT INTO quiz_question (question_text, options) VALUES ('Morning person or Night owl?', 'Morning,Night,Afternoon,Depends');
INSERT INTO quiz_question (question_text, options) VALUES ('Vacation preference?', 'Beach,Mountains,City,Home');
INSERT INTO quiz_question (question_text, options) VALUES ('Music genre?', 'Pop,Rock,Jazz,Classical');
INSERT INTO quiz_question (question_text, options) VALUES ('Favorite season?', 'Summer,Winter,Spring,Autumn');
INSERT INTO quiz_question (question_text, options) VALUES ('Movie genre?', 'Action,Comedy,Drama,Sci-Fi');
INSERT INTO quiz_question (question_text, options) VALUES ('Cooking skill level?', 'MasterChef,Decent,Can boil water,Disaster');
INSERT INTO quiz_question (question_text, options) VALUES ('Introvert or Extrovert?', 'Introvert,Extrovert,Ambivert,Confused');
INSERT INTO icebreakers (text) VALUES (' Hi! How''s your week going?');
INSERT INTO icebreakers (text) VALUES (' What''s your favorite travel destination?');
INSERT INTO icebreakers (text) VALUES (' Start any good shows lately?');
INSERT INTO icebreakers (text) VALUES (' Tacos or Sushi?');
INSERT INTO icebreakers (text) VALUES (' Cats or Dogs?');
INSERT INTO icebreakers (text) VALUES (' All-time favorite movie?');
INSERT INTO icebreakers (text) VALUES (' Beach or Mountains?');
