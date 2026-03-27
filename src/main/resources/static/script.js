function login() {

    const email = document.getElementById("username").value;
    const password = document.getElementById("password").value;

    fetch("/auth/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    })
        .then(res => res.text())
        .then(data => {

            alert(data);

            if (data === "Login Successful") {
                window.location.href = "/home";
            }
        });
}

function register() {

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const department = document.getElementById("department").value;
    const password = document.getElementById("password").value;

    fetch("/auth/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: username,
            email: email,
            department: department,
            password: password
        })
    })
        .then(res => res.text())
        .then(data => {

            alert(data);

            // 🔥 THIS IS WHAT YOU ARE MISSING
            if (data === "OTP Sent to your email.") {

                // store email for OTP page
                localStorage.setItem("verifyEmail", email);

                // redirect to verify page
                window.location.href = "/auth/verify";
            }

        });
}

function verifyOtp() {

    const otp = document.getElementById("otp").value;

    // 🔥 Get email from storage
    const email = localStorage.getItem("verifyEmail");

    fetch("/auth/verify", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: email,
            otp: otp
        })
    })
        .then(res => res.text())
        .then(data => {

            alert(data);

            if (data === "Verification Successful") {

                localStorage.removeItem("verifyEmail");

                window.location.href = "/home";
            }
        });
}

function createPost() {
    const content = document.getElementById("postContent").value;

    fetch("/posts", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ content: content })
    })
        .then(res => res.text())
        .then(() => loadPosts());
}

function loadPosts() {
    fetch("/posts")
        .then(res => res.json())
        .then(posts => {

            const feed = document.getElementById("feed");
            if (!feed) return;

            feed.innerHTML = "";

            posts.forEach(post => {

                const card = document.createElement("div");
                card.className = "post-card";

                card.innerHTML = `
                    <div class="post-header">Student</div>
                    <div class="post-content">${post}</div>
                    <div class="post-actions">
                        <i data-lucide="heart"></i>
                        <i data-lucide="message-circle"></i>
                        <i data-lucide="send"></i>
                    </div>
                `;

                feed.appendChild(card);
            });

            lucide.createIcons();
        });
}

function performSearch() {
    const query = document.getElementById("searchInput").value;

    fetch("/search/api?query=" + query)
        .then(res => res.text())
        .then(data => {
            document.getElementById("searchResults").innerText = data;
        });
}
fetch("/profile/data")
    .then(res => res.json())
    .then(user => {

        document.getElementById("name").innerText = user.username;
        document.getElementById("email").innerText = user.email;

    });