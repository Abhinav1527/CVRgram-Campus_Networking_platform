// Dark mode initialization (run immediately)
if (localStorage.getItem('darkMode') === 'enabled') {
    document.body.classList.add('dark-mode');
}

function withCacheBust(url) {
    if (!url || !url.startsWith('/uploads/')) return url;
    const separator = url.includes('?') ? '&' : '?';
    return `${url}${separator}t=${Date.now()}`;
}

function openUserProfileById(userId) {
    if (!userId) return;
    window.location.href = `/profile?userId=${userId}`;
}

function showToast(message) {
    let toast = document.getElementById('cvr-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'cvr-toast';
        document.body.appendChild(toast);
    }
    toast.textContent = message;
    toast.className = 'toast-notification show';
    
    // Auto hide after 3 seconds
    setTimeout(() => {
        toast.className = 'toast-notification';
    }, 3000);
}

function toggleDarkMode() {
    const isDark = document.body.classList.toggle('dark-mode');
    localStorage.setItem('darkMode', isDark ? 'enabled' : 'disabled');
    
    const toggle = document.getElementById('darkModeToggle');
    if (toggle) {
        if (isDark) {
            toggle.classList.add('on');
        } else {
            toggle.classList.remove('on');
        }
    }
}

function togglePasswordVisibility(inputId, spanElement) {
    const input = document.getElementById(inputId);
    const eyeIcon = spanElement.querySelector('.eye-icon');
    const eyeOffIcon = spanElement.querySelector('.eye-off-icon');
    
    if (input.type === 'password') {
        input.type = 'text';
        eyeIcon.style.display = 'none';
        eyeOffIcon.style.display = 'block';
    } else {
        input.type = 'password';
        eyeIcon.style.display = 'block';
        eyeOffIcon.style.display = 'none';
    }
}

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

            showToast(data);

            if (data === "Login Successful") {
                setTimeout(() => window.location.href = "/home", 1000);
            }
        });
}

function register() {

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const department = document.getElementById("department").value;
    const password = document.getElementById("password").value;

    const passwordRegex = /^(?=.*[0-9])(?=.*[!@#$%^&*(),.?":{}|<>])(?=.*[A-Z]).{6,15}$/;
    if (!passwordRegex.test(password)) {
        showToast("Password must be 6-15 characters, with at least 1 number, 1 uppercase, and 1 special char.");
        return;
    }

    const btn = document.querySelector('.btn-auth');
    const originalText = btn.innerText;
    btn.innerText = "Sending OTP...";
    btn.disabled = true;

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
            
            btn.innerText = originalText;
            btn.disabled = false;

            showToast(data);

            if (data === "OTP Sent to your email.") {
                localStorage.setItem("verifyEmail", email);
                setTimeout(() => window.location.href = "/verify", 1000);
            }

        })
        .catch(err => {
            btn.innerText = originalText;
            btn.disabled = false;
            showToast("Network Error");
        });
}

function verifyOtp() {

    const otp = document.getElementById("otp").value;
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

            showToast(data);

            if (data === "Verification Successful") {
                localStorage.removeItem("verifyEmail");
                setTimeout(() => window.location.href = "/home", 1000);
            }
        });
}

let selectedMediaFile = null;

function previewMedia(event) {
    const file = event.target.files[0];
    if (file) {
        selectedMediaFile = file;
        const reader = new FileReader();
        reader.onload = function(e) {
            document.getElementById('imagePreview').src = e.target.result;
            document.getElementById('imagePreviewContainer').style.display = 'block';
        }
        reader.readAsDataURL(file);
    }
}

function removeMedia() {
    selectedMediaFile = null;
    document.getElementById('mediaUpload').value = '';
    document.getElementById('imagePreviewContainer').style.display = 'none';
    document.getElementById('imagePreview').src = '';
}

function createPost() {
    const content = document.getElementById("postContent").value;
    
    if(!content.trim() && !selectedMediaFile) return;

    const formData = new FormData();
    formData.append("content", content);
    if (selectedMediaFile) {
        formData.append("file", selectedMediaFile);
    }

    fetch("/api/posts", {
        method: "POST",
        body: formData
    })
        .then(res => res.text())
        .then(() => {
            document.getElementById("postContent").value = '';
            removeMedia();
            loadPosts();
        });
}

