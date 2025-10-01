import React from "react";
import useSWR, { mutate } from "swr";

import CustomLink from "../common/CustomLink";
import CustomImage from "../common/CustomImage";
import Maybe from "../common/Maybe";
import DeleteButton from "./DeleteButton";
import checkLogin from "../../lib/utils/checkLogin";
import storage from "../../lib/utils/storage";
import CommentAPI from "../../lib/api/comment";
import { SERVER_BASE_URL } from "../../lib/utils/constant";

const Comment = ({ comment, articleSlug }) => {
  const { data: currentUser } = useSWR("user", storage);
  const isLoggedIn = checkLogin(currentUser);
  const canModify =
    isLoggedIn && currentUser?.username === comment?.author?.username;

  const handleLike = async () => {
    if (!isLoggedIn) return;
    const response = await CommentAPI.like(comment.id, currentUser?.token);
    if (response?.status === 200 && response?.data?.comment) {
      const swrKey = `${SERVER_BASE_URL}/articles/${articleSlug}/comments`;
      const updatedComment = response.data.comment;
      await mutate(swrKey, (data) => {
        if (!data) return data;
        return {
          ...data,
          comments: data.comments.map(c => 
            c.id === updatedComment.id ? updatedComment : c
          )
        };
      }, false);
    }
  };

  const handleDislike = async () => {
    if (!isLoggedIn) return;
    const response = await CommentAPI.dislike(comment.id, currentUser?.token);
    if (response?.status === 200 && response?.data?.comment) {
      const swrKey = `${SERVER_BASE_URL}/articles/${articleSlug}/comments`;
      const updatedComment = response.data.comment;
      await mutate(swrKey, (data) => {
        if (!data) return data;
        return {
          ...data,
          comments: data.comments.map(c => 
            c.id === updatedComment.id ? updatedComment : c
          )
        };
      }, false);
    }
  };

  const handleRemoveReaction = async () => {
    if (!isLoggedIn) return;
    const response = await CommentAPI.removeReaction(comment.id, currentUser?.token);
    if (response?.status === 200 && response?.data?.comment) {
      const swrKey = `${SERVER_BASE_URL}/articles/${articleSlug}/comments`;
      const updatedComment = response.data.comment;
      await mutate(swrKey, (data) => {
        if (!data) return data;
        return {
          ...data,
          comments: data.comments.map(c => 
            c.id === updatedComment.id ? updatedComment : c
          )
        };
      }, false);
    }
  };

  return (
    <div className="card">
      <div className="card-block">
        <p className="card-text">{comment.body}</p>
      </div>
      <div className="card-footer">
        <CustomLink
          href="profile/[pid]"
          as={`/profile/${comment.author.username}`}
          className="comment-author"
        >
          <CustomImage
            src={comment.author.image}
            alt="Comment author's profile image"
            className="comment-author-img"
          />
        </CustomLink>
        &nbsp;
        <CustomLink
          href="profile/[pid]"
          as={`/profile/${comment.author.username}`}
          className="comment-author"
        >
          {comment.author.username}
        </CustomLink>
        <span className="date-posted">
          {new Date(comment.createdAt).toDateString()}
        </span>
        <Maybe test={isLoggedIn && !canModify}>
          <span className="comment-reactions" style={{ marginLeft: '10px' }}>
            <button
              className={`btn btn-sm ${comment.userReaction === 'LIKE' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={comment.userReaction === 'LIKE' ? handleRemoveReaction : handleLike}
            >
              <i className="ion-thumbsup"></i> {comment.likeCount || 0}
            </button>
            {' '}
            <button
              className={`btn btn-sm ${comment.userReaction === 'DISLIKE' ? 'btn-primary' : 'btn-outline-primary'}`}
              onClick={comment.userReaction === 'DISLIKE' ? handleRemoveReaction : handleDislike}
            >
              <i className="ion-thumbsdown"></i> {comment.dislikeCount || 0}
            </button>
          </span>
        </Maybe>
        <Maybe test={canModify}>
          <DeleteButton commentId={comment.id} />
        </Maybe>
      </div>
    </div>
  );
};

export default Comment;
