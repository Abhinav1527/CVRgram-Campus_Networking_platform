package com.CVRgram.Controller;

import com.CVRgram.Model.Post;
import com.CVRgram.Model.User;
import com.CVRgram.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @GetMapping
    public List<Post> getPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/user/{userId}")
    public List<Post> getUserPosts(@PathVariable Long userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    @PostMapping
    public ResponseEntity<String> createPost(
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            HttpSession session) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).body("Not logged in.");
        }

        Post post = new Post();
        post.setContent(content);
        post.setAuthor(user);

        // Handle File Upload
        if (file != null && !file.isEmpty()) {
            try {
                File dir = new File(UPLOAD_DIR);
                if (!dir.exists()) dir.mkdirs();

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File dest = new File(dir.getAbsolutePath() + File.separator + filename);
                file.transferTo(dest);
                
                post.setImageUrl("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("File upload failed.");
            }
        }

        postRepository.save(post);
        return ResponseEntity.ok("Post created successfully!");
    }

    @Autowired
    private com.CVRgram.Repository.CommentRepository commentRepository;

    @PostMapping("/{id}/like")
    public ResponseEntity<String> toggleLike(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).body("Not logged in");

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        if (post.getLikedUserIds().contains(user.getId())) {
            post.getLikedUserIds().remove(user.getId());
        } else {
            post.getLikedUserIds().add(user.getId());
        }
        postRepository.save(post);
        return ResponseEntity.ok(String.valueOf(post.getLikedUserIds().size()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<List<com.CVRgram.Model.Comment>> addComment(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> data,
            HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        com.CVRgram.Model.Comment comment = new com.CVRgram.Model.Comment();
        comment.setText(data.get("text"));
        comment.setAuthor(user);
        comment.setPost(post);
        
        commentRepository.save(comment);
        
        // Refresh post to get latest comments list
        post = postRepository.findById(id).get();
        return ResponseEntity.ok(post.getComments());
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long postId, @PathVariable Long commentId, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).build();

        com.CVRgram.Model.Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) return ResponseEntity.notFound().build();

        if (!comment.getAuthor().getId().equals(user.getId()) && !comment.getPost().getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not authorized");
        }

        commentRepository.delete(comment);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        if (!post.getAuthor().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("Not authorized to delete this post");
        }

        // Optional: delete associated image from the file system if it exists
        if (post.getImageUrl() != null) {
            try {
                String filename = post.getImageUrl().substring(post.getImageUrl().lastIndexOf("/") + 1);
                File imageFile = new File(UPLOAD_DIR + filename);
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        postRepository.delete(post);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<?> toggleSave(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(id).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        if (post.getSavedUserIds().contains(user.getId())) {
            post.getSavedUserIds().remove(user.getId());
        } else {
            post.getSavedUserIds().add(user.getId());
        }
        postRepository.save(post);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/saved")
    public ResponseEntity<List<Post>> getSavedPosts(HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return ResponseEntity.status(401).build();

        List<Post> allPosts = postRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Post> savedPosts = new java.util.ArrayList<>();
        for (Post post : allPosts) {
            if (post.getSavedUserIds().contains(user.getId())) {
                savedPosts.add(post);
            }
        }
        return ResponseEntity.ok(savedPosts);
    }
}