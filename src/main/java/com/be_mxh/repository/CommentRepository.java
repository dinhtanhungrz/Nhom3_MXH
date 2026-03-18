package com.be_mxh.repository;

import com.be_mxh.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  long countByStatusIdAndDeletedFalse(Long statusId);

  List<Comment> findAllByStatusIdAndDeletedFalseOrderByCreatedAtDesc(Long statusId);

  /**
   * Tìm comment theo ID và chưa bị xóa mềm (deleted = false).
   * Dùng trong service xóa comment để đảm bảo không tìm thấy comment đã xóa.
   */
  Optional<Comment> findByIdAndDeletedFalse(Long id);

  List<Comment> findByStatusIdAndDeletedFalseOrderByCreatedAtAsc(Long statusId);
}