function loadPosts() {
    const isSaved = window.location.pathname === "/saved";
    const isProfile = window.location.pathname.startsWith("/profile");
    const urlParams = new URLSearchParams(window.location.search);
    const targetUserId = urlParams.get('userId');

    fetch("/profile/data").then(res => res.ok ? res.json() : null).catch(() => null)
        .then(user => {
            const currentUserId = user ? user.id : null;
            let endpoint = "/api/posts";
            
            if (isSaved) {
                endpoint = "/api/posts/saved";
            } else if (isProfile) {
                if (targetUserId) {
                    endpoint = `/api/posts/user/${targetUserId}`;
                } else if (currentUserId) {
                    endpoint = `/api/posts/user/${currentUserId}`;
                } else {
                    return;
                }
            }

            fetch(endpoint).then(res => res.json()).then(posts => {

        const feed = document.getElementById("feed");
        if (!feed) return;

        feed.innerHTML = "";

        posts.forEach(post => {

            const card = document.createElement("div");
            card.className = "card post";
            
            const authorName = post.author ? post.author.username : "Unknown";
            const authorHeadline = post.author && post.author.headline ? post.author.headline : "Student at CVR College";
            const avatarName = post.author ? post.author.username : "Student";
            const avatarUrl = (post.author && post.author.profilePhotoUrl)
                ? withCacheBust(post.author.profilePhotoUrl)
                : `https://ui-avatars.com/api/?name=${encodeURIComponent(avatarName)}&background=random`;
            
            let mediaHtml = "";
            if (post.imageUrl) {
                mediaHtml = `
                    <div class="post-media-container" ondblclick="triggerInstaLike(${post.id})">
                        <img src="${post.imageUrl}" style="width: 100%; border-radius: 8px; border: 1px solid var(--border-color); cursor: pointer;" alt="Post Media">
                        <i data-lucide="heart" class="instagram-like-overlay" id="insta-like-${post.id}" style="width: 80px; height: 80px; fill: white; color: white;"></i>
                    </div>
                `;
            }

            let likeCount = post.likedUserIds ? post.likedUserIds.length : 0;
            let isLiked = currentUserId && post.likedUserIds && post.likedUserIds.includes(currentUserId);
            let likeColorStyle = isLiked ? 'color: #ed4956; fill: #ed4956;' : '';

            let isSaved = currentUserId && post.savedUserIds && post.savedUserIds.includes(currentUserId);
            let saveColorStyle = isSaved ? 'color: var(--text-dark); fill: var(--text-dark);' : '';

            let commentsHtml = "";
            if (post.comments && post.comments.length > 0) {
                post.comments.forEach(c => {
                    let cAuthor = c.author ? c.author.username : "Unknown";
                    let cAuthorId = c.author ? c.author.id : null;
                    let cAvatarUrl = (c.author && c.author.profilePhotoUrl)
                        ? withCacheBust(c.author.profilePhotoUrl)
                        : `https://ui-avatars.com/api/?name=${encodeURIComponent(cAuthor)}&background=random`;
                    commentsHtml += `
                        <div class="comment-item" style="position: relative;">
                            <img src="${cAvatarUrl}" class="nav-avatar" style="width: 32px; height: 32px; cursor: ${cAuthorId ? 'pointer' : 'default'};" alt="Profile" ${cAuthorId ? `onclick="openUserProfileById(${cAuthorId})"` : ''}>
                            <div class="comment-content">
                                <span class="comment-author" style="display: flex; justify-content: space-between; align-items: center;">
                                    <span style="cursor: ${cAuthorId ? 'pointer' : 'default'};" ${cAuthorId ? `onclick="openUserProfileById(${cAuthorId})"` : ''}>${cAuthor}</span>
                                    <i data-lucide="trash-2" onclick="deleteComment(${post.id}, ${c.id})" style="width: 14px; height: 14px; color: var(--danger-color, #ef4444); cursor: pointer;"></i>
                                </span>
                                <span>${c.text}</span>
                            </div>
                        </div>
                    `;
                });
            }
            let deletePostHtml = "";
            if (currentUserId && post.author && post.author.id === currentUserId) {
                deletePostHtml = `<i data-lucide="trash-2" onclick="deletePost(${post.id})" style="width: 18px; height: 18px; color: var(--danger-color, #ef4444); cursor: pointer; margin-right: 12px;" title="Delete Post"></i>`;
            }

            card.innerHTML = `
                <div class="post-header" style="padding: 12px 16px; border-bottom: none;">
                    <div class="post-author-info">
                        <a href="/profile?userId=${post.author.id}">
                            <img src="${avatarUrl}" class="nav-avatar" style="width: 32px; height: 32px;" alt="Profile">
                        </a>
                        <div class="post-author-details" style="display: flex; align-items: center; gap: 6px;">
                            <a href="/profile?userId=${post.author.id}" style="text-decoration: none; color: inherit;">
                                <span class="post-author-name" style="font-weight: 600; font-size: 14px;">${authorName}</span>
                            </a>
                            <span class="post-time" style="font-size: 12px; color: var(--text-light);">• Just now</span>
                        </div>
                    </div>
                    <div style="display: flex; align-items: center;">
                        ${deletePostHtml}
                        <i data-lucide="more-horizontal" style="width: 20px; height: 20px; color: var(--text-dark); cursor: pointer;"></i>
                    </div>
                </div>
                ${mediaHtml}
                <div class="post-actions" style="display: flex; justify-content: space-between; padding: 12px 16px 8px; border-top: none;">
                    <div style="display: flex; gap: 16px; align-items: center;">
                        <i id="like-icon-${post.id}" data-lucide="heart" onclick="toggleLike(${post.id})" style="width: 24px; height: 24px; cursor: pointer; ${likeColorStyle}"></i>
                        <i data-lucide="message-circle" onclick="toggleCommentSection(${post.id})" style="width: 24px; height: 24px; cursor: pointer;"></i>
                        <i data-lucide="send" onclick="sendPost(${post.id})" style="width: 24px; height: 24px; cursor: pointer; transform: rotate(15deg); margin-top: -4px;"></i>
                    </div>
                    <div>
                        <i id="save-icon-${post.id}" data-lucide="bookmark" onclick="toggleSavePost(${post.id})" style="width: 24px; height: 24px; cursor: pointer; ${saveColorStyle}"></i>
                    </div>
                </div>
                <div class="post-likes" style="padding: 0 16px 8px; font-weight: 600; font-size: 14px;">
                    <span id="like-count-${post.id}">${likeCount > 0 ? likeCount + ' likes' : 'Be the first to like this'}</span>
                </div>
                <div class="post-caption" style="padding: 0 16px 12px; font-size: 14px;">
                    <span style="font-weight: 600; margin-right: 4px;">${authorName}</span>${post.content}
                </div>
                <div id="comments-section-${post.id}" class="comment-section" style="display: block;">
                    <div class="comments-list" id="comments-list-${post.id}">
                        ${commentsHtml}
                    </div>
                    <div class="comment-input-container">
                        <img src="https://ui-avatars.com/api/?name=Me&background=random" class="nav-avatar" style="width: 32px; height: 32px;" alt="Profile">
                        <div class="comment-input-wrapper">
                            <input type="text" id="comment-input-${post.id}" class="comment-input" placeholder="Add a comment..." onkeypress="handleCommentKeyPress(event, ${post.id})">
                            <div class="comment-icons">
                                <i data-lucide="smile" style="width: 20px; height: 20px; color: var(--text-light); cursor: pointer;"></i>
                                <i data-lucide="image" style="width: 20px; height: 20px; color: var(--text-light); cursor: pointer;"></i>
                            </div>
                        </div>
                    </div>
                </div>
            `;

            feed.appendChild(card);
        });

        lucide.createIcons();
    });
    });
}

