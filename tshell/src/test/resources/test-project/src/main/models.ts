export interface User {
  id: number
  name: string
  email: string
  active: boolean
  createdAt: Date
}

export interface Post {
  id: number
  authorId: number
  title: string
  body: string
  published: boolean
}

export interface Comment {
  id: number
  postId: number
  userId: number
  text: string
}

export function validateUser(user: Partial<User>): string[] {
  const errors: string[] = []
  if (!user.name) errors.push("name is required")
  if (!user.email) errors.push("email is required")
  // TODO: add email format validation
  return errors
}

export function validatePost(post: Partial<Post>): string[] {
  const errors: string[] = []
  if (!post.title) errors.push("title is required")
  if (!post.body) errors.push("body is required")
  if (post.title && post.title.length > 200) errors.push("title too long")
  return errors
}
