import { axios } from '../../utils/request'

// 基础API路径
const COMMENTS_API = '/api/books/comments'

// 评论接口类型定义
export interface BookComment {
  id: number | string;
  bookId: string;
  bookTitle: string;
  bookCover: string;
  userId: number | string;
  userName: string;
  userAvatar: string;
  content: string;
  rating: number;
  createTime: string;
  updateTime: string | null;
}

export interface CommentStats {
  averageRating: number;
  commentCount: number;
}

// 重写评论提交函数，使用表单格式与URL参数
export const addComment = (bookId: string, userId: string | number, content: string, rating: number) => {
  // 准备请求参数
  console.log('评论参数准备:', { bookId, userId, content, rating });

  // 直接使用URL参数提交，与@RequestParam对应
  const url = `${COMMENTS_API}/?bookId=${encodeURIComponent(bookId)}&userId=${encodeURIComponent(String(userId))}&content=${encodeURIComponent(content)}&rating=${encodeURIComponent(String(rating))}`;
  
  console.log('评论请求URL:', url);
  
  // 使用GET方式提交请求参数而非JSON数据 (因为这样更符合@RequestParam的期望)
  return axios({
    method: 'post',
    url: url,
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  }).catch(error => {
    console.error('评论提交失败:', error);
    if (error.response) {
      console.error('状态码:', error.response.status);
      console.error('响应数据:', error.response.data);
    }
    throw error;
  });
}

// 获取书籍评论列表
export const getBookComments = (bookId: string, page: number = 0, size: number = 10) => {
  console.log('获取书籍评论:', bookId, page, size);
  
  return axios.get(`${COMMENTS_API}/${bookId}`, {
    params: { page, size }
  });
}

// 获取书籍评论统计
export const getBookCommentStats = (bookId: string) => {
  console.log('获取评论统计:', bookId);
  
  return axios.get(`${COMMENTS_API}/${bookId}/stats`);
}

// 检查用户是否已评论
export const checkUserComment = (bookId: string, userId: string) => {
  console.log('检查用户评论:', bookId, userId);
  
  return axios.get(`${COMMENTS_API}/${bookId}/check`, {
    params: { userId }
  });
}