function triggerInstaLike(postId) {
    const overlay = document.getElementById(`insta-like-${postId}`);
    if(overlay) {
        overlay.classList.remove("animate");
        void overlay.offsetWidth; // trigger reflow
        overlay.classList.add("animate");
    }
    
    // Always trigger toggle Like (ideally we would only like if not liked, but toggle is fine for now)
    toggleLike(postId);
}

function toggleLike(postId) {
    const icon = document.getElementById(`like-icon-${postId}`);
    
    // Add animation class
    icon.classList.add("like-animation");
    setTimeout(() => icon.classList.remove("like-animation"), 600);

    fetch(`/api/posts/${postId}/like`, { method: "POST" })
        .then(res => {
            if(res.status === 401) { showToast("Please login to like"); throw new Error("Unauthorized"); }
            return res.text();
        })
        .then(countStr => {
            const span = document.getElementById(`like-count-${postId}`);
            const count = parseInt(countStr);
            if (span) {
                span.innerText = count > 0 ? count + ' likes' : 'Be the first to like this';
            }
            
            // Toggle red heart manually
            if (icon.style.color === "rgb(237, 73, 86)" || icon.style.color === "#ed4956") {
                icon.style.color = "";
                icon.style.fill = "none";
            } else {
                icon.style.color = "#ed4956";
                icon.style.fill = "#ed4956";
            }
        }).catch(e => {});
}

function toggleSavePost(postId) {
    const icon = document.getElementById(`save-icon-${postId}`);
    
    // Add simple animation
    icon.style.transform = "scale(1.2)";
    setTimeout(() => icon.style.transform = "scale(1)", 200);

    fetch(`/api/posts/${postId}/save`, { method: "POST" })
        .then(res => {
            if(res.status === 401) { showToast("Please login to save"); throw new Error("Unauthorized"); }
            if(res.ok) {
                // Toggle black filled bookmark manually
                if (icon.style.fill === "var(--text-dark)") {
                    icon.style.color = "";
                    icon.style.fill = "none";
                    showToast("Removed from saved");
                } else {
                    icon.style.color = "var(--text-dark)";
                    icon.style.fill = "var(--text-dark)";
                    showToast("Saved");
                }
            }
        }).catch(e => {});
}

function deletePost(postId) {
    if(!confirm("Are you sure you want to delete this post?")) return;
    fetch(`/api/posts/${postId}`, { method: "DELETE" })
        .then(res => {
            if(res.status === 401) { showToast("Please login"); throw new Error("Unauthorized"); }
            if(res.status === 403) { showToast("Not authorized to delete this post"); throw new Error("Forbidden"); }
            if(res.ok) {
                showToast("Post deleted successfully");
                loadPosts();
            }
        }).catch(e => {});
}

