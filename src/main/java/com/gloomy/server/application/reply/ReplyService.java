package com.gloomy.server.application.reply;

import com.gloomy.server.application.comment.CommentService;
import com.gloomy.server.application.feed.FeedService;
import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.reply.Reply;
import com.gloomy.server.domain.user.User;
import com.gloomy.server.domain.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class ReplyService {
    private final UserService userService;
    private final FeedService feedService;
    private final CommentService commentService;
    private final ReplyRepository replyRepository;

    public ReplyService(UserService userService, FeedService feedService, CommentService commentService, ReplyRepository replyRepository) {
        this.userService = userService;
        this.feedService = feedService;
        this.commentService = commentService;
        this.replyRepository = replyRepository;
    }

    public Reply createReply(ReplyDTO.Request replyDTO) {
        validateReplyDTO(replyDTO);
        return replyRepository.save(makeReply(replyDTO));
    }

    private Reply makeReply(ReplyDTO.Request replyDTO) {
        Feed foundFeed = feedService.findOneFeed(replyDTO.getFeedId());
        Comment foundComment = commentService.findComment(replyDTO.getCommentId());
        if (replyDTO.getUserId() != null) {
            User foundUser = userService.findUser(replyDTO.getUserId());
            return replyRepository.save(Reply.of(replyDTO.getContent(), foundFeed, foundComment, foundUser));
        }
        return replyRepository.save(Reply.of(replyDTO.getContent(), foundFeed, foundComment, replyDTO.getPassword()));
    }

    private void validateReplyDTO(ReplyDTO.Request replyDTO) throws IllegalArgumentException {
        if ((replyDTO.getUserId() == null) == (replyDTO.getPassword() == null)
            || !validateCommonElements(replyDTO)) {
            throw new IllegalArgumentException("[ReplyService] 대댓글 등록 요청 메시지가 잘못되었습니다.");
        }
        if (replyDTO.getUserId() != null) {
            validateUserReplyDTO(replyDTO);
            return;
        }
        validateNonUserReplyDTO(replyDTO);
    }

    private boolean validateCommonElements(ReplyDTO.Request replyDTO) throws IllegalArgumentException {
        if ((replyDTO.getContent() == null || replyDTO.getContent().length() <= 0)
                || (replyDTO.getFeedId() == null || replyDTO.getFeedId() <= 0L)
                || (replyDTO.getCommentId() == null || replyDTO.getCommentId() <= 0L)) {
            return false;
        }
        return true;
    }

    private void validateUserReplyDTO(ReplyDTO.Request replyDTO) {
        if (replyDTO.getUserId() <= 0L) {
            throw new IllegalArgumentException("[ReplyService] 회원 대댓글 등록 요청 메시지가 잘못되었습니다.");
        }
    }

    private void validateNonUserReplyDTO(ReplyDTO.Request replyDTO) {
        if (replyDTO.getPassword().length() <= 0) {
            throw new IllegalArgumentException("[ReplyService] 비회원 대댓글 등록 요청 메시지가 잘못되었습니다.");
        }
    }

    public Reply findReply(Long replyId) {
        return replyRepository.findById(replyId).orElseThrow(() -> {
            throw new IllegalArgumentException("[ReplyService] 해당 대댓글 ID가 존재하지 않습니다.");
        });
    }

    public void deleteAll() {
        replyRepository.deleteAll();
    }
}
