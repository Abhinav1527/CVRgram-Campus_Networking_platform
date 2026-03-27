# 📱 CVRgram

CVRgram is a social media web application built using **Spring Boot** that allows users to connect, share posts, chat, and explore profiles — similar to a lightweight Instagram-style platform.

---

## 🚀 Features

- 🔐 User Authentication (Login & Register)
- 🧑 User Profiles
- 📝 Create & View Posts
- 💬 Real-time Chat (basic messaging)
- 🔍 Search Users
- 🏠 Home Feed
- 📧 Account Verification (if implemented)

---

## 🛠️ Tech Stack

- **Backend:** Spring Boot (Java)
- **Frontend:** HTML, CSS, JavaScript
- **Database:** (Configure in `application.properties`)
- **Build Tool:** Maven

---

## 📂 Project Structure

```
CVRgram/
│── src/main/java/com/CVRgram/
│   ├── Controller/     # Handles HTTP requests
│   ├── Service/        # Business logic
│   ├── Repository/     # Database access
│   ├── Model/          # Entity classes
│   └── CVRgram.java    # Main application
│
│── src/main/resources/
│   ├── static/         # HTML, CSS, JS files
│   └── application.properties
│
│── pom.xml             # Maven dependencies
```

---

## ⚙️ Setup & Installation

### 1. Clone the repository
```bash
git clone https://github.com/your-username/CVRgram.git
cd CVRgram
```

### 2. Configure Database
Edit `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Run the project
```bash
./mvnw spring-boot:run
```
or (Windows)
```bash
mvnw.cmd spring-boot:run
```

---

## 🌐 Access the Application

Open your browser and go to:
```
http://localhost:8080
```

---

## 📸 Screens (Available Pages)

- Login Page → `/login.html`
- Register Page → `/register.html`
- Home Page → `/home.html`
- Profile Page → `/profile.html`
- Chat Page → `/chat.html`
- Search Page → `/search.html`

---

## 🔧 Future Improvements

- 📱 Responsive UI
- 🔔 Notifications
- ❤️ Likes & Comments
- 📷 Image Upload Support
- 🔐 JWT Authentication

---

## 🤝 Contributing

Contributions are welcome!

1. Fork the repo
2. Create a new branch
3. Make changes
4. Submit a pull request

---

## 📄 License

This project is open-source and available under the **MIT License**.

---

## 👨‍💻 Author

Developed by **Somasani Abhinav**

---

⭐ If you like this project, give it a star!