function deleteComment(postId, commentId) {
    fetch(`/api/posts/${postId}/comments/${commentId}`, { method: "DELETE" })
        .then(res => {
            if(res.status === 401) { showToast("Please login"); throw new Error("Unauthorized"); }
            if(res.status === 403) { showToast("Not authorized to delete this comment"); throw new Error("Forbidden"); }
            if(res.ok) {
                showToast("Comment deleted");
                loadPosts();
            }
        }).catch(e => {});
}

function toggleCommentSection(postId) {
    const section = document.getElementById(`comments-section-${postId}`);
    if(section.style.display === "none") {
        section.style.display = "block";
    } else {
        section.style.display = "none";
    }
}

function submitComment(postId) {
    const input = document.getElementById(`comment-input-${postId}`);
    const text = input.value;
    if(!text.trim()) return;

    fetch(`/api/posts/${postId}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ text: text })
    })
    .then(res => {
        if(res.status === 401) { showToast("Please login to comment"); throw new Error("Unauthorized"); }
        return res.json();
    })
    .then(comments => {
        input.value = "";
        loadPosts();
    }).catch(e => {});
}

function handleCommentKeyPress(event, postId) {
    if (event.key === "Enter") {
        submitComment(postId);
    }
}

function sharePost(postId) {
    showToast("Link copied to clipboard!");
}

function sendPost(postId) {
    showToast("Direct Messaging coming soon!");
}

let currentSearchFilter = 'All';

function setSearchFilter(filterType, element) {
    currentSearchFilter = filterType;
    
    const links = document.querySelectorAll('.filter-link');
    links.forEach(link => link.classList.remove('active'));
    if (element) {
        element.classList.add('active');
    }
    
    const searchInput = document.getElementById("searchInput");
    if (searchInput && searchInput.value) {
        performSearch();
    } else {
        const resultsDiv = document.getElementById("searchResults");
        if(resultsDiv) {
            resultsDiv.innerHTML = `Type a query above to see <strong>${filterType}</strong> results.`;
        }
    }
}

function performSearch() {
    const searchInput = document.getElementById("searchInput");
    if(!searchInput) return;
    
    const query = searchInput.value;
    if (!query) return;

    fetch("/search/api?query=" + encodeURIComponent(query))
        .then(res => res.json())
        .then(users => {
            const resultsDiv = document.getElementById("searchResults");
            if(!resultsDiv) return;
            
            if (users.length === 0) {
                resultsDiv.innerHTML = `<div style="padding: 16px; text-align: center; color: var(--text-light);">No users found matching "${query}"</div>`;
                return;
            }
            
            let html = `<div style="display: flex; flex-direction: column; gap: 12px;">`;
            users.forEach(user => {
                const nameStr = user.username || 'Unknown';
                const avatarUrl = `https://ui-avatars.com/api/?name=${encodeURIComponent(nameStr)}&background=random`;
                const headline = user.headline ? user.headline : "Student at CVR College";
                
                html += `
                    <div class="user-search-card" style="display: flex; align-items: center; justify-content: space-between; padding: 12px; border: 1px solid var(--border-color); border-radius: 8px;">
                        <div style="display: flex; align-items: center; gap: 12px;">
                            <a href="/profile?userId=${user.id}">
                                <img src="${avatarUrl}" style="width: 48px; height: 48px; border-radius: 50%; object-fit: cover;" alt="Profile">
                            </a>
                            <div>
                                <a href="/profile?userId=${user.id}" style="text-decoration: none; color: var(--text-dark); font-weight: 600; font-size: 16px; display: block;">${nameStr}</a>
                                <span style="font-size: 13px; color: var(--text-light);">${headline}</span>
                            </div>
                        </div>
                        <a href="/profile?userId=${user.id}" class="btn-outline" style="text-decoration: none; padding: 6px 16px; border-radius: 20px;">View Profile</a>
                    </div>
                `;
            });
            html += `</div>
                <div style="margin-top: 16px; padding: 8px 12px; background: #eef3f8; border-radius: 4px; display: inline-block;">
                    <span style="color: var(--text-light); font-size: 12px;">Filtered by:</span>
                    <strong style="color: var(--primary-blue); font-size: 14px; margin-left: 4px;">${currentSearchFilter}</strong>
                </div>
            `;
            resultsDiv.innerHTML = html;
        }).catch(e => {
            console.error("Search error:", e);
            const resultsDiv = document.getElementById("searchResults");
            if(resultsDiv) resultsDiv.innerHTML = "Error fetching results.";
        });
}

