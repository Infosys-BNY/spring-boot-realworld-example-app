# Test Coverage Improvement Todo List

**Current Coverage:** 36% (was 33%, improved by +3%)
**Total Tests:** 105 tests passing (37 new tests added)

## ✅ Completed in This Session
- CommentReadService - 6 tests added
- UserRelationshipQueryService - 8 tests added
- Tag domain entity - 8 tests added
- FollowRelation domain entity - 8 tests added
- GraphQLCustomizeExceptionHandler - 8 tests added
- **Total:** 37 new unit tests, all passing

---

## 1. Repository Layer Testing

### ArticleMapper - Comprehensive CRUD Tests
- [ ] Test `insert()` - insert new article successfully
- [ ] Test `findById()` - find existing article
- [ ] Test `findById()` - return null for non-existent article
- [ ] Test `findBySlug()` - find article by slug
- [ ] Test `findBySlug()` - return null for non-existent slug
- [ ] Test `update()` - update article fields
- [ ] Test `delete()` - delete article by id
- [ ] Test `findTag()` - find existing tag
- [ ] Test `insertTag()` - insert new tag
- [ ] Test `insertArticleTagRelation()` - create article-tag relationship

### CommentMapper Tests (if not fully covered)
- [ ] Test insert operations
- [ ] Test findById operations
- [ ] Test delete operations

### UserMapper Tests (if not fully covered)
- [ ] Test insert user
- [ ] Test findByUsername
- [ ] Test findByEmail
- [ ] Test update user

### ArticleFavoriteMapper Tests (if not fully covered)
- [ ] Test favorite creation
- [ ] Test favorite deletion
- [ ] Test finding favorites by article/user

---

## 2. Service Layer Testing

### CommentReadService Tests
- [x] Test `findById()` - return comment data for valid id
- [x] Test `findById()` - return null for invalid id
- [x] Test `findByArticleId()` - return list of comments for article
- [x] Test `findByArticleId()` - return empty list for article with no comments
- [x] Test `findByArticleIdWithCursor()` - return paginated comments
- [x] Test `findByArticleIdWithCursor()` - handle cursor pagination correctly

### UserRelationshipQueryService Tests
- [x] Test `isUserFollowing()` - return true when user follows another
- [x] Test `isUserFollowing()` - return false when user doesn't follow
- [x] Test `followingAuthors()` - return set of followed author ids
- [x] Test `followingAuthors()` - return empty set when no follows
- [x] Test `followedUsers()` - return list of users followed by user
- [x] Test `followedUsers()` - return empty list when no follows

### UserService Tests
- [ ] Test `createUser()` - create user with valid parameters
- [ ] Test `createUser()` - validate email format
- [ ] Test `createUser()` - validate username uniqueness
- [ ] Test `createUser()` - encode password correctly
- [ ] Test `updateUser()` - update user with valid parameters
- [ ] Test `updateUser()` - validate email uniqueness on update
- [ ] Test `updateUser()` - validate username uniqueness on update
- [ ] Test `updateUser()` - handle optional fields correctly

### ArticleCommandService Tests (if not fully covered)
- [ ] Test article creation with tags
- [ ] Test article updates
- [ ] Test article deletion

### TagReadService Tests (if not already covered)
- [ ] Test finding all tags
- [ ] Test tag retrieval

### ArticleReadService Tests (if not fully covered)
- [ ] Test finding articles with filters
- [ ] Test article data retrieval

### UserReadService Tests (if not already covered)
- [ ] Test user data retrieval
- [ ] Test user profile queries

### ArticleFavoritesReadService Tests (if not already covered)
- [ ] Test finding favorites
- [ ] Test favorite counts

---

## 3. Domain Entity Testing

### Tag Tests
- [x] Test `Tag(String name)` constructor - generates UUID
- [x] Test `Tag(String name)` constructor - sets name correctly
- [x] Test equals() method - tags with same name are equal
- [x] Test hashCode() method - based on name field
- [ ] Test tag with null name handling
- [ ] Test tag with empty name handling

