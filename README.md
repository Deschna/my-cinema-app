# 🎥 My Cinema App 🎬
## Description
Welcome to My Cinema App! This project is an imitation of a cinema application.
<br>With this app, you can easily manage various movie-related tasks as a user or administrator using the following endpoints:
- `POST: /register` - for all unauthorized users, to register as a new user (by default, have a user role after registration)
- `GET: /cinema-halls`  - for USER and ADMIN, to get all available cinema halls
- `POST: /cinema-halls` - for ADMIN, to create a cinema hall
- `GET: /movies` - for USER and ADMIN, to get all available movies
- `POST: /movies` - for ADMIN, to create a movie
- `GET: /movie-sessions/available` - for USER and ADMIN, to get all available movie sessions (depends on show time)
- `POST: /movie-sessions` - for ADMIN, to create a movie session based on the movie, the cinema hall and the showtime
- `PUT,DELETE: /movie-sessions/{id}` - for ADMIN, to update or delete a movie session by its ID
- `PUT: /shopping-carts/movie-sessions` - for USER, to add a ticket to a movie session in the user's shopping cart
- `GET: /shopping-carts/by-user` - for USER, to get tickets in the user's shopping cart
- `POST: /orders/complete` - for USER, to buy tickets in the shopping cart
- `GET: /orders` - for USER, to get order history by user
- `GET: /users/by-email` - for ADMIN, to find a user by email

All endpoints receive and send data in JSON format
## Getting Started
- Clone this repository: `git clone https://github.com/Deschna/my-cinema-app.git`
- Replace `#MySQLProperties` values in `src/main/resources/db.properties` with suitable for your database
- Build the project using Maven (write `mvn clean package` command in your terminal)
- Deploy the WAR file to a servlet container (Tomcat for example)
- Navigate to http://localhost:8080 in your browser
## Used Technologies
- Java `17.0.6`
- Apache Maven `3.8.7`
- Apache Tomcat `9.0.73`
- MySQL `8.0.22`
- Spring `5.3.20`
- Spring Security `5.6.10`
- Hibernate `5.6.14.Final`
## Authors
[Déschna (Ditkovskyi Pasha)](https://github.com/Deschna)
