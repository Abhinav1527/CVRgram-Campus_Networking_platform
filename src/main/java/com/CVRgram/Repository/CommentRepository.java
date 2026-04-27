package com.CVRgram.Repository;

import com.CVRgram.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    void deleteByAuthorId(Long authorId);
}
