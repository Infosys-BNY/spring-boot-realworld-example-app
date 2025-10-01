import React from "react";
import { mutate } from "swr";

import CommentAPI from "../../lib/api/comment";

const CommentReactions = ({ comment, slug }) => {
  const { likeCount = 0, dislikeCount = 0, userReaction } = comment;

  const handleLike = async (e) => {
    e.preventDefault();
    await CommentAPI.like(comment.id);
    mutate(`/articles/${slug}/comments`);
  };

  const handleDislike = async (e) => {
    e.preventDefault();
    await CommentAPI.dislike(comment.id);
    mutate(`/articles/${slug}/comments`);
  };

  const handleRemove = async (e) => {
    e.preventDefault();
    await CommentAPI.removeReaction(comment.id);
    mutate(`/articles/${slug}/comments`);
  };

  const isLiked = userReaction === "LIKE";
  const isDisliked = userReaction === "DISLIKE";

  return (
    <div className="comment-reactions">
      <button
        className={`btn btn-sm ${isLiked ? "btn-primary" : "btn-outline-primary"}`}
        onClick={isLiked ? handleRemove : handleLike}
        title={isLiked ? "Remove like" : "Like comment"}
      >
        <i className="ion-thumbsup" />
        <span className="counter"> {likeCount}</span>
      </button>
      &nbsp;
      <button
        className={`btn btn-sm ${isDisliked ? "btn-danger" : "btn-outline-danger"}`}
        onClick={isDisliked ? handleRemove : handleDislike}
        title={isDisliked ? "Remove dislike" : "Dislike comment"}
      >
        <i className="ion-thumbsdown" />
        <span className="counter"> {dislikeCount}</span>
      </button>
    </div>
  );
};

export default CommentReactions;