// Global profile data loader for navbars and sidebars
if (window.location.pathname !== "/login" && window.location.pathname !== "/register" && window.location.pathname !== "/verify" && window.location.pathname !== "/") {
    fetch("/profile/data")
        .then(res => res.ok ? res.json() : null)
        .then(user => {
            if (user && user.username) {
                const nameStr = user.username;
                const avatarUrl = user.profilePhotoUrl
                    ? withCacheBust(user.profilePhotoUrl)
                    : `https://ui-avatars.com/api/?name=${encodeURIComponent(nameStr)}&background=random`;
                
                // Update nav avatar
                const navAvatar = document.getElementById("navAvatar");
                if (navAvatar) navAvatar.src = avatarUrl;
                
                // Update sidebar avatar and info
                const sidebarAvatar = document.getElementById("sidebarAvatar");
                if (sidebarAvatar) sidebarAvatar.src = avatarUrl;
                
                const sidebarName = document.getElementById("sidebarName");
                if (sidebarName) sidebarName.innerText = nameStr;
                
                const sidebarDept = document.getElementById("sidebarDept");
                if (sidebarDept) sidebarDept.innerText = user.department || 'Department';

                const createPostAvatar = document.getElementById("createPostAvatar");
                if (createPostAvatar) createPostAvatar.src = avatarUrl;
            }
        })
        .catch(err => console.log("User not logged in or API error", err));
    checkFollowRequests();
    setInterval(checkFollowRequests, 15000);
    
    // Inject notification dot if not present
    const notifItem = document.querySelector('a[href="/notifications"]');
    if (notifItem && !document.getElementById("notifDot")) {
        const dot = document.createElement("div");
        dot.id = "notifDot";
        dot.style.cssText = "position: absolute; top: 10px; right: 26px; width: 11px; height: 11px; background: var(--primary-blue); border-radius: 50%; border: 2px solid var(--white); display: none; z-index: 10;";
        notifItem.style.position = "relative";
        notifItem.appendChild(dot);
    }
}

let lastPendingCount = -1;
function checkFollowRequests() {
    fetch("/api/follow/pending")
        .then(res => res.ok ? res.json() : null)
        .then(requests => {
            if (!requests) return;
            
            const dot = document.getElementById("notifDot");
            if (dot) {
                dot.style.display = requests.length > 0 ? "block" : "none";
            }

            if (lastPendingCount === -1) {
                if (requests.length > 0) {
                    showToast(`You have ${requests.length} pending follow request(s)!`);
                }
            } else if (requests.length > lastPendingCount) {
                const latest = requests[requests.length - 1];
                showToast(`New follow request from ${latest.username}!`);
            }
            lastPendingCount = requests.length;
        }).catch(() => {});
}

// ================= PROFILE MODAL LOGIC =================
function openEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (!modal) return;
    
    // Fetch latest user data and populate form
    fetch('/profile/data')
        .then(res => res.json())
        .then(user => {
            document.getElementById('editName').value = user.username || '';
            document.getElementById('editHeadline').value = user.headline || '';
            document.getElementById('editDepartment').value = user.department || '';
            document.getElementById('editBio').value = user.bio || '';
            modal.classList.add('show');
        });
}

function closeEditProfileModal() {
    const modal = document.getElementById('editProfileModal');
    if (modal) modal.classList.remove('show');
}

function submitProfileUpdate() {
    const data = {
        username: document.getElementById('editName').value,
        headline: document.getElementById('editHeadline').value,
        department: document.getElementById('editDepartment').value,
        bio: document.getElementById('editBio').value
    };

    fetch('/profile/update', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(res => res.text())
    .then(msg => {
        showToast(msg);
        closeEditProfileModal();
        // Refresh profile card dynamically
        document.getElementById("profileName").innerText = data.username;
        document.getElementById("profileDept").innerText = (data.headline ? data.headline + " | " : "") + data.department;
        const bioEl = document.getElementById("profileBio");
        if (bioEl) bioEl.innerText = data.bio || 'Student at CVR College of Engineering.';
    });
}

// ================= EDIT ABOUT MODAL LOGIC =================
function openEditAboutModal() {
    const modal = document.getElementById('editAboutModal');
    if (!modal) return;

    fetch('/profile/data')
        .then(res => res.json())
        .then(user => {
            document.getElementById('editAboutBio').value = user.bio || '';
            modal.classList.add('show');
            lucide.createIcons();
        });
}

function closeEditAboutModal() {
    const modal = document.getElementById('editAboutModal');
    if (modal) modal.classList.remove('show');
}

function submitAboutUpdate() {
    const newBio = document.getElementById('editAboutBio').value;

    // We need to send all required fields, so fetch current data first
    fetch('/profile/data')
        .then(res => res.json())
        .then(user => {
            const data = {
                username: user.username,
                headline: user.headline || '',
                department: user.department || '',
                bio: newBio
            };

            return fetch('/profile/update', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });
        })
        .then(res => res.text())
        .then(msg => {
            showToast(msg);
            closeEditAboutModal();
            const bioEl = document.getElementById("profileBio");
            if (bioEl) bioEl.innerText = newBio || 'Student at CVR College of Engineering.';
        });
}
(function injectAiAssistant() {
    // Only inject on authenticated pages
    const path = window.location.pathname;
    if (path === "/home") {
        const aiHtml = `
            <!-- AI Assistant UI -->
            <div class="ai-fab" id="aiFab" onclick="toggleAiChat()">
                <i data-lucide="bot" style="width: 24px; height: 24px;"></i>
            </div>

            <div class="ai-chat-window" id="aiChatWindow">
                <div class="ai-chat-header">
                    <div style="display: flex; align-items: center; gap: 8px;">
                        <i data-lucide="sparkles" style="width: 18px; height: 18px;"></i> AI Assistant
                    </div>
                    <i data-lucide="x" class="ai-chat-close" onclick="toggleAiChat()"></i>
                </div>
                <div class="ai-chat-messages" id="aiChatMessages">
                    <div class="ai-message bot">Hi there! I'm your CVRgram AI Assistant. How can I help you today?</div>
                </div>
                <div class="ai-typing" id="aiTyping">AI is typing...</div>
                <div class="ai-chat-input-area">
                    <input type="text" class="ai-chat-input" id="aiChatInput" placeholder="Ask me anything..." onkeypress="handleAiKeyPress(event)">
                    <button class="ai-send-btn" onclick="sendAiMessage()">
                        <i data-lucide="send" style="width: 16px; height: 16px; margin-left: -2px;"></i>
                    </button>
                </div>
            </div>
        `;
        document.body.insertAdjacentHTML("beforeend", aiHtml);
        if (window.lucide) {
            lucide.createIcons();
        }
    }
})();

