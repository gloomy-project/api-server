package com.gloomy.server.application.comment;

import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.application.notice.NoticeService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.common.entity.Status;
import com.gloomy.server.domain.feed.Content;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.feed.Password;
import com.gloomy.server.domain.notice.Type;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    private final UserService userService;
    private final FeedService feedService;
    private final NoticeService noticeService;
    private final CommentRepository commentRepository;

    public CommentService(UserService userService, FeedService feedService, NoticeService noticeService, CommentRepository commentRepository) {
        this.userService = userService;
        this.feedService = feedService;
        this.noticeService = noticeService;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment createComment(Long userId, CommentDTO.Request commentDTO) throws IllegalArgumentException {
        validateCommentDTO(userId, commentDTO);
        Comment comment = commentRepository.save(makeComment(userId, commentDTO));
        noticeService.createNotice(comment.getFeedId(), comment, Type.COMMENT);
        return comment;
    }

    private Comment makeComment(Long userId, CommentDTO.Request commentDTO) {
        Feed feedId = feedService.findOneFeed(commentDTO.getFeedId());
        if (userId != null) {
            User user = userService.findUser(userId);
            return Comment.of(new Content(commentDTO.getContent()), feedId, user);
        }
        return Comment.of(new Content(commentDTO.getContent()), feedId, new Password(commentDTO.getPassword()));
    }

    private void validateCommentDTO(Long userId, CommentDTO.Request commentDTO) throws IllegalArgumentException {
        if ((commentDTO.getContent() == null || commentDTO.getFeedId() == null)
                || (userId == null && commentDTO.getPassword() == null)
                || (userId != null && commentDTO.getPassword() != null)) {
            throw new IllegalArgumentException("[CommentService] 회원 댓글 등록 요청 메시지가 잘못되었습니다.");
        }
    }

    @Transactional(readOnly = true)
    public Comment findComment(Long commentId) {
        if (commentId == null || commentId <= 0L) {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        }
        return commentRepository.findById(commentId).orElseThrow(() -> {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 존재하지 않습니다.");
        });
    }

    @Transactional
    public Comment updateComment(Long commentId, UpdateCommentDTO.Request updateCommentDTO) {
        validateUpdateCommentRequest(commentId, updateCommentDTO);
        Comment foundComment = findComment(commentId);
        foundComment.setContent(new Content(updateCommentDTO.getContent()));
        return commentRepository.save(foundComment);
    }

    private void validateUpdateCommentRequest(Long commentId, UpdateCommentDTO.Request updateCommentDTO) {
        if (commentId == null || commentId <= 0L) {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        }
        if (updateCommentDTO.getContent() == null) {
            throw new IllegalArgumentException("[CommentService] 댓글 수정 요청 메시지가 잘못되었습니다.");
        }
    }

    @Transactional
    public Comment deleteComment(Long commentId) {
        Comment foundComment = findComment(commentId);
        foundComment.delete();
        return commentRepository.save(foundComment);
    }

    @Transactional
    public void deleteAll() {
        commentRepository.deleteAll();
    }

    public Page<Comment> getFeedAllComments(Pageable pageable, Long feedId) {
        if (pageable == null) {
            throw new IllegalArgumentException("[CommentService] Pageable이 유효하지 않습니다.");
        }
        if (feedId == null || feedId <= 0L) {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        }
        List<Comment> feedAllComments = findAllComments(feedId);
        return new PageImpl<>(feedAllComments, pageable, feedAllComments.size());
    }

    @Transactional(readOnly = true)
    public Page<Comment> getFeedAllActiveComments(Pageable pageable, Long feedId) {
        if (pageable == null) {
            throw new IllegalArgumentException("[CommentService] Pageable이 유효하지 않습니다.");
        }
        if (feedId == null || feedId <= 0L) {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        }
        Feed foundFeed = feedService.findOneFeed(feedId);
        return commentRepository.findAllByFeedIdAndStatus(pageable, foundFeed, Status.active());
    }

    @Transactional(readOnly = true)
    public Page<Comment> getCommentByIdAndActive(Pageable pageable, Long userId) {
        if (pageable == null) {
            throw new IllegalArgumentException("[CommentService] Pageable이 유효하지 않습니다.");
        }
        if (userId == null || userId <= 0L) {
            throw new IllegalArgumentException("[CommentService] 해당 댓글 ID가 유효하지 않습니다.");
        }
        User findUser = userService.findUser(userId);
        return commentRepository.findAllByUserIdAndStatus(pageable, findUser, Status.active());
    }

    @Transactional(readOnly = true)
    public Integer getFeedAllActiveCommentsCount(Feed feedId) {
        return commentRepository.countCommentByFeedId(feedId);
    }

    @Transactional(readOnly = true)
    public List<Comment> findAllComments(Long feedId) {
        Feed foundFeed = feedService.findOneFeed(feedId);
        return commentRepository.findAllByFeedId(foundFeed);
    }
}
