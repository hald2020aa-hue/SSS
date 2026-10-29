# Security Specification (`security_spec.md`)

## 1. Data Invariants
1. **Strict Multi-User Isolation**: Every collection is nested under `/users/{userId}/...` (`ui_profiles`, `library`, `reading_sessions`). Only the authenticated user where `request.auth.uid == userId` and `incoming().userId == request.auth.uid` can read, list, create, update, or delete their own documents.
2. **Path Variable Hardening**: Every document ID (`userId`, `profileId`, `mangaId`, `sessionId`) must match `^[a-zA-Z0-9_\-]+$` and have length `<= 128`.
3. **Immutable Audit Fields**: On `update`, `userId` and `createdAt` are strictly immutable (`incoming().userId == existing().userId && incoming().createdAt == existing().createdAt`).
4. **Server Timestamps**: Both `createdAt` and `updatedAt` must be Firestore `timestamp` objects and satisfy `<= request.time`.

## 2. The "Dirty Dozen" Payloads
1. **Unauthenticated Read/Write**: `request.auth == null` attempting `get` or `list` on `/users/alice_123/ui_profiles`.
2. **Cross-User Read**: Authenticated `bob_456` attempting `get` or `list` on `/users/alice_123/library`.
3. **Identity Spoofing on Create**: `alice_123` creating `/users/alice_123/ui_profiles/p1` with `userId: "bob_456"`.
4. **Shadow Update / Ghost Field**: Adding `"isAdmin": true` to `/users/alice_123/ui_profiles/p1`.
5. **Invalid Enum Injection**: Setting `neonThemeId: "HACKED_THEME"` or `layoutMode: "INVALID_GRID"`.
6. **Out-of-Bounds Numeric Value**: Setting `neonGlowIntensity: 999` or `cardCornerRadiusDp: -5`.
7. **String Size Overflow**: Sending a 5000-character `profileName` or `title`.
8. **CreatedAt Mutation**: Updating a `MangaBookmark` while modifying `createdAt`.
9. **Owner Transfer on Update**: Updating a `MangaBookmark` to change `userId` to another user.
10. **Future Timestamp Spoofing**: Writing a client timestamp far in the future (`> request.time`).
11. **Malformed Document ID**: Creating a document with spaces or special characters in ID (`bad$id!`).
12. **Missing Required Keys**: Creating a `ReadingSessionLog` without `minutesSpent` or `pagesRead`.