document.addEventListener("DOMContentLoaded", () => {
    const toggle = document.getElementById('darkModeToggle');
    if (toggle && localStorage.getItem('darkMode') === 'enabled') {
        toggle.classList.add('on');
    }
});

function toggleAiChat() {
    const chatWindow = document.getElementById('aiChatWindow');
    if (chatWindow) {
        chatWindow.classList.toggle('open');
        if (chatWindow.classList.contains('open')) {
            document.getElementById('aiChatInput').focus();
        }
    }
}

function handleAiKeyPress(event) {
    if (event.key === 'Enter') {
        sendAiMessage();
    }
}

function sendAiMessage() {
    const input = document.getElementById('aiChatInput');
    const message = input.value.trim();
    if (!message) return;

    appendAiMessage(message, 'user');
    input.value = '';

    document.getElementById('aiTyping').style.display = 'block';

    fetch('/api/ai/chat', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ message: message })
    })
    .then(res => res.text())
    .then(data => {
        document.getElementById('aiTyping').style.display = 'none';
        appendAiMessage(data, 'bot');
    })
    .catch(err => {
        document.getElementById('aiTyping').style.display = 'none';
        appendAiMessage("Sorry, I'm having trouble connecting to the server.", 'bot');
    });
}

function appendAiMessage(text, sender) {
    const messagesDiv = document.getElementById('aiChatMessages');
    const msgDiv = document.createElement('div');
    msgDiv.className = `ai-message ${sender}`;
    msgDiv.innerText = text;
    messagesDiv.appendChild(msgDiv);
    messagesDiv.scrollTop = messagesDiv.scrollHeight;
}

// 🔹 GLOBAL NOTIFICATION SYSTEM
let globalStompClient = null;
let globalUser = null;

async function initGlobalNotifications() {
    console.log("Global Notifications: Initializing...");
    try {
        const res = await fetch('/profile/data');
        if (!res.ok) {
            console.warn("Global Notifications: Not logged in.");
            return;
        }
        globalUser = await res.json();
        console.log("Global Notifications: Logged in as", globalUser.username, "(ID:", globalUser.id, ")");
        
        if (typeof SockJS !== 'undefined' && typeof Stomp !== 'undefined') {
            const socket = new SockJS('/ws');
            globalStompClient = Stomp.over(socket);
            globalStompClient.debug = null;
            globalStompClient.connect({}, () => {
                const topic = '/topic/notifications/' + globalUser.id;
                console.log("%c[NOTIFY] Connected! Listening on: " + topic, "color: #0073b1; font-weight: bold;");
                
                globalStompClient.subscribe(topic, (payload) => {
                    const msg = JSON.parse(payload.body);
                    console.log("%c[NOTIFY] Incoming from " + msg.sender.username + ": " + msg.content, "color: #ff4b4b; font-weight: bold;");
                    
                    // 1. Handle Active Chat Skip
                    if (window.location.pathname === '/chat' && typeof activeChatUser !== 'undefined' && activeChatUser && activeChatUser.id === msg.sender.id) {
                        return;
                    }

                    // 2. Update Chat Sidebar (if on chat page)
                    if (window.location.pathname === '/chat') {
                        const userItem = document.getElementById(`user-item-${msg.sender.id}`);
                        if (userItem && !userItem.querySelector('.sidebar-dot')) {
                            const dot = document.createElement('div');
                            dot.className = 'sidebar-dot';
                            Object.assign(dot.style, {
                                width: '10px', height: '10px', backgroundColor: '#0073b1',
                                borderRadius: '50%', marginLeft: 'auto', border: '1.5px solid white'
                            });
                            userItem.appendChild(dot);
                        }
                    }
                    
                    showMessageBadge();
                    showNotificationPopup(msg);
                });
            }, (err) => {
                console.error("[NOTIFY] Connection failed, retrying in 5s...", err);
                setTimeout(initGlobalNotifications, 5000);
            });
        } else {
            console.warn("Global Notifications: WebSocket libraries not loaded on this page.");
        }
    } catch (e) { console.error('Global Notifications: Init error', e); }
}

