package com.gloomy.server.domain.reply;

import com.gloomy.server.domain.comment.Comment;
import com.gloomy.server.domain.feed.Content;
import com.gloomy.server.domain.feed.Feed;
import com.gloomy.server.domain.feed.Password;
import com.gloomy.server.domain.user.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import javax.persistence.*;
import java.util.Objects;

@Getter
@Entity
public class Reply {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Content content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Feed feedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Comment commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User userId;

    @Embedded
    private Password password;

    @Column(name = "status", nullable = false)
    private REPLY_STATUS status;

    Reply() {
    }

    @Builder(builderClassName = "userReplyBuilder", builderMethodName = "userReplyBuilder", access = AccessLevel.PRIVATE)
    private Reply(Content content, Feed feedId, Comment commentId, User userId, REPLY_STATUS status) {
        this.content = content;
        this.feedId = feedId;
        this.commentId = commentId;
        this.userId = userId;
        this.status = status;
    }

    @Builder(builderClassName = "nonUserReplyBuilder", builderMethodName = "nonUserReplyBuilder", access = AccessLevel.PRIVATE)
    private Reply(Content content, Feed feedId, Comment commentId, Password password, REPLY_STATUS status) {
        this.content = content;
        this.feedId = feedId;
        this.commentId = commentId;
        this.password = password;
        this.status = status;
    }

    public static Reply of(String content, Feed feedId, Comment commentId, User userId) {
        return Reply.userReplyBuilder()
                .content(new Content(content))
                .feedId(feedId)
                .commentId(commentId)
                .userId(userId)
                .status(REPLY_STATUS.ACTIVE)
                .build();
    }

    public static Reply of(String content, Feed feedId, Comment commentId, String password) {
        return Reply.nonUserReplyBuilder()
                .content(new Content(content))
                .feedId(feedId)
                .commentId(commentId)
                .password(new Password(password))
                .status(REPLY_STATUS.ACTIVE)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reply reply = (Reply) o;
        boolean result = Objects.equals(id, reply.getId())
                && Objects.equals(content.getContent(), reply.getContent().getContent())
                && Objects.equals(feedId.getId(), reply.getFeedId().getId())
                && Objects.equals(commentId.getId(), reply.getCommentId().getId())
                && status == reply.getStatus();
        if (Objects.nonNull(userId)) {
            return result && Objects.equals(userId.getId(), reply.getUserId().getId());
        }
        return result && Objects.equals(password.getPassword(), reply.getPassword().getPassword());
    }
}
