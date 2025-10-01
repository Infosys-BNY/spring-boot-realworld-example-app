import axios from "axios";

import { SERVER_BASE_URL } from "../utils/constant";

const CommentAPI = {
  create: async (slug, comment) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/articles/${slug}/comments`,
        JSON.stringify({ comment })
      );
      return response;
    } catch (error) {
      return error.response;
    }
  },
  delete: async (slug, commentId) => {
    try {
      const response = await axios.delete(
        `${SERVER_BASE_URL}/articles/${slug}/comments/${commentId}`
      );
      return response;
    } catch (error) {
      return error.response;
    }
  },

  forArticle: async (slug, token) => {
    const headers = token ? { Authorization: `Token ${token}` } : {};
    const response = await axios.get(`${SERVER_BASE_URL}/articles/${slug}/comments`, { headers });
    return response.data;
  },

  like: async (commentId, token) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/comments/${commentId}/like`,
        {},
        {
          headers: {
            Authorization: `Token ${token}`,
          },
        }
      );
      return response;
    } catch (error) {
      return error.response;
    }
  },

  dislike: async (commentId, token) => {
    try {
      const response = await axios.post(
        `${SERVER_BASE_URL}/comments/${commentId}/dislike`,
        {},
        {
          headers: {
            Authorization: `Token ${token}`,
          },
        }
      );
      return response;
    } catch (error) {
      return error.response;
    }
  },

  removeReaction: async (commentId, token) => {
    try {
      const response = await axios.delete(
        `${SERVER_BASE_URL}/comments/${commentId}/reaction`,
        {
          headers: {
            Authorization: `Token ${token}`,
          },
        }
      );
      return response;
    } catch (error) {
      return error.response;
    }
  },
};

export default CommentAPI;