function showMessageBadge() {
    const msgNav = document.querySelector('a[href="/chat"]');
    if (!msgNav) return;

    const icon = msgNav.querySelector('i');
    if (!icon) return;

    if (msgNav.querySelector('.nav-badge')) return;

    const badge = document.createElement('div');
    badge.className = 'nav-badge';
    Object.assign(badge.style, {
        position: 'absolute',
        top: '1px',
        left: 'calc(50% + 6px)',
        width: '8px',
        height: '8px',
        backgroundColor: '#0073b1',
        borderRadius: '50%',
        border: '1.5px solid white',
        boxShadow: '0 1px 2px rgba(0,0,0,0.2)',
        zIndex: '100',
        pointerEvents: 'none',
        animation: 'pulseBadge 2s infinite'
    });
    
    msgNav.style.position = 'relative';
    msgNav.appendChild(badge);
    
    localStorage.setItem('hasUnreadMessages', 'true');
}

function clearMessageBadge() {
    const badge = document.querySelector('.nav-badge');
    if (badge) badge.remove();
    localStorage.removeItem('hasUnreadMessages');
}

// Add pulse animation
const badgeStyle = document.createElement('style');
badgeStyle.innerHTML = `
    @keyframes pulseBadge {
        0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0,115,177,0.4); }
        70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(0,115,177,0); }
        100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(0,115,177,0); }
    }
`;
document.head.appendChild(badgeStyle);

// Clear badge if we are on the chat page
if (window.location.pathname === '/chat') {
    clearMessageBadge();
}

// Restore badge on load if state exists
if (localStorage.getItem('hasUnreadMessages') === 'true' && window.location.pathname !== '/chat') {
    showMessageBadge();
}

// Global click listener to clear badge when messaging is clicked
document.addEventListener('click', (e) => {
    if (e.target.closest('a[href="/chat"]')) {
        clearMessageBadge();
    }
});

function showNotificationPopup(msg) {
    const popup = document.createElement('div');
    popup.className = 'global-notification-toast';
    const senderAvatar = msg.sender.profilePhotoUrl
        ? withCacheBust(msg.sender.profilePhotoUrl)
        : `https://ui-avatars.com/api/?name=${encodeURIComponent(msg.sender.username)}&background=random`;
    
    popup.innerHTML = `
        <div style="display: flex; gap: 12px; align-items: center; position: relative;">
            <img src="${senderAvatar}" style="width: 44px; height: 44px; border-radius: 50%; object-fit: cover; border: 2px solid #0073b1;">
            <div style="flex: 1; min-width: 0;">
                <div style="font-weight: 700; font-size: 14px; color: #1d1d1d; margin-bottom: 2px;">${msg.sender.username}</div>
                <div style="font-size: 13px; color: #5c5c5c; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; padding-right: 10px;">${msg.content}</div>
            </div>
            <a href="/chat" style="background: #0073b1; color: white; border: none; padding: 6px 14px; border-radius: 20px; cursor: pointer; font-size: 12px; font-weight: 600; text-decoration: none; transition: opacity 0.2s;">View</a>
            <div onclick="this.parentElement.parentElement.remove()" style="position: absolute; top: -10px; right: -10px; background: #eee; border-radius: 50%; width: 20px; height: 20px; display: flex; align-items: center; justify-content: center; cursor: pointer; font-size: 12px; border: 1px solid #ddd;">×</div>
        </div>
    `;

    Object.assign(popup.style, {
        position: 'fixed', top: '85px', right: '25px', backgroundColor: 'rgba(255, 255, 255, 0.98)',
        backdropFilter: 'blur(10px)', padding: '14px 18px', borderRadius: '12px',
        boxShadow: '0 8px 24px rgba(0,0,0,0.12)', zIndex: '99999', width: '320px',
        border: '1px solid rgba(0,0,0,0.05)', animation: 'slideInRight 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
        fontFamily: "'Inter', sans-serif"
    });

    document.body.appendChild(popup);
    setTimeout(() => {
        if (popup.parentElement) {
            popup.style.animation = 'slideOutRight 0.4s forwards';
            setTimeout(() => popup.remove(), 400);
        }
    }, 6000);
}