### FollowRelation Tests
- [x] Test `FollowRelation(userId, targetId)` constructor
- [x] Test constructor sets userId correctly
- [x] Test constructor sets targetId correctly
- [x] Test equals() and hashCode() methods
- [ ] Test with null parameters

### Article Tests (enhance existing if needed)
- [ ] Test article creation with all fields
- [ ] Test slug generation
- [ ] Test article updates

### Comment Tests (if not fully covered)
- [ ] Test comment creation
- [ ] Test comment relationships

### User Tests (if not fully covered)
- [ ] Test user creation
- [ ] Test password encoding
- [ ] Test user updates

---

## 4. GraphQL API Testing

### GraphQLCustomizeExceptionHandler Tests
- [x] Test `onException()` - handle InvalidAuthenticationException
- [x] Test `onException()` - return UNAUTHENTICATED error type
- [x] Test `onException()` - include correct error message and path
- [x] Test `onException()` - handle ConstraintViolationException
- [x] Test `onException()` - return BAD_REQUEST error type
- [x] Test `onException()` - extract field errors from violations
- [x] Test `onException()` - format errors map correctly
- [x] Test `onException()` - delegate to default handler for other exceptions
- [x] Test `getErrorsAsData()` - convert ConstraintViolationException to Error
- [x] Test `getErrorsAsData()` - group errors by field
- [x] Test `getErrorsAsData()` - handle multiple violations per field
- [ ] Test `getParam()` - extract parameter name from path
- [ ] Test `getParam()` - handle nested paths
- [ ] Test `getParam()` - handle simple paths
- [ ] Test `errorsToMap()` - convert FieldErrorResource list to map
- [ ] Test `errorsToMap()` - group messages by field

### GraphQL Mutations Tests (if not already covered)
- [ ] Test UserMutation operations
- [ ] Test ArticleMutation operations
- [ ] Test CommentMutation operations

### GraphQL DataFetchers Tests (if not already covered)
- [ ] Test article data fetching
- [ ] Test user data fetching
- [ ] Test comment data fetching

---

## 5. Integration Testing

### MyBatis Mapper XML Tests
- [ ] Test ArticleMapper.xml SQL queries execute correctly
- [ ] Test CommentMapper.xml SQL queries execute correctly
- [ ] Test UserMapper.xml SQL queries execute correctly
- [ ] Test ArticleFavoriteMapper.xml SQL queries execute correctly
- [ ] Test complex joins and relationships
- [ ] Test cursor-based pagination queries
- [ ] Test filtering and sorting operations

### Database Integration Tests
- [ ] Test article CRUD operations end-to-end
- [ ] Test comment CRUD operations end-to-end
- [ ] Test user follow/unfollow operations
- [ ] Test article favorite/unfavorite operations
- [ ] Test tag creation and association
- [ ] Test transaction rollback scenarios

---

## Priority Order

**High Priority (Critical Paths):**
1. GraphQLCustomizeExceptionHandler (authentication and validation)
2. CommentReadService (data retrieval)
3. UserRelationshipQueryService (social features)
4. ArticleMapper comprehensive CRUD tests
5. UserService (user management)

**Medium Priority:**
6. Domain entities (Tag, FollowRelation)
7. Other ReadServices
8. Integration tests for complex queries

**Low Priority:**
9. Additional edge case coverage
10. Performance testing scenarios

---

## Success Criteria

- [ ] Overall line coverage ≥ 80%
- [ ] Service layer coverage ≥ 80%
- [ ] Repository layer coverage ≥ 80%
- [ ] All critical authentication paths tested
- [ ] All data persistence operations tested
- [ ] Error handling scenarios covered
- [ ] All tests passing
- [ ] JaCoCo coverage verification passes