const notifyStyle = document.createElement('style');
notifyStyle.innerHTML = `
    @keyframes slideInRight { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    @keyframes slideOutRight { from { transform: translateX(0); opacity: 1; } to { transform: translateX(120%); opacity: 0; } }
`;
document.head.appendChild(notifyStyle);

initGlobalNotifications();

// 🔹 NOTIFICATION BELL SYSTEM (Follow Requests)
async function checkFollowRequests() {
    try {
        const res = await fetch('/api/follow/pending');
        if (res.ok) {
            const requests = await res.json();
            if (requests.length > 0) {
                showNotificationBadge();
            } else {
                clearNotificationBadge();
            }
        }
    } catch (e) {}
}

function showNotificationBadge() {
    const nav = document.querySelector('a[href="/notifications"]');
    if (!nav || nav.querySelector('.nav-badge')) return;
    const badge = document.createElement('div');
    badge.className = 'nav-badge';
    Object.assign(badge.style, {
        position: 'absolute', top: '1px', left: 'calc(50% + 6px)', width: '8px', height: '8px',
        backgroundColor: '#0073b1', borderRadius: '50%', border: '1.5px solid white',
        boxShadow: '0 1px 2px rgba(0,0,0,0.2)', zIndex: '100', pointerEvents: 'none',
        animation: 'pulseBadge 2s infinite'
    });
    nav.style.position = 'relative';
    nav.appendChild(badge);
}

function clearNotificationBadge() {
    const nav = document.querySelector('a[href="/notifications"]');
    const badge = nav?.querySelector('.nav-badge');
    if (badge) badge.remove();
}

// Background check every 15 seconds as requested
setInterval(checkFollowRequests, 15000);
checkFollowRequests();

// 🔹 INSTAGRAM-STYLE PFP VIEWER
function initPfpViewer() {
    // Create the modal element if it doesn't exist
    if (document.getElementById('pfpModal')) return;

    const modal = document.createElement('div');
    modal.id = 'pfpModal';
    Object.assign(modal.style, {
        position: 'fixed', top: '0', left: '0', width: '100%', height: '100%',
        backgroundColor: 'rgba(0,0,0,0.85)', backdropFilter: 'blur(10px)',
        zIndex: '100000', display: 'none', alignItems: 'center', justifyContent: 'center',
        cursor: 'zoom-out', transition: 'opacity 0.3s ease'
    });

    modal.innerHTML = `
        <div style="position: relative; max-width: 90%; max-height: 90%; display: flex; align-items: center; justify-content: center;">
            <img id="pfpModalImg" src="" style="max-width: 100%; max-height: 90vh; border-radius: 12px; box-shadow: 0 20px 50px rgba(0,0,0,0.5); transform: scale(0.8); transition: transform 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);">
            <div style="position: absolute; top: -40px; right: 0; color: white; font-size: 30px; cursor: pointer; font-family: sans-serif;">&times;</div>
        </div>
    `;

    document.body.appendChild(modal);

    modal.onclick = () => {
        modal.style.opacity = '0';
        document.getElementById('pfpModalImg').style.transform = 'scale(0.8)';
        setTimeout(() => { modal.style.display = 'none'; }, 300);
    };

    // Close on Escape key
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modal.style.display === 'flex') modal.click();
    });
}

function openPfpView(url) {
    const modal = document.getElementById('pfpModal');
    const img = document.getElementById('pfpModalImg');
    if (!modal || !img) return;

    img.src = url;
    modal.style.display = 'flex';
    modal.style.opacity = '0';
    
    // Trigger reflow
    modal.offsetHeight;
    
    modal.style.opacity = '1';
    img.style.transform = 'scale(1)';
}

// Attach listeners to profile pictures (Limited to Profile Page as requested)
document.addEventListener('click', (e) => {
    // Robust path check (handles /profile, /profile/, etc)
    const isProfilePage = window.location.pathname.startsWith('/profile');
    if (!isProfilePage) return;

    const target = e.target;
    
    // 1. Handle Main Profile Picture (IMG)
    const isProfilePfp = target.id === 'profileAvatar' || target.classList.contains('profile-avatar');
    if (isProfilePfp && target.tagName === 'IMG') {
        e.preventDefault();
        e.stopImmediatePropagation();
        initPfpViewer();
        openPfpView(target.src);
        return;
    }

    // 2. Handle Background Cover Photo (DIV with background-image)
    if (target.id === 'profileBg') {
        e.preventDefault();
        e.stopImmediatePropagation();
        
        const style = window.getComputedStyle(target);
        const bg = style.backgroundImage;
        
        if (bg && bg !== 'none' && bg.includes('url')) {
            // Extract URL from background-image: url("...")
            const url = bg.match(/url\((['"]?)(.*?)\1\)/)[2];
            initPfpViewer();
            openPfpView(url);
        }
    }
}, true); // 🔹 Use capture phase for maximum stability

// Initialize on load
initPfpViewer();

window.onload = () => {
    if (typeof lucide !== 'undefined') lucide.createIcons();
};